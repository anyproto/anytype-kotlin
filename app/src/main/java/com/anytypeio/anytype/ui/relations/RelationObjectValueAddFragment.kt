package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.RelationObjectValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.ObjectValueAddCommand
import com.anytypeio.anytype.presentation.relations.ObjectValueAddView
import com.anytypeio.anytype.presentation.relations.RelationObjectValueAddViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_relation_object_value_add.*
import javax.inject.Inject

class RelationObjectValueAddFragment : BaseDialogFragment() {

    private val behavior get() = BottomSheetBehavior.from(sheet)

    @Inject
    lateinit var factory: RelationObjectValueAddViewModel.Factory
    val vm: RelationObjectValueAddViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationId get() = argString(RELATION_ID)
    private val flow get() = arg<Int>(FLOW_KEY)

    private val adapter by lazy {
        RelationObjectValueAdapter(
            onObjectClick = { objectId -> vm.onObjectClicked(objectId) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_relation_object_value_add, container, false).apply {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(lifecycleScope) {
            subscribe(view.clicks()) { dismiss() }
        }
        rvObjects.layoutManager = LinearLayoutManager(requireContext())
        rvObjects.adapter = adapter
        btnBottomAction.setOnClickListener { vm.onActionButtonClicked() }
        setupBottomSheet()
    }

    private fun setupBottomSheet() {
        behavior.apply {
            skipCollapsed = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        if (slideOffset < 0)
                            btnAddContainer.gone()
                        else
                            btnAddContainer.visible()
                    }
                }
            )
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewsFiltered) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(searchRelationInput.textChanges())
        { vm.onFilterTextChanged(it.toString()) }
        super.onStart()
        setupAppearance()
        vm.onStart(objectId = objectId, relationId = relationId)
    }

    private fun observeState(state: ObjectValueAddView) {
        adapter.update(state.objects)
        tvObjectsCount.text = state.count
    }

    private fun observeCommands(command: ObjectValueAddCommand) {
        when (command) {
            is ObjectValueAddCommand.DispatchResult -> {
                dispatchResultAndDismiss(command.ids)
            }
        }
    }

    private fun dispatchResultAndDismiss(ids: List<Id>) {
        withParent<ObjectValueAddReceiver> {
            onObjectValueChanged(
                ctx = ctx,
                objectId = objectId,
                relationId = relationId,
                ids = ids
            )
        }
        dismiss()
    }

    private fun setupAppearance() {
        dialog?.window?.apply {
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.DefaultBottomDialogAnimation)
        }
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
        ) = RelationObjectValueAddFragment().apply {
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

    interface ObjectValueAddReceiver {
        fun onObjectValueChanged(
            ctx: Id,
            objectId: Id,
            relationId: Id,
            ids: List<Id>
        )
    }
}