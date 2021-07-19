package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_ui.features.sets.CreateFilterAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment.DatePickerReceiver
import com.anytypeio.anytype.ui.sets.modals.PickFilterConditionFragment
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFromInputFieldValueFragment.Companion.FILTER_INDEX_EMPTY
import kotlinx.android.synthetic.main.fragment_create_or_update_filter.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

open class CreateFilterFromSelectedValueFragment :
    BaseFragment(R.layout.fragment_create_or_update_filter), UpdateConditionActionReceiver,
    DatePickerReceiver {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)

    @Inject
    lateinit var factory: FilterViewModel.Factory

    private val vm: FilterViewModel by viewModels { factory }

    private val createFilterAdapter by lazy {
        CreateFilterAdapter(
            onItemClicked = vm::onFilterItemClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBottomAction.setText(R.string.create)
        rvViewerFilterRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = createFilterAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_filter_list))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(tvFilterCondition.clicks()) {
                vm.onConditionClicked()
            }
            subscribe(btnBottomAction.clicks()) {
                vm.onCreateFilterFromSelectedValueClicked(ctx = ctx, relation = relation)
            }
            subscribe(vm.relationState.filterNotNull()) {
                tvRelationName.text = it.title
                ivRelationIcon.setImageResource(it.format.relationIcon(true))
            }
            subscribe(vm.optionCountState) { tvOptionCount.text = it.toString() }
            subscribe(vm.isCompleted) { isCompleted ->
                if (isCompleted) withParent<CreateFilterFlow> { onFilterCreated() }
            }
            subscribe(vm.conditionState) {
                tvFilterCondition.text = it?.condition?.title
            }
            val queries = searchRelationInput.textChanges()
                .onStart { emit(searchRelationInput.text.toString()) }
            val views =
                vm.filterValueListState.combine(queries) { views, query ->
                    if (views.isEmpty()) {
                        views
                    } else {
                        views.filter { it.text.contains(query, true) }
                    }
                }
            subscribe(views) { createFilterAdapter.update(it) }
            subscribe(vm.commands) { observeCommands(it) }
        }
        vm.onStart(relationId = relation, filterIndex = FILTER_INDEX_EMPTY)
    }

    private fun observeCommands(commands: FilterViewModel.Commands) {
        when (commands) {
            is FilterViewModel.Commands.OpenDatePicker -> {
                DatePickerFragment.new(commands.timeInSeconds)
                    .show(childFragmentManager, null)
            }
            is FilterViewModel.Commands.OpenConditionPicker -> {
                PickFilterConditionFragment.new(
                    ctx = ctx,
                    mode = PickFilterConditionFragment.MODE_CREATE,
                    type = commands.type,
                    index = commands.index
                ).show(childFragmentManager, null)
            }
            FilterViewModel.Commands.ShowCount -> tvOptionCount.visible()
            FilterViewModel.Commands.HideCount -> tvOptionCount.gone()
            FilterViewModel.Commands.ShowSearchbar -> searchBar.visible()
            FilterViewModel.Commands.HideSearchbar -> searchBar.gone()
            else -> {}
        }
    }

    override fun update(condition: Viewer.Filter.Condition) {
        vm.onConditionUpdate(condition)
    }

    override fun onPickDate(timeInSeconds: Long) {
        vm.onExactDayPicked(timeInSeconds)
    }

    override fun injectDependencies() {
        componentManager().createFilterComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createFilterComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, relation: Id): CreateFilterFromSelectedValueFragment = CreateFilterFromSelectedValueFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, RELATION_KEY to relation)
        }

        const val CTX_KEY = "arg.create-filter-relation.ctx"
        const val RELATION_KEY = "arg.create-filter-relation.relation"
    }
}