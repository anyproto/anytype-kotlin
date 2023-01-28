package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentViewerBottomSheetRootBinding
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterFlowViewModel
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterFlowViewModel.Step
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

class CreateFilterFlowRootFragment :
    BaseBottomSheetFragment<FragmentViewerBottomSheetRootBinding>(), CreateFilterFlow {

    private val ctx: String get() = arg(CTX_KEY)

    val vm by lazy { CreateFilterFlowViewModel() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.subscribe(vm.step) { step ->
            when (step) {
                is Step.SelectRelation -> transitToSelection()
                is Step.CreateFilter -> transitToCreation(step)
            }
        }
    }

    override fun onRelationSelected(ctx: Id, relation: SimpleRelationView) {
        vm.onRelationSelected(ctx = ctx, relation = relation.key, format = relation.format)
    }

    override fun onFilterCreated() {
        dismiss()
    }

    private fun transitToCreation(step: Step.CreateFilter) {
        val fr = when (step.type) {
            Step.CreateFilter.Type.INPUT_FIELD -> {
                CreateFilterFromInputFieldValueFragment.new(
                    ctx = step.ctx,
                    relation = step.relation
                )
            }
            else -> {
                CreateFilterFromSelectedValueFragment.new(
                    ctx = step.ctx,
                    relation = step.relation
                )
            }
        }
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.filter_slide_up, R.anim.fade_out)
            .replace(R.id.container, fr)
            .addToBackStack(fr.javaClass.name)
            .commit()
    }

    private fun transitToSelection() {
        val fr = SelectFilterRelationFragment.new(ctx)
        childFragmentManager
            .beginTransaction()
            .add(R.id.container, fr)
            .addToBackStack(TAG_ROOT)
            .commit()
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentViewerBottomSheetRootBinding = FragmentViewerBottomSheetRootBinding.inflate(
        inflater, container, false
    )

    companion object {
        fun new(ctx: Id): CreateFilterFlowRootFragment = CreateFilterFlowRootFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val TAG_ROOT = "tag.root"
        private const val CTX_KEY = "arg.create-filter-flow-root.ctx"
    }
}