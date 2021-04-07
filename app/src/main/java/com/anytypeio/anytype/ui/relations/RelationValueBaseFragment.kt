package com.anytypeio.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.sets.RelationValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.RelationValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.ui.relations.*
import kotlinx.android.synthetic.main.fragment_relation_value.*
import javax.inject.Inject

abstract class RelationValueBaseFragment : BaseBottomSheetFragment(),
    OnStartDragListener,
    RelationObjectValueAddFragment.ObjectValueAddReceiver,
    FileActionsFragment.FileActionReceiver,
    RelationFileValueAddFragment.FileValueAddReceiver
{

    protected val ctx get() = argString(CTX_KEY)
    protected val relation get() = argString(RELATION_KEY)
    protected val target get() = argString(TARGET_KEY)
    protected val dataview get() = argString(DATAVIEW_KEY)
    protected val viewer get() = argString(VIEWER_KEY)

    abstract val vm: RelationValueBaseViewModel

    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }

    private val dndBehavior by lazy {
        object : DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> editCellTagAdapter.onItemMove(from, to) },
            onItemDropped = { onItemDropped() }
        ) {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is DragAndDropViewHolder) return super.getMovementFlags(recyclerView, viewHolder)
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
            onObjectClicked = {},
            onRemoveObjectClicked = { obj -> onRemoveObjectClicked(obj) },
            onFileClicked = {},
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
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(filterInput.textChanges()) { vm.onFilterInputChanged(it.toString()) }
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
        jobs += lifecycleScope.subscribe(vm.isFilterVisible) {
            if (it) filterInputContainer.visible() else filterInputContainer.gone()
        }
        super.onStart()
        vm.onStart(relationId = relation, objectId = target)
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
            btnEditOrDone.setText(R.string.done)
            dndItemTouchHelper.attachToRecyclerView(recycler)
        } else {
            btnEditOrDone.setText(R.string.edit)
            dndItemTouchHelper.attachToRecyclerView(null)
        }
    }

    private fun observeDismiss(isDismissed: Boolean) {
        if (isDismissed) {
            filterInput.apply {
                clearFocus()
                hideKeyboard()
            }
            dismiss()
        }
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
                FileActionsFragment().show(childFragmentManager, null)
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
                FileActionsFragment().show(childFragmentManager, null)
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