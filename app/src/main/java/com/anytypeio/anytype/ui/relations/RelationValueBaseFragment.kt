package com.anytypeio.anytype.ui.relations

import android.Manifest
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
import androidx.annotation.StringRes
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
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.ui.page.REQUEST_FILE_CODE
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import kotlinx.android.synthetic.main.fragment_relation_value.*
import permissions.dispatcher.*
import timber.log.Timber
import javax.inject.Inject

@RuntimePermissions
abstract class RelationValueBaseFragment : BaseBottomSheetFragment(),
    OnStartDragListener,
    RelationObjectValueAddFragment.ObjectValueAddReceiver,
    FileActionsFragment.FileActionReceiver,
    RelationFileValueAddFragment.FileValueAddReceiver,
    PickiTCallbacks {

    protected val ctx get() = argString(CTX_KEY)
    protected val relation get() = argString(RELATION_KEY)
    protected val target get() = argString(TARGET_KEY)
    protected val dataview get() = argString(DATAVIEW_KEY)
    protected val viewer get() = argString(VIEWER_KEY)

    abstract val vm: RelationValueBaseViewModel

    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }
    private lateinit var dividerItem : DividerItemDecoration
    private lateinit var dividerItemEdit: DividerItemDecoration

    private val dndBehavior by lazy {
        object : DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> editCellTagAdapter.onItemMove(from, to) },
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

    protected val editCellTagAdapter by lazy {
        RelationValueAdapter(
            onCreateOptionClicked = {},
            onTagClicked = {},
            onStatusClicked = {},
            onRemoveStatusClicked = { status -> onRemoveStatusClicked(status) },
            onRemoveTagClicked = { tag -> onRemoveTagClicked(tag) },
            onObjectClicked = { o -> vm.onObjectClicked(o.id, o.type) },
            onRemoveObjectClicked = { obj -> onRemoveObjectClicked(obj) },
            onFileClicked = { o -> vm.onFileClicked(o.id) },
            onRemoveFileClicked = { file -> onRemoveFileClicked(file) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_relation_value, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = editCellTagAdapter
        }
        dividerItem = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.divider_relations))
        }
        dividerItemEdit = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.divider_relations_edit))
        }
        with(lifecycleScope) {
            subscribe(btnEditOrDone.clicks()) { vm.onEditOrDoneClicked() }
            subscribe(btnAddValue.clicks()) { vm.onAddValueClicked() }
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.isDimissed) { observeDismiss(it) }
        jobs += lifecycleScope.subscribe(vm.isEditing) { observeEditing(it) }
        jobs += lifecycleScope.subscribe(vm.views) { editCellTagAdapter.update(it) }
        jobs += lifecycleScope.subscribe(vm.name) { tvTagOrStatusRelationHeader.text = it }
        jobs += lifecycleScope.subscribe(vm.navigation) { command -> navigate(command) }
        jobs += lifecycleScope.subscribe(vm.isLoading) { isLoading -> observeLoading(isLoading)}
        super.onStart()
        vm.onStart(relationId = relation, objectId = target)
    }

    private fun observeLoading(isLoading: Boolean) {
        if (isLoading) {
            refresh.visible()
            btnAddValue.invisible()
        } else {
            refresh.gone()
            btnAddValue.visible()
        }
    }

    private fun navigate(command: AppNavigation.Command) {
        when (command) {
            is AppNavigation.Command.OpenObjectSet -> {
                findNavController().navigate(
                    R.id.objectSetScreen,
                    bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to command.target)
                )
            }
            is AppNavigation.Command.OpenObject -> {
                findNavController().navigate(
                    R.id.objectNavigation,
                    bundleOf(PageFragment.ID_KEY to command.id)
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
            recycler.apply {
                removeItemDecoration(dividerItem)
                addItemDecoration(dividerItemEdit)
            }
            btnAddValue.invisible()
            btnEditOrDone.setText(R.string.done)
            dndItemTouchHelper.attachToRecyclerView(recycler)
        } else {
            recycler.apply {
                removeItemDecoration(dividerItemEdit)
                addItemDecoration(dividerItem)
            }
            btnAddValue.visible()
            btnEditOrDone.setText(R.string.edit)
            dndItemTouchHelper.attachToRecyclerView(null)
        }
    }

    private fun observeDismiss(isDismissed: Boolean) {
        if (isDismissed) {
            dismiss()
        }
    }

    //region READ STORAGE

    abstract fun onFilePathReady(filePath: String?)

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun openGallery(type: String) {
        startActivityForResult(getVideoFileIntent(type), REQUEST_FILE_CODE)
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForReadExternalStoragePermission(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_read_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onReadExternalStoragePermissionDenied() {
        toast(getString(R.string.permission_read_denied))
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onReadExternalStoragePermissionNeverAskAgain() {
        toast(getString(R.string.permission_read_never_ask_again))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }

    private lateinit var pickiT: PickiT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickiT = PickiT(requireContext(), this, requireActivity())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_FILE_CODE -> {
                    data?.data?.let {
                        pickiT.getPath(it, Build.VERSION.SDK_INT)
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

    private var pickitProgressDialog: ProgressDialog? = null
    private var pickitProgressBar: ProgressBar? = null
    private var pickitAlertDialog: AlertDialog? = null

    override fun PickiTonUriReturned() {
        pickitProgressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.pickit_waiting))
            setCancelable(false)
        }
        pickitProgressDialog?.show()
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
        pickiT.cancelTask()
        pickitAlertDialog?.dismiss()
        pickitProgressDialog?.dismiss()
    }
    //endregion

    override fun onDestroyView() {
        clearPickit()
        super.onDestroyView()
    }

    abstract fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand)
    abstract fun onItemDropped()
    abstract fun onRemoveTagClicked(tag: RelationValueBaseViewModel.RelationValueView.Tag)
    abstract fun onRemoveStatusClicked(status: RelationValueBaseViewModel.RelationValueView.Status)
    abstract fun onRemoveObjectClicked(objectId: Id)
    abstract fun onRemoveFileClicked(fileId: Id)

    companion object {
        const val CTX_KEY = "arg.edit-cell-tag.ctx"
        const val RELATION_KEY = "arg.edit-cell-tag.relation"
        const val TARGET_KEY = "arg.edit-cell-tag.target"
        const val DATAVIEW_KEY = "arg.edit-cell-tag.dataview"
        const val VIEWER_KEY = "arg.edit-cell-tag.viewer"
    }
}

open class RelationValueDVFragment : RelationValueBaseFragment() {

    @Inject
    lateinit var factory: RelationValueDVViewModel.Factory
    override val vm: RelationValueDVViewModel by viewModels { factory }

    override fun onRemoveTagClicked(tag: RelationValueBaseViewModel.RelationValueView.Tag) {
        vm.onRemoveTagFromDataViewRecordClicked(
            ctx = ctx,
            dataview = dataview,
            target = target,
            relation = relation,
            tag = tag.id,
            viewer = viewer
        )
    }

    override fun onRemoveStatusClicked(status: RelationValueBaseViewModel.RelationValueView.Status) {
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
            order = editCellTagAdapter.order()
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
                val fragmentFlow = RelationObjectValueAddFragment.FLOW_DATAVIEW
                val fr = RelationObjectValueAddFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = fragmentFlow
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                val fr = RelationOptionValueDVAddFragment.new(
                    ctx = ctx,
                    target = target,
                    relation = relation,
                    dataview = dataview,
                    viewer = viewer
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                val fr = RelationFileValueAddFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = RelationFileValueAddFragment.FLOW_DATAVIEW
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                //turn off for now https://app.clickup.com/t/h59z1j
                //FileActionsFragment().show(childFragmentManager, null)
                openGalleryWithPermissionCheck(type = MIME_FILE_ALL)
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

    override fun onFilePathReady(filePath: String?) {
        if (filePath != null) {
            vm.onAddFileToRecord(
                ctx = ctx,
                dataview = dataview,
                record = target,
                relation = relation,
                filePath = filePath)
        } else {
            Timber.e("Couldn't get file path")
        }
    }

    override fun onFileValueActionUploadFromStorage() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromStorageClicked()
    }

    override fun injectDependencies() {
        componentManager().objectSetObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetObjectRelationValueComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            target: Id,
            relation: Id,
            dataview: Id,
            viewer: Id
        ) = RelationValueDVFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relation,
                DATAVIEW_KEY to dataview,
                VIEWER_KEY to viewer
            )
        }
    }
}

class RelationValueFragment : RelationValueBaseFragment() {

    @Inject
    lateinit var factory: RelationValueViewModel.Factory
    override val vm: RelationValueViewModel by viewModels { factory }

    override fun onRemoveTagClicked(tag: RelationValueBaseViewModel.RelationValueView.Tag) {
        vm.onRemoveTagFromObjectClicked(
            ctx = ctx,
            target = target,
            relation = relation,
            tag = tag.id
        )
    }

    override fun onRemoveStatusClicked(status: RelationValueBaseViewModel.RelationValueView.Status) {
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
            order = editCellTagAdapter.order()
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

    override fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand) {
        when (command) {
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddObjectScreen -> {
                val fr = RelationObjectValueAddFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                val fr = RelationOptionValueAddFragment.new(
                    ctx = ctx,
                    objectId = target,
                    relationId = relation
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                val fr = RelationFileValueAddFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = RelationFileValueAddFragment.FLOW_DEFAULT
                )
                fr.show(childFragmentManager, null)
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                //turn off for now https://app.clickup.com/t/h59z1j
                //FileActionsFragment().show(childFragmentManager, null)
                openGalleryWithPermissionCheck(type = MIME_FILE_ALL)
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

    override fun onFilePathReady(filePath: String?) {
        if (filePath != null) {
            vm.onAddFileToObject(
                ctx = ctx,
                target = target,
                relation = relation,
                filePath = filePath)
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
            relation: Id
        ) = RelationValueFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relation
            )
        }
    }
}