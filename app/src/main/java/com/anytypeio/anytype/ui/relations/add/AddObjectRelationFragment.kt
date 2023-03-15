package com.anytypeio.anytype.ui.relations.add

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.features.relations.RelationObjectValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.databinding.FragmentRelationObjectValueAddBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.add.AddObjectRelationViewModel
import com.anytypeio.anytype.presentation.relations.add.ObjectValueAddCommand
import com.anytypeio.anytype.presentation.relations.add.ObjectValueAddView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class AddObjectRelationFragment : BaseDialogFragment<FragmentRelationObjectValueAddBinding>() {

    private val behavior get() = BottomSheetBehavior.from(binding.sheet)

    @Inject
    lateinit var factory: AddObjectRelationViewModel.Factory
    val vm: AddObjectRelationViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationKey get() = argString(RELATION_KEY)
    private val types get() = arg<List<String>>(TARGET_TYPES)
    private val flow get() = arg<Int>(FLOW_KEY)

    private lateinit var searchRelationInput: EditText
    private lateinit var clearSearchText: View

    private val adapter by lazy {
        RelationObjectValueAdapter(
            onObjectClick = { objectId -> vm.onObjectClicked(objectId) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(lifecycleScope) {
            subscribe(view.clicks()) { dismiss() }
        }
        binding.rvObjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvObjects.adapter = adapter
        binding.btnBottomAction.setOnClickListener { vm.onActionButtonClicked() }
        setupBottomSheet()
        searchRelationInput = binding.searchBar.root.findViewById(R.id.filterInputField)
        searchRelationInput.apply {
            hint = getString(R.string.choose_options)
        }
        clearSearchText = binding.searchBar.root.findViewById(R.id.clearSearchText)
        clearSearchText.setOnClickListener {
            searchRelationInput.setText("")
            clearSearchText.invisible()
        }
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
                        if (binding.btnAddContainer == null) return
                        if (slideOffset < 0)
                            binding.btnAddContainer.gone()
                        else
                            binding.btnAddContainer.visible()
                    }
                }
            )
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewsFiltered) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(searchRelationInput.textChanges())
        {
            if (it.isEmpty()) clearSearchText.invisible() else clearSearchText.visible()
            vm.onFilterTextChanged(it.toString())
        }
        super.onStart()
        setupAppearance()
        vm.onStart(
            ctx = ctx,
            objectId = objectId,
            relationKey = relationKey,
            targetTypes = types
        )
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun observeState(state: ObjectValueAddView) {
        adapter.update(state.objects)
        binding.tvObjectsCount.text = state.count
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
                relationKey = relationKey,
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

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationObjectValueAddBinding = FragmentRelationObjectValueAddBinding.inflate(
        inflater, container, false
    )

    companion object {

        fun new(
            ctx: Id,
            objectId: Id,
            relationKey: Key,
            types: List<Id>,
            flow: Int = FLOW_DEFAULT
        ) = AddObjectRelationFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                OBJECT_ID to objectId,
                RELATION_KEY to relationKey,
                TARGET_TYPES to types,
                FLOW_KEY to flow
            )
        }

        const val CONTEXT_ID = "arg.relation.add.object.context"
        const val RELATION_KEY = "arg.relation.add.object.relation.key"
        const val OBJECT_ID = "arg.relation.add.object.object.id"
        const val TARGET_TYPES = "arg.relation.add.object.target_types"
        const val FLOW_KEY = "arg.relation.add.object.flow"
        const val FLOW_DEFAULT = 0
        const val FLOW_DATAVIEW = 1
    }

    interface ObjectValueAddReceiver {
        fun onObjectValueChanged(
            ctx: Id,
            objectId: Id,
            relationKey: Key,
            ids: List<Id>
        )
    }
}