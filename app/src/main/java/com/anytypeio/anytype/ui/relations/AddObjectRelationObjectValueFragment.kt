package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.RelationObjectValueAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.AddObjectRelationObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.AddObjectValueCommand
import com.anytypeio.anytype.presentation.relations.AddObjectValueView
import kotlinx.android.synthetic.main.fragment_relation_value_object_add.*
import javax.inject.Inject

class AddObjectRelationObjectValueFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: AddObjectRelationObjectValueViewModel.Factory
    val vm: AddObjectRelationObjectValueViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationId get() = argString(RELATION_ID)
    private val flow get() = arg<Int>(FLOW_KEY)

    private val adapter by lazy {
        RelationObjectValueAdapter(
            onObjectClick = { objectId -> vm.onObjectClicked(objectId) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentBackground()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_relation_value_object_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvObjects.layoutManager = LinearLayoutManager(requireContext())
        rvObjects.adapter = adapter
        btnBottomAction.setOnClickListener { vm.onActionButtonClicked() }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewsFiltered) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(searchRelationInput.textChanges())
        { vm.onFilterTextChanged(it.toString()) }
        super.onStart()
        vm.onStart(objectId = objectId, relationId = relationId)
    }

    private fun observeState(state: AddObjectValueView) {
        adapter.update(state.objects)
        tvObjectsCount.text = state.count
    }

    private fun observeCommands(command: AddObjectValueCommand) {
        when (command) {
            is AddObjectValueCommand.DispatchResult -> {
                dispatchResultAndDismiss(command.ids)
            }
        }
    }

    private fun dispatchResultAndDismiss(ids: List<Id>) {
        withParent<AddObjectRelationObjectValueReceiver> {
            onRelationObjectValueChanged(
                ctx = ctx,
                objectId = objectId,
                relationId = relationId,
                ids = ids
            )
        }
        dismiss()
    }

    private fun setTransparentBackground() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun injectDependencies() {
        if (flow == FLOW_DEFAULT) {
            componentManager().addObjectRelationObjectValueComponent.get(ctx).inject(this)
        } else {
            componentManager().addObjectSetObjectRelationObjectValueComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (flow == FLOW_DEFAULT) {
            componentManager().addObjectRelationObjectValueComponent.release(ctx)
        } else {
            componentManager().addObjectSetObjectRelationObjectValueComponent.release(ctx)
        }
    }

    companion object {

        fun new(
            ctx: Id,
            objectId: Id,
            relationId: Id,
            flow: Int = FLOW_DEFAULT
        ) = AddObjectRelationObjectValueFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                OBJECT_ID to objectId,
                RELATION_ID to relationId,
                FLOW_KEY to flow
            )
        }

        const val CONTEXT_ID = "arg.relation.add.object.context"
        const val RELATION_ID = "arg.relation.add.object.relation.id"
        const val OBJECT_ID = "arg.relation.add.object.object.id"
        const val FLOW_KEY = "arg.relation.add.object.flow"
        const val FLOW_DEFAULT = 0
        const val FLOW_DATAVIEW = 1
    }

    interface AddObjectRelationObjectValueReceiver {
        fun onRelationObjectValueChanged(
            ctx: Id,
            objectId: Id,
            relationId: Id,
            ids: List<Id>
        )
    }
}