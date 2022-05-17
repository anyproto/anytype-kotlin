package com.anytypeio.anytype.ui.relations

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.sets.RelationValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_FILE_SAF_CODE
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_MEDIA_CODE
import com.anytypeio.anytype.core_utils.ext.MIME_FILE_ALL
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.isPermissionGranted
import com.anytypeio.anytype.core_utils.ext.shouldShowRequestPermissionRationaleCompat
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.startFilePicker
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.databinding.FragmentRelationValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueViewModel
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.relations.add.AddFileRelationFragment
import com.anytypeio.anytype.ui.relations.add.AddObjectRelationFragment
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationDVFragment
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.google.android.material.snackbar.Snackbar
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import timber.log.Timber
import javax.inject.Inject

abstract class RelationValueBaseFragment : BaseBottomSheetFragment<FragmentRelationValueBinding>(),
    OnStartDragListener,
    AddObjectRelationFragment.ObjectValueAddReceiver,
    FileActionsFragment.FileActionReceiver,
    AddFileRelationFragment.FileValueAddReceiver,
    PickiTCallbacks {

    protected val ctx get() = argString(CTX_KEY)
    protected val relation get() = argString(RELATION_KEY)
    protected val target get() = argString(TARGET_KEY)
    protected val dataview get() = argString(DATAVIEW_KEY)
    protected val viewer get() = argString(VIEWER_KEY)
    protected val types get() = arg<List<String>>(TARGET_TYPES_KEY)
    protected val isLocked get() = arg<Boolean>(IS_LOCKED_KEY)

    abstract val vm: RelationValueBaseViewModel

    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }
    private lateinit var dividerItem: DividerItemDecoration
    private lateinit var dividerItemEdit: DividerItemDecoration

    private val dndBehavior by lazy {
        object : DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> relationValueAdapter.onItemMove(from, to) },
            onItemDropped = { onItemDropped() }
        ) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (viewHolder is DragAndDropViewHolder) return super.getMovementFlags(
                    recyclerView,
                    viewHolder
                )
                return makeMovementFlags(0, 0)
            }
        }
    }

    protected val relationValueAdapter by lazy {
        RelationValueAdapter(
            onCreateOptionClicked = {},
            onTagClicked = {},
            onStatusClicked = {},
            onRemoveStatusClicked = { status -> onRemoveStatusClicked(status) },
            onRemoveTagClicked = { tag -> onRemoveTagClicked(tag) },
            onObjectClicked = { o ->
                if (o is RelationValueView.Object.Default) {
                    vm.onObjectClicked(
                        ctx = ctx,
                        id = o.id,
                        layout = o.layout
                    )
                } else {
                    vm.onNonExistentObjectClicked(
                        ctx = ctx,
                        target = o.id
                    )
                }
            },
            onRemoveObjectClicked = { obj -> onRemoveObjectClicked(obj) },
            onFileClicked = { o -> vm.onFileClicked(o.id) },
            onRemoveFileClicked = { file -> onRemoveFileClicked(file) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = relationValueAdapter
        }
        dividerItem = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.divider_relations))
        }
        dividerItemEdit = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.divider_relations_edit))
        }
        with(lifecycleScope) {
            subscribe(binding.btnEditOrDone.clicks()) { vm.onEditOrDoneClicked(isLocked) }
            subscribe(binding.btnAddValue.clicks()) { vm.onAddValueClicked(isLocked) }
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.toasts) { toast(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.isDismissed) { observeDismiss(it) }
        jobs += lifecycleScope.subscribe(vm.isEditing) { observeEditing(it) }
        jobs += lifecycleScope.subscribe(vm.views) { relationValueAdapter.update(it) }
        jobs += lifecycleScope.subscribe(vm.name) { binding.tvTagOrStatusRelationHeader.text = it }
        jobs += lifecycleScope.subscribe(vm.navigation) { command -> navigate(command) }
        jobs += lifecycleScope.subscribe(vm.isLoading) { isLoading -> observeLoading(isLoading) }
        jobs += lifecycleScope.subscribe(vm.copyFileStatus) { command -> onCopyFileCommand(command) }
        super.onStart()
        vm.onStart(relationId = relation, objectId = target)
    }

    private fun observeLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.refresh.visible()
            binding.btnAddValue.invisible()
        } else {
            binding.refresh.gone()
            binding.btnAddValue.visible()
        }
    }

    private fun navigate(command: AppNavigation.Command) {
        when (command) {
            is AppNavigation.Command.OpenObjectSet -> {
                findNavController().navigate(
                    R.id.dataViewNavigation,
                    bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to command.target)
                )
            }
            is AppNavigation.Command.OpenObject -> {
                findNavController().navigate(
                    R.id.objectNavigation,
                    bundleOf(EditorFragment.ID_KEY to command.id)
                )
            }
            else -> toast("Unexpected nav command: $command")
        }
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        dndItemTouchHelper.startDrag(viewHolder)
    }

    private fun observeEditing(isEditing: Boolean) {
        if (isEditing) {
            binding.recycler.apply {
                removeItemDecoration(dividerItem)
                addItemDecoration(dividerItemEdit)
            }
            binding.btnAddValue.invisible()
            binding.btnEditOrDone.setText(R.string.done)
            dndItemTouchHelper.attachToRecyclerView(binding.recycler)
        } else {
            binding.recycler.apply {
                removeItemDecoration(dividerItemEdit)
                addItemDecoration(dividerItem)
            }
            binding.btnAddValue.visible()
            binding.btnEditOrDone.setText(R.string.edit)
            dndItemTouchHelper.attachToRecyclerView(null)
        }
    }

    private fun observeDismiss(isDismissed: Boolean) {
        if (isDismissed) {
            dismiss()
        }
    }

    //region PICK IT

    abstract fun onFilePathReady(filePath: String?)

    private lateinit var pickiT: PickiT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickiT = PickiT(requireContext(), this, requireActivity())
    }

    private var pickitProgressDialog: ProgressDialog? = null
    private var pickitProgressBar: ProgressBar? = null
    private var pickitAlertDialog: AlertDialog? = null

    override fun PickiTonUriReturned() {
        if (pickitProgressDialog == null || pickitProgressDialog?.isShowing == false) {
            pickitProgressDialog = ProgressDialog(requireContext()).apply {
                setMessage(getString(R.string.pickit_waiting))
                setCancelable(false)
            }
            pickitProgressDialog?.show()
        }
    }

    override fun PickiTonStartListener() {
        if (pickitProgressDialog?.isShowing == true) {
            pickitProgressDialog?.cancel()
        }
        pickitAlertDialog =
            AlertDialog.Builder(requireContext(), R.style.SyncFromCloudDialog).apply {
                val view =
                    LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
                setView(view)
                view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                    pickiT.cancelTask()
                    if (pickitAlertDialog?.isShowing == true) {
                        pickitAlertDialog?.cancel()
                    }
                }
                pickitProgressBar = view.findViewById(R.id.mProgressBar)
            }.create()
        pickitAlertDialog?.show()
        Timber.d("PickiTonStartListener")
    }

    override fun PickiTonProgressUpdate(progress: Int) {
        Timber.d("PickiTonProgressUpdate progress:$progress")
        pickitProgressBar?.progress = progress
    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        Timber.d("PickiTonCompleteListener path:$path, wasDriveFile:$wasDriveFile, wasUnknownProvider:$wasUnknownProvider, wasSuccessful:$wasSuccessful, reason:$Reason")
        if (pickitAlertDialog?.isShowing == true) {
            pickitAlertDialog?.cancel()
        }
        if (pickitProgressDialog?.isShowing == true) {
            pickitProgressDialog?.cancel()
        }
        if (BuildConfig.DEBUG) {
            when {
                wasDriveFile -> toast(getString(R.string.pickit_drive))
                wasUnknownProvider -> toast(getString(R.string.pickit_file_selected))
                else -> toast(getString(R.string.pickit_local_file))
            }
        }
        when {
            wasSuccessful -> onFilePathReady(path)
            else -> toast("Error: $Reason")
        }
    }

    private fun clearPickit() {
        val ctx = context
        if (ctx != null) {
            pickiT.deleteTemporaryFile(ctx)
        }
        pickiT.cancelTask()
        pickitAlertDialog?.dismiss()
        pickitProgressDialog?.dismiss()
    }
    //endregion

    //region READ PERMISSION
    private fun takeReadStoragePermission() {
        if (requireActivity().shouldShowRequestPermissionRationaleCompat(READ_EXTERNAL_STORAGE)) {
            binding.root.showSnackbar(
                R.string.permission_read_rationale,
                Snackbar.LENGTH_INDEFINITE,
                R.string.button_ok
            ) {
                permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
            }
        } else {
            permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private val permissionReadStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[READ_EXTERNAL_STORAGE]
            if (readResult == true) {
                startFilePicker(MIME_FILE_ALL)
            } else {
                binding.root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }
    //endregion

    //region UPLOAD FILE LOGIC
    private var mSnackbar: Snackbar? = null

    protected fun openFilePicker() {
        if (requireContext().isPermissionGranted(MIME_FILE_ALL)) {
            startFilePicker(MIME_FILE_ALL)
        } else {
            takeReadStoragePermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_MEDIA_CODE -> {
                    data?.data?.let { uri ->
                        pickiT.getPath(uri, Build.VERSION.SDK_INT)
                    }
                }
                REQUEST_FILE_SAF_CODE -> {
                    data?.data?.let { uri ->
                        vm.onStartCopyFileToCacheDir(uri)
                    } ?: run {
                        toast("Error while getting file")
                    }
                }
                else -> toast("Unknown Request Code:$requestCode")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onCopyFileCommand(command: CopyFileStatus) {
        when (command) {
            is CopyFileStatus.Error -> {
                mSnackbar?.dismiss()
                activity?.toast("Error while loading file:${command.msg}")
            }
            is CopyFileStatus.Completed -> {
                mSnackbar?.dismiss()
                onFilePathReady(command.result)
            }
            CopyFileStatus.Started -> {
                mSnackbar = binding.root.showSnackbar(
                    R.string.loading_file,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.cancel
                ) {
                    vm.onCancelCopyFileToCacheDir()
                }
            }
        }
    }

    private fun clearOnCopyFile() {
        vm.onCancelCopyFileToCacheDir()
        mSnackbar?.dismiss()
        mSnackbar = null
    }
    //endregion

    override fun onDestroyView() {
        clearPickit()
        clearOnCopyFile()
        super.onDestroyView()
    }

    abstract fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand)
    abstract fun onItemDropped()
    abstract fun onRemoveTagClicked(tag: RelationValueView.Option.Tag)
    abstract fun onRemoveStatusClicked(status: RelationValueView.Option.Status)
    abstract fun onRemoveObjectClicked(objectId: Id)
    abstract fun onRemoveFileClicked(fileId: Id)

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationValueBinding = FragmentRelationValueBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.edit-cell-tag.ctx"
        const val IS_LOCKED_KEY = "arg.edit-cell-tag.locked"
        const val RELATION_KEY = "arg.edit-cell-tag.relation"
        const val TARGET_KEY = "arg.edit-cell-tag.target"
        const val DATAVIEW_KEY = "arg.edit-cell-tag.dataview"
        const val VIEWER_KEY = "arg.edit-cell-tag.viewer"
        const val TARGET_TYPES_KEY = "arg.relation-value.target-types"
    }
}

open class RelationValueDVFragment : RelationValueBaseFragment() {

    @Inject
    lateinit var factory: RelationValueDVViewModel.Factory
    override val vm: RelationValueDVViewModel by viewModels { factory }

    override fun onRemoveTagClicked(tag: RelationValueView.Option.Tag) {
        vm.onRemoveTagFromDataViewRecordClicked(
            ctx = ctx,
            dataview = dataview,
            target = target,
            relation = relation,
            tag = tag.id,
            viewer = viewer
        )
    }

    override fun onRemoveStatusClicked(status: RelationValueView.Option.Status) {
        vm.onRemoveStatusFromDataViewRecordClicked(
            ctx = ctx,
            dataview = dataview,
            target = target,
            relation = relation,
            status = status.id,
            viewer = viewer
        )
    }

    override fun onRemoveObjectClicked(objectId: Id) {
        vm.onRemoveObjectFromDataViewRecordClicked(
            ctx = ctx,
            dataview = dataview,
            target = target,
            relation = relation,
            objectId = objectId
        )
    }

    override fun onRemoveFileClicked(fileId: Id) {
        vm.onRemoveFileFromDataViewRecordClicked(
            ctx = ctx,
            dataview = dataview,
            target = target,
            relation = relation,
            fileId = fileId
        )
    }

    override fun onItemDropped() {
        vm.onDataViewValueOrderChanged(
            ctx = ctx,
            obj = target,
            dv = dataview,
            relation = relation,
            order = relationValueAdapter.order()
        )
    }

    override fun onObjectValueChanged(
        ctx: Id,
        objectId: Id,
        relationId: Id,
        ids: List<Id>
    ) {
        vm.onAddObjectsOrFilesValueToRecord(
            ctx = ctx,
            dataview = dataview,
            record = target,
            relation = relation,
            ids = ids
        )
    }

    override fun onFileValueChanged(ctx: Id, objectId: Id, relationId: Id, ids: List<Id>) {
        vm.onAddObjectsOrFilesValueToRecord(
            ctx = ctx,
            dataview = dataview,
            record = target,
            relation = relation,
            ids = ids
        )
    }

    override fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand) {
        when (command) {
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddObjectScreen -> {
                val fragmentFlow = AddObjectRelationFragment.FLOW_DATAVIEW
                val fr = AddObjectRelationFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    types = types,
                    flow = fragmentFlow
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                val fr = AddOptionsRelationDVFragment.new(
                    ctx = ctx,
                    target = target,
                    relation = relation,
                    dataview = dataview,
                    viewer = viewer
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                val fr = AddFileRelationFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = AddFileRelationFragment.FLOW_DATAVIEW
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                //turn off for now https://app.clickup.com/t/h59z1j
                //FileActionsFragment().show(childFragmentManager, null)
                openFilePicker()
            }
        }
    }

    override fun onFileValueActionAdd() {
        vm.onFileValueActionAddClicked()
    }

    override fun onFileValueActionUploadFromGallery() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromGalleryClicked()
    }

    /**
     * Called when a file was picked from file picker.
     */
    override fun onFilePathReady(filePath: String?) {
        if (filePath != null) {
            vm.onAddFileToRecord(
                ctx = ctx,
                dataview = dataview,
                record = target,
                relation = relation,
                filePath = filePath
            )
        } else {
            Timber.e("Couldn't get file path")
        }
    }

    override fun onFileValueActionUploadFromStorage() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromStorageClicked()
    }

    override fun PickiTonMultipleCompleteListener(
        paths: ArrayList<String>?,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        toast("Not implemented yet")
    }

    override fun injectDependencies() {
        componentManager().objectSetObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetObjectRelationValueComponent.release(ctx)
    }
}

class RelationValueFragment : RelationValueBaseFragment() {

    @Inject
    lateinit var factory: RelationValueViewModel.Factory
    override val vm: RelationValueViewModel by viewModels { factory }

    override fun onRemoveTagClicked(tag: RelationValueView.Option.Tag) {
        vm.onRemoveTagFromObjectClicked(
            ctx = ctx,
            target = target,
            relation = relation,
            tag = tag.id
        )
    }

    override fun onRemoveStatusClicked(status: RelationValueView.Option.Status) {
        vm.onRemoveStatusFromObjectClicked(
            ctx = ctx,
            target = target,
            relation = relation,
            status = status.id
        )
    }

    override fun onRemoveObjectClicked(objectId: Id) {
        vm.onRemoveObjectFromObjectClicked(
            ctx = ctx,
            target = target,
            relation = relation,
            objectId = objectId
        )
    }

    override fun onRemoveFileClicked(fileId: Id) {
        vm.onRemoveFileFromObjectClicked(
            ctx = ctx,
            target = target,
            relation = relation,
            fileId = fileId
        )
    }

    override fun onItemDropped() {
        vm.onObjectValueOrderChanged(
            ctx = ctx,
            relation = relation,
            order = relationValueAdapter.order()
        )
    }

    override fun onObjectValueChanged(
        ctx: Id,
        objectId: Id,
        relationId: Id,
        ids: List<Id>
    ) {
        vm.onAddObjectsOrFilesValueToObject(
            ctx = ctx,
            target = target,
            relation = relation,
            ids = ids
        )
    }

    override fun onFileValueChanged(ctx: Id, objectId: Id, relationId: Id, ids: List<Id>) {
        vm.onAddObjectsOrFilesValueToObject(
            ctx = ctx,
            target = target,
            relation = relation,
            ids = ids
        )
    }

    override fun PickiTonMultipleCompleteListener(
        paths: ArrayList<String>?,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        toast("Not implemented yet")
    }

    override fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand) {
        when (command) {
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddObjectScreen -> {
                val fr = AddObjectRelationFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    types = types
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                val fr = AddOptionsRelationFragment.new(
                    ctx = ctx,
                    objectId = target,
                    relationId = relation
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                val fr = AddFileRelationFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = AddFileRelationFragment.FLOW_DEFAULT
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                //turn off for now https://app.clickup.com/t/h59z1j
                //FileActionsFragment().show(childFragmentManager, null)
                openFilePicker()
            }
        }
    }

    override fun onFileValueActionAdd() {
        vm.onFileValueActionAddClicked()
    }

    override fun onFileValueActionUploadFromGallery() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromGalleryClicked()
    }

    override fun onFileValueActionUploadFromStorage() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromStorageClicked()
    }

    /**
     * Called when a file was picked from file picker.
     */
    override fun onFilePathReady(filePath: String?) {
        if (filePath != null) {
            vm.onAddFileToObject(
                ctx = ctx,
                target = target,
                relation = relation,
                filePath = filePath
            )
        } else {
            Timber.e("Couldn't get file path")
        }
    }

    override fun injectDependencies() {
        componentManager().objectObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectObjectRelationValueComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            target: Id,
            relation: Id,
            targetObjectTypes: List<Id>,
            isLocked: Boolean = false
        ) = RelationValueFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relation,
                TARGET_TYPES_KEY to targetObjectTypes,
                IS_LOCKED_KEY to isLocked
            )
        }
    }
}