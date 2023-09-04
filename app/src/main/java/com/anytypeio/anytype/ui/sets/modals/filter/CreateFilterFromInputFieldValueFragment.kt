package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.anytypeio.anytype.databinding.FragmentCreateOrUpdateFilterInputFieldValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.PickFilterConditionFragment
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class CreateFilterFromInputFieldValueFragment :
    BaseFragment<FragmentCreateOrUpdateFilterInputFieldValueBinding>(R.layout.fragment_create_or_update_filter_input_field_value),
    UpdateConditionActionReceiver {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)
    private val viewer: String get() = arg(VIEWER_KEY)

    @Inject
    lateinit var factory: FilterViewModel.Factory
    private val vm: FilterViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBottomAction.setText(R.string.create)
        with(lifecycleScope) {
            subscribe(binding.btnBottomAction.clicks()) {
                vm.onCreateInputValueFilterClicked(
                    ctx = ctx,
                    viewerId = viewer,
                    relation = relation,
                    input = binding.enterTextValueInputField.text.toString()
                )
            }
            subscribe(binding.tvFilterCondition.clicks()) {
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
                ).showChildFragment()
            }
            FilterViewModel.Commands.HideInput -> {
                binding.enterTextValueInputField.gone()
            }
            FilterViewModel.Commands.ShowInput -> {
                binding.enterTextValueInputField.visible()
            }
            else -> {
            }
        }
    }

    override fun onStart() {
        setupJobs()
        super.onStart()
        vm.onStart(
            viewerId = viewer,
            relationKey = relation,
            filterIndex = FILTER_INDEX_EMPTY
        )
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun setupJobs() {
        jobs += lifecycleScope.subscribe(vm.relationState.filterNotNull()) {
            binding.enterTextValueInputField.setInputTypeBaseOnFormat(it.format)
            binding.tvRelationName.text = it.title
            binding.ivRelationIcon.setImageResource(it.format.relationIcon(true))
        }
        jobs += lifecycleScope.subscribe(vm.isCompleted) {
            if (it) withParent<CreateFilterFlow> { onFilterCreated() }
        }
        jobs += lifecycleScope.subscribe(vm.conditionState) {
            binding.tvFilterCondition.text = it?.condition?.title
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

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateOrUpdateFilterInputFieldValueBinding = FragmentCreateOrUpdateFilterInputFieldValueBinding.inflate(
        inflater, container, false
    )

    companion object {
        fun new(ctx: Id, relation: Id, viewer: Id) = CreateFilterFromInputFieldValueFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, RELATION_KEY to relation, VIEWER_KEY to viewer)
        }

        private const val CTX_KEY = "arg.create-filter-relation.ctx"
        private const val RELATION_KEY = "arg.create-filter-relation.relation"
        private const val VIEWER_KEY = "arg.create-filter-relation.viewer"
        val FILTER_INDEX_EMPTY: Int? = null
    }
}