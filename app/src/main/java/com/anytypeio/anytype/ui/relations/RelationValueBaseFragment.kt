package com.anytypeio.anytype.ui.relations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.RelationValueAdapter
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.editor.PickerDelegate
import com.anytypeio.anytype.ui.relations.add.AddObjectRelationFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import timber.log.Timber

abstract class  RelationValueBaseFragment<T: ViewBinding> : BaseBottomSheetFragment<T>(),
    OnStartDragListener,
    AddObjectRelationFragment.ObjectValueAddReceiver {

    protected val ctx get() = argString(CTX_KEY)
    protected val space get() = argString(SPACE_KEY)
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
                        layout = o.layout,
                        profileLinkIdentity = o.profileLinkIdentity,
                        space = o.space
                    )
                } else {
                    vm.onNonExistentObjectClicked(
                        ctx = ctx,
                        target = o.id,
                        space = o.space
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
            onFileClicked = { o -> vm.onFileClicked(target = o.id, space = o.space) },
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickerDelegate.initPicker(ctx)
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
        jobs += lifecycleScope.subscribe(vm.copyFileStatus) { command ->
            pickerDelegate.onCopyFileCommand(command)
        }
        jobs += lifecycleScope.subscribe(vm.isReadOnlyValue) { isReadOnly ->
            if (isReadOnly) {
                btnAddValue.gone()
                btnEditOrDone.gone()
            } else {
                btnAddValue.visible()
                btnEditOrDone.visible()
            }
        }
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
                    EditorFragment.args(
                        ctx = command.target,
                        space = command.space
                    )
                )
            }
            else -> toast("Unexpected nav command: $command")
        }
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
        pickerDelegate.onStop()
    }

    override fun onDestroyView() {
        pickerDelegate.clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickerDelegate.deleteTemporaryFile()
        pickerDelegate.clearOnCopyFile()
        super.onDestroy()
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

    private val pickerDelegate = PickerDelegate.Impl(this) { actions ->
        when (actions) {
            PickerDelegate.Actions.OnCancelCopyFileToCacheDir -> {
                vm.onCancelCopyFileToCacheDir()
            }
            is PickerDelegate.Actions.OnPickedDocImageFromDevice -> {
                onFilePathReady(actions.filePath)
            }
            is PickerDelegate.Actions.OnProceedWithFilePath -> {
                onFilePathReady(actions.filePath)
            }
            is PickerDelegate.Actions.OnStartCopyFileToCacheDir -> {
                vm.onStartCopyFileToCacheDir(actions.uri)
            }
        }
    }
    //endregion

    //region UPLOAD FILE LOGIC

    fun openGallery() {
        pickerDelegate.openFilePicker(Mimetype.MIME_IMAGE_AND_VIDEO)
    }

    protected fun openFilePicker() {
        pickerDelegate.openFilePicker(Mimetype.MIME_FILE_ALL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            pickerDelegate.resolveActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    //endregion

    abstract fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand)

    companion object {
        const val CTX_KEY = "arg.edit-cell-tag.ctx"
        const val SPACE_KEY = "arg.edit-cell-tag.space"
        const val IS_LOCKED_KEY = "arg.edit-cell-tag.locked"
        const val RELATION_KEY = "arg.edit-cell-tag.relation.key"
        const val TARGET_KEY = "arg.edit-cell-tag.target"
        const val DV_KEY = "arg.edit-cell-tag.dataview"
        const val VIEWER_KEY = "arg.edit-cell-tag.viewer"
        const val TARGET_TYPES_KEY = "arg.relation-value.target-types"
    }
}