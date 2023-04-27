package com.anytypeio.anytype.ui.relations

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.RelationValueAdapter
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_FILE_SAF_CODE
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_MEDIA_CODE
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
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
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.relations.add.AddObjectRelationFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.google.android.material.snackbar.Snackbar
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import timber.log.Timber

abstract class  RelationValueBaseFragment<T: ViewBinding> : BaseBottomSheetFragment<T>(),
    OnStartDragListener,
    AddObjectRelationFragment.ObjectValueAddReceiver,
    PickiTCallbacks {

    protected val ctx get() = argString(CTX_KEY)
    protected val relationKey get() = argString(RELATION_KEY)
    protected val target get() = argString(TARGET_KEY)
    protected val dv get() = argString(DV_KEY)
    protected val viewer get() = argString(VIEWER_KEY)
    protected val types get() = arg<List<String>>(TARGET_TYPES_KEY)
    protected val isLocked get() = arg<Boolean>(IS_LOCKED_KEY)

    abstract val vm: RelationValueBaseViewModel

    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }
    protected lateinit var dividerItem: RecyclerView.ItemDecoration
    protected lateinit var dividerItemEdit: RecyclerView.ItemDecoration

    protected abstract val root: View
    protected abstract val recycler: RecyclerView
    protected abstract val btnAddValue: ImageView
    protected abstract val btnEditOrDone: TextView
    protected abstract val refresh: ProgressBar
    protected abstract val tvRelationHeader: TextView

    protected abstract val onStatusClickedCallback: (RelationValueView.Option.Status) -> Unit
    protected abstract fun observeViews(values: List<RelationValueView>)

    private val dndBehavior by lazy {
        object : DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> relationValueAdapter.onItemMove(from, to) },
            onItemDropped = {
                vm.onObjectValueOrderChanged(
                    target = target,
                    relationKey = relationKey,
                    order = relationValueAdapter.order()
                )
            }
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
            onStatusClicked = onStatusClickedCallback,
            onRemoveStatusClicked = { status ->
                vm.onRemoveStatusFromObjectClicked(
                    ctx = ctx,
                    target = target,
                    relationKey = relationKey,
                    status = status.id
                )
            },
            onRemoveTagClicked = { tag ->
                vm.onRemoveTagFromObjectClicked(
                    ctx = ctx,
                    target = target,
                    relationKey = relationKey,
                    tag = tag.id
                )
            },
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
            onRemoveObjectClicked = { obj ->
                vm.onRemoveObjectFromObjectClicked(
                    ctx = ctx,
                    target = target,
                    relationKey = relationKey,
                    objectId = obj
                )
            },
            onFileClicked = { o -> vm.onFileClicked(o.id) },
            onRemoveFileClicked = { file ->
                vm.onRemoveFileFromObjectClicked(
                    ctx = ctx,
                    target = target,
                    relationKey = relationKey,
                    fileId = file
                )
            }
        )
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.toasts) { toast(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.isDismissed) { observeDismiss(it) }
        jobs += lifecycleScope.subscribe(vm.isEditing) { observeEditing(it) }
        jobs += lifecycleScope.subscribe(vm.views) { observeViews(it) }
        jobs += lifecycleScope.subscribe(vm.name) { tvRelationHeader.text = it }
        jobs += lifecycleScope.subscribe(vm.navigation) { command -> navigate(command) }
        jobs += lifecycleScope.subscribe(vm.isLoading) { isLoading -> observeLoading(isLoading) }
        jobs += lifecycleScope.subscribe(vm.copyFileStatus) { command -> onCopyFileCommand(command) }
        super.onStart()
        vm.onStart(
            ctx = ctx,
            relationKey = relationKey,
            objectId = target,
        )
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
            is AppNavigation.Command.OpenSetOrCollection -> {
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

    //region PICK IT

    /**
     * Called when a file was picked from file picker.
     */
    private fun onFilePathReady(filePath: String?) {
        if (filePath != null) {
            vm.onAddFileToObject(
                ctx = ctx,
                target = target,
                relationKey = relationKey,
                filePath = filePath
            )
        } else {
            Timber.e("Couldn't get file path")
        }
    }

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
            root.showSnackbar(
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
                startFilePicker(Mimetype.MIME_FILE_ALL)
            } else {
                root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }
    //endregion

    //region UPLOAD FILE LOGIC
    private var mSnackbar: Snackbar? = null

    protected fun openFilePicker() {
        if (requireContext().isPermissionGranted(Mimetype.MIME_FILE_ALL)) {
            startFilePicker(Mimetype.MIME_FILE_ALL)
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
                mSnackbar = root.showSnackbar(
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

    companion object {
        const val CTX_KEY = "arg.edit-cell-tag.ctx"
        const val IS_LOCKED_KEY = "arg.edit-cell-tag.locked"
        const val RELATION_KEY = "arg.edit-cell-tag.relation.key"
        const val TARGET_KEY = "arg.edit-cell-tag.target"
        const val DV_KEY = "arg.edit-cell-tag.dataview"
        const val VIEWER_KEY = "arg.edit-cell-tag.viewer"
        const val TARGET_TYPES_KEY = "arg.relation-value.target-types"
    }
}