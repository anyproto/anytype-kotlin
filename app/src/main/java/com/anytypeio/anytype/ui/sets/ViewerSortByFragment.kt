package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.dataview.modals.SortByAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentSortingBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ViewerSortByCommand
import com.anytypeio.anytype.presentation.sets.ViewerSortByViewModel
import com.anytypeio.anytype.presentation.sets.ViewerSortByViewState
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.PickSortingKeyFragment
import com.anytypeio.anytype.ui.sets.modals.PickSortingTypeFragment
import com.anytypeio.anytype.ui.sets.modals.ViewerBottomSheetRootFragment
import javax.inject.Inject

class ViewerSortByFragment : BaseBottomSheetFragment<FragmentSortingBinding>() {

    private val sortingAdapter by lazy {
        SortByAdapter(click = vm::itemClicked)
    }

    private val ctx get() = argString(CONTEXT_ID_KEY)
    private val viewer get() = argString(VIEWER_ID_KEY)

    @Inject
    lateinit var factory: ViewerSortByViewModel.Factory
    private val vm: ViewerSortByViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerView) {
            adapter = sortingAdapter
        }
        lifecycleScope.subscribe(vm.viewState) { observeState(it) }
        lifecycleScope.subscribe(vm.commands.stream()) { observeCommands(it) }
        lifecycleScope.subscribe(binding.ivBack.clicks()) { vm.onBackClicked() }
        vm.onViewCreated(viewer)
    }

    fun onPickSortType(key: String, type: Viewer.SortType) {
        vm.onPickSortType(key, type)
    }

    fun onReplaceSortKey(keySelected: String, keyNew: String) {
        vm.onReplaceSortKey(keySelected = keySelected, keyNew = keyNew)
    }

    fun onAddSortKey(key: String) {
        vm.onAddSortKey(key)
    }

    private fun observeState(state: ViewerSortByViewState) {
        when (state) {
            ViewerSortByViewState.Init -> {
            }
            is ViewerSortByViewState.Success -> {
                sortingAdapter.update(state.items)
            }
        }
    }

    private fun observeCommands(command: ViewerSortByCommand) {
        when (command) {
            is ViewerSortByCommand.Modal.ShowSortingKeyList -> {
                PickSortingKeyFragment.new(
                    selected = command.old,
                    relations = command.relations,
                    sorts = command.sortingExpression
                ).show(childFragmentManager, null)
            }
            is ViewerSortByCommand.Modal.ShowSortingTypeList -> {
                PickSortingTypeFragment.new(
                    key = command.key,
                    type = command.selected
                )
                    .show(childFragmentManager, null)
            }
            is ViewerSortByCommand.Apply -> dispatchResultAndDismiss(command)
            is ViewerSortByCommand.BackToCustomize -> exitToCustomizeScreen()
        }
    }

    private fun dispatchResultAndDismiss(command: ViewerSortByCommand.Apply) {
        withParent<ViewerBottomSheetRootFragment> {
            dispatchResultSortsAndDismiss(command.sorts)
        }
    }

    private fun exitToCustomizeScreen() {
        withParent<ViewerBottomSheetRootFragment> {
            transitToCustomize()
        }
    }

    override fun injectDependencies() {
        componentManager().viewerSortByComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerSortByComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSortingBinding = FragmentSortingBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CONTEXT_ID_KEY = "arg.viewer.sorts.context"
        const val VIEWER_ID_KEY = "arg.viewer.sorts.viewer_id"

        fun new(ctx: Id, viewer: Id) = ViewerSortByFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx,
                VIEWER_ID_KEY to viewer
            )
        }
    }
}