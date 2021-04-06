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
import com.anytypeio.anytype.core_ui.features.sets.ObjectRelationValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ObjectObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.sets.ObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetObjectRelationValueViewModel
import com.anytypeio.anytype.ui.relations.*
import kotlinx.android.synthetic.main.object_relation_value_fragment.*
import javax.inject.Inject

abstract class ObjectRelationValueFragment : BaseBottomSheetFragment(),
    OnStartDragListener,
    AddObjectRelationObjectValueFragment.AddObjectRelationObjectValueReceiver,
    RelationFileValueActionsFragment.RelationFileValueActionReceiver,
    RelationFileValueAddFragment.AddRelationFileValueReceiver
{

    protected val ctx get() = argString(CTX_KEY)
    protected val relation get() = argString(RELATION_KEY)
    protected val target get() = argString(TARGET_KEY)
    protected val dataview get() = argString(DATAVIEW_KEY)
    protected val viewer get() = argString(VIEWER_KEY)

    abstract val vm: ObjectRelationValueViewModel

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
        ObjectRelationValueAdapter(
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
    ): View = inflater.inflate(R.layout.object_relation_value_fragment, container, false)

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

    abstract fun observeCommands(command: ObjectRelationValueViewModel.ObjectRelationValueCommand)
    abstract fun onItemDropped()
    abstract fun onRemoveTagClicked(tag: ObjectRelationValueViewModel.ObjectRelationValueView.Tag)
    abstract fun onRemoveStatusClicked(status: ObjectRelationValueViewModel.ObjectRelationValueView.Status)
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

open class ObjectSetObjectRelationValueFragment : ObjectRelationValueFragment() {

    @Inject
    lateinit var factory: ObjectSetObjectRelationValueViewModel.Factory
    override val vm: ObjectSetObjectRelationValueViewModel by viewModels { factory }

    override fun onRemoveTagClicked(tag: ObjectRelationValueViewModel.ObjectRelationValueView.Tag) {
        vm.onRemoveTagFromDataViewRecordClicked(
            ctx = ctx,
            dataview = dataview,
            target = target,
            relation = relation,
            tag = tag.id,
            viewer = viewer
        )
    }

    override fun onRemoveStatusClicked(status: ObjectRelationValueViewModel.ObjectRelationValueView.Status) {
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

    override fun onRelationObjectValueChanged(
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

    override fun onRelationFileValueChanged(ctx: Id, objectId: Id, relationId: Id, ids: List<Id>) {
        vm.onAddObjectsOrFilesValueToRecord(
            ctx = ctx,
            dataview = dataview,
            record = target,
            relation = relation,
            ids = ids
        )
    }

    override fun observeCommands(command: ObjectRelationValueViewModel.ObjectRelationValueCommand) {
        when (command) {
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowAddObjectScreen -> {
                val fragmentFlow = AddObjectRelationObjectValueFragment.FLOW_DATAVIEW
                val fr = AddObjectRelationObjectValueFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = fragmentFlow
                )
                fr.show(childFragmentManager, null)
            }
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                val fr = AddObjectSetObjectRelationValueFragment.new(
                    ctx = ctx,
                    target = target,
                    relation = relation,
                    dataview = dataview,
                    viewer = viewer
                )
                fr.show(childFragmentManager, null)
            }
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                val fr = RelationFileValueAddFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = RelationFileValueAddFragment.FLOW_DATAVIEW
                )
                fr.show(childFragmentManager, null)
            }
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                RelationFileValueActionsFragment().show(childFragmentManager, null)
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
        ) = ObjectSetObjectRelationValueFragment().apply {
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

class ObjectObjectRelationValueFragment : ObjectRelationValueFragment() {

    @Inject
    lateinit var factory: ObjectObjectRelationValueViewModel.Factory
    override val vm: ObjectObjectRelationValueViewModel by viewModels { factory }

    override fun onRemoveTagClicked(tag: ObjectRelationValueViewModel.ObjectRelationValueView.Tag) {
        vm.onRemoveTagFromObjectClicked(
            ctx = ctx,
            target = target,
            relation = relation,
            tag = tag.id
        )
    }

    override fun onRemoveStatusClicked(status: ObjectRelationValueViewModel.ObjectRelationValueView.Status) {
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

    override fun onRelationObjectValueChanged(
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

    override fun onRelationFileValueChanged(ctx: Id, objectId: Id, relationId: Id, ids: List<Id>) {
        vm.onAddObjectsOrFilesValueToObject(
            ctx = ctx,
            target = target,
            relation = relation,
            ids = ids
        )
    }

    override fun observeCommands(command: ObjectRelationValueViewModel.ObjectRelationValueCommand) {
        when (command) {
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowAddObjectScreen -> {
                val fr = AddObjectRelationObjectValueFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target
                )
                fr.show(childFragmentManager, null)
            }
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                val fr = AddObjectObjectRelationValueFragment.new(
                    ctx = ctx,
                    objectId = target,
                    relationId = relation
                )
                fr.show(childFragmentManager, null)
            }
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                val fr = RelationFileValueAddFragment.new(
                    ctx = ctx,
                    relationId = relation,
                    objectId = target,
                    flow = RelationFileValueAddFragment.FLOW_DEFAULT
                )
                fr.show(childFragmentManager, null)
            }
            ObjectRelationValueViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                RelationFileValueActionsFragment().show(childFragmentManager, null)
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
        ) = ObjectObjectRelationValueFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relation
            )
        }
    }
}