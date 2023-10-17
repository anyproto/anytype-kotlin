package com.anytypeio.anytype.ui.sets

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
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.dataview.modals.FilterByAdapter
import com.anytypeio.anytype.core_ui.layout.DividerVerticalItemDecoration
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentFilterBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterCommand
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFlowRootFragment
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromInputFieldValueFragment
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromSelectedValueFragment
import javax.inject.Inject

open class ViewerFilterFragment : BaseBottomSheetFragment<FragmentFilterBinding>() {

    private val ctx get() = argString(CONTEXT_ID_KEY)
    private val viewer get() = argString(VIEWER_ID_KEY)

    private val filterAdapter by lazy {
        FilterByAdapter(
            click = { click -> vm.onFilterClicked(ctx = ctx, viewerId = viewer, click = click) }
        )
    }

    @Inject
    lateinit var factory: ViewerFilterViewModel.Factory
    private val vm: ViewerFilterViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = filterAdapter
        with(lifecycleScope) {
            subscribe(vm.commands) { observeCommands(it) }
            subscribe(binding.addButton.clicks()) { vm.onAddNewFilterClicked(viewerId = viewer) }
            subscribe(binding.doneBtn.clicks()) { vm.onDoneButtonClicked() }
            subscribe(binding.editBtn.clicks()) { vm.onEditButtonClicked() }
            subscribe(vm.views) { filterAdapter.update(it) }
            subscribe(vm.screenState) { render(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(viewerId = viewer)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun render(state: ViewerFilterViewModel.ScreenState) {
        when (state) {
            ViewerFilterViewModel.ScreenState.LIST -> {
                removeDivider()
                with(binding) {
                    editBtn.visible()
                    addButton.visible()
                    doneBtn.invisible()
                    txtEmptyState.gone()
                    recycler.addItemDecoration(
                        DividerVerticalItemDecoration(
                            divider = requireContext().drawable(R.drawable.divider_filter_list),
                            isShowInLastItem = false
                        ),
                        0
                    )
                }
            }
            ViewerFilterViewModel.ScreenState.EDIT -> {
                removeDivider()
                with(binding) {
                    doneBtn.visible()
                    editBtn.invisible()
                    addButton.invisible()
                    txtEmptyState.gone()
                    recycler.addItemDecoration(
                        DividerVerticalItemDecoration(
                            divider = requireContext().drawable(R.drawable.divider_filter_edit),
                            isShowInLastItem = false
                        ),
                        0
                    )
                }
            }
            ViewerFilterViewModel.ScreenState.EMPTY -> {
                removeDivider()
                with(binding) {
                    doneBtn.invisible()
                    editBtn.invisible()
                    addButton.visible()
                    txtEmptyState.visible()
                }
            }
        }
    }

    private fun removeDivider() {
        if (binding.recycler.itemDecorationCount > 0) binding.recycler.removeItemDecorationAt(0)
    }

    private fun observeCommands(command: ViewerFilterCommand) {
        when (command) {
            is ViewerFilterCommand.Modal.ShowRelationList -> {
                val fr = CreateFilterFlowRootFragment.new(ctx = ctx, viewer = viewer)
                fr.show(parentFragmentManager, null)
            }
            is ViewerFilterCommand.Modal.UpdateInputValueFilter -> {
                val fr = ModifyFilterFromInputFieldValueFragment.new(
                    ctx = ctx,
                    relation = command.relation,
                    index = command.filterIndex,
                    viewer = viewer
                )
                fr.showChildFragment(fr.javaClass.canonicalName)
            }
            is ViewerFilterCommand.Modal.UpdateSelectValueFilter -> {
                val fr = ModifyFilterFromSelectedValueFragment.new(
                    ctx = ctx,
                    relation = command.relation,
                    index = command.filterIndex,
                    viewer = viewer
                )
                fr.showChildFragment(fr.javaClass.canonicalName)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().viewerFilterComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerFilterComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFilterBinding = FragmentFilterBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CONTEXT_ID_KEY = "arg.viewer.filters.context"
        const val VIEWER_ID_KEY = "arg.viewer.filters.viewer"

        fun new(ctx: Id, viewer: Id) = ViewerFilterFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, VIEWER_ID_KEY to viewer)
        }
    }
}