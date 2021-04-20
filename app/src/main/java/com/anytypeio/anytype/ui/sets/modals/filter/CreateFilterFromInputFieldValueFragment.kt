package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_ui.extensions.setInputTypeBaseOnFormat
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.PickFilterConditionFragment
import kotlinx.android.synthetic.main.fragment_create_or_update_filter.btnBottomAction
import kotlinx.android.synthetic.main.fragment_create_or_update_filter.ivRelationIcon
import kotlinx.android.synthetic.main.fragment_create_or_update_filter.tvRelationName
import kotlinx.android.synthetic.main.fragment_create_or_update_filter_input_field_value.*
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class CreateFilterFromInputFieldValueFragment :
    BaseFragment(R.layout.fragment_create_or_update_filter_input_field_value), UpdateConditionActionReceiver {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)

    @Inject
    lateinit var factory: FilterViewModel.Factory
    private val vm: FilterViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBottomAction.setText(R.string.create)
        with(lifecycleScope) {
            subscribe(btnBottomAction.clicks()) {
                vm.onCreateInputValueFilterClicked(
                    ctx = ctx,
                    relation = relation,
                    input = enterTextValueInputField.text.toString()
                )
            }
            subscribe(tvFilterCondition.clicks()) {
                vm.onConditionClicked()
            }
        }
    }

    private fun observeCommands(commands: FilterViewModel.Commands) {
        when (commands) {
            is FilterViewModel.Commands.OpenConditionPicker -> {
                PickFilterConditionFragment.new(
                    ctx = ctx,
                    mode = PickFilterConditionFragment.MODE_CREATE,
                    type = commands.type,
                    index = commands.index
                ).show(childFragmentManager, null)
            }
            FilterViewModel.Commands.HideInput -> {
                enterTextValueInputField.gone()
            }
            FilterViewModel.Commands.ShowInput -> {
                enterTextValueInputField.visible()
            }
            else -> {
            }
        }
    }

    override fun onStart() {
        setupJobs()
        super.onStart()
        vm.onStart(relation, FILTER_INDEX_EMPTY)
    }

    private fun setupJobs() {
        jobs += lifecycleScope.subscribe(vm.relationState.filterNotNull()) {
            enterTextValueInputField.setInputTypeBaseOnFormat(it.format)
            tvRelationName.text = it.title
            ivRelationIcon.setImageResource(it.format.relationIcon(true))
        }
        jobs += lifecycleScope.subscribe(vm.isCompleted) {
            if (it) withParent<CreateFilterFlow> { onFilterCreated() }
        }
        jobs += lifecycleScope.subscribe(vm.conditionState) {
            tvFilterCondition.text = it?.condition?.title
        }
        jobs += lifecycleScope.subscribe(vm.commands) {
            observeCommands(it)
        }
    }

    override fun update(condition: Viewer.Filter.Condition) {
        vm.onConditionUpdate(condition)
    }

    override fun injectDependencies() {
        componentManager().createFilterComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createFilterComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, relation: Id) = CreateFilterFromInputFieldValueFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, RELATION_KEY to relation)
        }

        private const val CTX_KEY = "arg.create-filter-relation.ctx"
        private const val RELATION_KEY = "arg.create-filter-relation.relation"
        val FILTER_INDEX_EMPTY: Int? = null
    }
}