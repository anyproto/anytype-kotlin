package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.features.relations.DocumentRelationAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.argStringOrNull
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentRelationListBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.RelationListViewModel.Command
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

open class ObjectRelationListFragment : BaseBottomSheetFragment<FragmentRelationListBinding>(),
    RelationTextValueFragment.TextValueEditReceiver,
    RelationDateValueFragment.DateValueEditReceiver {

    private val vm by viewModels<RelationListViewModel> { factory }

    @Inject
    lateinit var factory: ObjectRelationListViewModelFactory

    private lateinit var searchRelationInput: EditText
    private lateinit var clearSearchText: View

    private val ctx: String get() = argString(ARG_CTX)
    private val target: String? get() = argStringOrNull(ARG_TARGET)
    private val mode: Int get() = argInt(ARG_MODE)
    private val isLocked: Boolean get() = arg(ARG_LOCKED)
    private val isDataViewFLow: Boolean get() = arg(ARG_DATA_VIEW_FLOW)

    private val docRelationAdapter by lazy {
        DocumentRelationAdapter(
            items = emptyList(),
            onRelationClicked = {
                vm.onRelationClicked(
                    ctx = ctx,
                    target = target,
                    view = it.view
                )
            },
            onCheckboxClicked = {
                vm.onCheckboxClicked(
                    ctx = ctx,
                    view = it.view
                )
            },
            onDeleteClicked = {
                vm.onDeleteClicked(
                    ctx = ctx,
                    view = it.view
                )
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchRelationInput = binding.searchBar.root.findViewById(R.id.filterInputField)
        searchRelationInput.apply {
            hint = getString(R.string.choose_options)
        }
        clearSearchText = binding.searchBar.root.findViewById(R.id.clearSearchText)
        clearSearchText.setOnClickListener {
            searchRelationInput.setText("")
            clearSearchText.invisible()
        }
        binding.recycler.apply {
            adapter = docRelationAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations))
                }
            )
        }
        binding.btnPlus.setOnClickListener {
            if (!isLocked) {
                RelationAddToObjectFragment.new(
                    ctx = ctx,
                    isSetOrCollection = isDataViewFLow
                ).showChildFragment()
            } else {
                toast(getString(R.string.unlock_your_object_to_add_new_relation))
            }
        }
        binding.btnEditOrDone.setOnClickListener { vm.onEditOrDoneClicked(isLocked) }
    }
    
    private fun execute(command: Command) {
        when (command) {
            is Command.EditTextRelationValue -> {
                val fr = RelationTextValueFragment.new(
                    ctx = ctx,
                    relationKey = command.relationKey,
                    objectId = command.target,
                    isLocked = command.isLocked,
                    flow = if (isDataViewFLow)
                        RelationTextValueFragment.FLOW_DATAVIEW
                    else
                        RelationTextValueFragment.FLOW_DEFAULT
                )
                fr.showChildFragment()
            }
            is Command.EditDateRelationValue -> {
                val fr = RelationDateValueFragment.new(
                    ctx = ctx,
                    relationKey = command.relationKey,
                    objectId = command.target
                )
                fr.showChildFragment()
            }
            is Command.EditRelationValue -> {
                if (isDataViewFLow) {
                    val fr = RelationValueDVFragment().apply {
                        arguments = bundleOf(
                            RelationValueBaseFragment.CTX_KEY to command.ctx,
                            RelationValueBaseFragment.TARGET_KEY to command.target,
                            RelationValueBaseFragment.RELATION_KEY to command.relationKey,
                            RelationValueBaseFragment.TARGET_TYPES_KEY to command.targetObjectTypes,
                            RelationValueBaseFragment.IS_LOCKED_KEY to false
                        )
                    }
                    fr.showChildFragment()
                } else {
                    val fr = RelationValueFragment.new(
                        ctx = ctx,
                        target = command.target,
                        relationKey = command.relationKey,
                        targetObjectTypes = command.targetObjectTypes,
                        isLocked = command.isLocked
                    )
                    fr.showChildFragment()
                }
            }
            is Command.SetRelationKey -> {
                withParent<OnFragmentInteractionListener> {
                    onSetRelationKeyClicked(
                        blockId = command.blockId,
                        key = command.key
                    )
                }
                dismiss()
            }
            is Command.EditStatusRelationValue -> {
                if (isDataViewFLow) {
                    val fr = RelationValueDVFragment().apply {
                        arguments = bundleOf(
                            RelationValueBaseFragment.CTX_KEY to command.ctx,
                            RelationValueBaseFragment.TARGET_KEY to command.target,
                            RelationValueBaseFragment.RELATION_KEY to command.relationKey,
                            RelationValueBaseFragment.TARGET_TYPES_KEY to command.targetObjectTypes,
                            RelationValueBaseFragment.IS_LOCKED_KEY to false
                        )
                    }
                    fr.showChildFragment()
                } else {
                    val fr = RelationStatusValueFragment.new(
                        ctx = ctx,
                        target = command.target,
                        relationKey = command.relationKey,
                        targetObjectTypes = command.targetObjectTypes,
                        isLocked = command.isLocked
                    )
                    fr.showChildFragment()
                }
            }
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.commands) { command -> execute(command) }
        jobs += lifecycleScope.subscribe(vm.toasts) { toast(it) }
        jobs += lifecycleScope.subscribe(vm.isEditMode) { isEditMode ->
            if (isEditMode) {
                binding.btnEditOrDone.setText(R.string.done)
                binding.btnPlus.invisible()
            } else {
                binding.btnPlus.visible()
                binding.btnEditOrDone.setText(R.string.edit)
            }
        }
        if (mode == MODE_ADD) {
            binding.searchBar.root.visible()
            val queries = searchRelationInput.textChanges()
                .onStart { emit(searchRelationInput.text.toString()) }
            val views = vm.views.combine(queries) { views, query ->
                if (views.isEmpty()) {
                    views
                } else {
                    views.filter { model ->
                        if (model is RelationListViewModel.Model.Item) {
                            model.view.name.contains(query, true)
                        } else {
                            true
                        }
                    }
                }
            }
            jobs += lifecycleScope.subscribe(searchRelationInput.textChanges()) {
                if (it.isEmpty()) clearSearchText.invisible() else clearSearchText.visible()
            }
            jobs += lifecycleScope.subscribe(views) { docRelationAdapter.update(it) }
        } else {
            binding.searchBar.root.gone()
            jobs += lifecycleScope.subscribe(vm.views) { docRelationAdapter.update(it) }
        }
        super.onStart()
        if (mode == MODE_LIST) {
            vm.onStartListMode(ctx)
        } else {
            vm.onStartAddMode(ctx)
        }
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onTextValueChanged(ctx: Id, text: String, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = text
        )
    }

    override fun onNumberValueChanged(ctx: Id, number: Double?, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = number
        )
    }

    override fun onDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationKey: Key
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = timeInSeconds
        )
    }

    override fun injectDependencies() {
        if (isDataViewFLow) {
            componentManager().dataViewRelationListComponent.get(ctx).inject(this)
        } else {
            componentManager().objectRelationListComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (isDataViewFLow) {
            componentManager().dataViewRelationListComponent.release(ctx)
        } else {
            componentManager().objectRelationListComponent.release(ctx)
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationListBinding = FragmentRelationListBinding.inflate(
        inflater, container, false
    )

    companion object {
        fun new(
            ctx: String,
            target: String?,
            mode: Int,
            locked: Boolean = false,
            isDataViewFlow: Boolean = false,
        ) = ObjectRelationListFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_TARGET to target,
                ARG_MODE to mode,
                ARG_LOCKED to locked,
                ARG_DATA_VIEW_FLOW to isDataViewFlow
            )
        }

        const val ARG_CTX = "arg.document-relation.ctx"
        const val ARG_MODE = "arg.document-relation.mode"
        const val ARG_TARGET = "arg.document-relation.target"
        const val ARG_LOCKED = "arg.document-relation.locked"
        const val MODE_ADD = 1
        const val MODE_LIST = 2
        const val ARG_DATA_VIEW_FLOW = "arg.document-relation.data-view-flow"
    }
}