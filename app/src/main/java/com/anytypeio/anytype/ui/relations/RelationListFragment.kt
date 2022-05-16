package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.DocumentRelationAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentRelationListBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.RelationListViewModel.Command
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

open class RelationListFragment : BaseBottomSheetFragment<FragmentRelationListBinding>(),
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
                RelationAddToObjectFragment.new(ctx).show(childFragmentManager, null)
            } else {
                toast(getString(R.string.unlock_your_object_to_add_new_relation))
            }
        }
        binding.btnEditOrDone.setOnClickListener { vm.onEditOrDoneClicked(isLocked) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
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
                subscribe(searchRelationInput.textChanges()) {
                    if (it.isEmpty()) clearSearchText.invisible() else clearSearchText.visible()
                }
                subscribe(views) { docRelationAdapter.update(it) }
            } else {
                binding.searchBar.root.gone()
                subscribe(vm.views) { docRelationAdapter.update(it) }
            }
            subscribe(vm.commands) { command -> execute(command) }
            subscribe(vm.toasts) { toast(it) }
            subscribe(vm.isEditMode) { isEditMode ->
                if (isEditMode) {
                    binding.btnEditOrDone.setText(R.string.done)
                    binding.btnPlus.invisible()
                } else {
                    binding.btnPlus.visible()
                    binding.btnEditOrDone.setText(R.string.edit)
                }
            }
        }
    }

    private fun execute(command: Command) {
        when (command) {
            is Command.EditTextRelationValue -> {
                val fr = RelationTextValueFragment.new(
                    ctx = ctx,
                    relationId = command.relation,
                    objectId = command.target,
                    isLocked = command.isLocked
                )
                fr.show(childFragmentManager, null)
            }
            is Command.EditDateRelationValue -> {
                val fr = RelationDateValueFragment.new(
                    ctx = ctx,
                    relationId = command.relation,
                    objectId = command.target
                )
                fr.show(childFragmentManager, null)
            }
            is Command.EditRelationValue -> {
                findNavController().navigate(
                    R.id.objectRelationValueScreen,
                    bundleOf(
                        RelationValueBaseFragment.CTX_KEY to command.ctx,
                        RelationValueBaseFragment.TARGET_KEY to command.target,
                        RelationValueBaseFragment.RELATION_KEY to command.relation,
                        RelationValueBaseFragment.TARGET_TYPES_KEY to command.targetObjectTypes,
                        RelationValueBaseFragment.IS_LOCKED_KEY to command.isLocked
                    )
                )
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
        }
    }

    override fun onStart() {
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

    override fun onTextValueChanged(ctx: Id, text: String, objectId: Id, relationId: Id) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = text,
            relationId = relationId
        )
    }

    override fun onNumberValueChanged(ctx: Id, number: Double?, objectId: Id, relationId: Id) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = number,
            relationId = relationId
        )
    }

    override fun onDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationId: Id
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationId = relationId,
            value = timeInSeconds
        )
    }

    override fun injectDependencies() {
        componentManager().documentRelationComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().documentRelationComponent.release(ctx)
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
            locked: Boolean = false
        ) = RelationListFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_TARGET to target,
                ARG_MODE to mode,
                ARG_LOCKED to locked
            )
        }

        const val ARG_CTX = "arg.document-relation.ctx"
        const val ARG_MODE = "arg.document-relation.mode"
        const val ARG_TARGET = "arg.document-relation.target"
        const val ARG_LOCKED = "arg.document-relation.locked"
        const val MODE_ADD = 1
        const val MODE_LIST = 2
    }
}