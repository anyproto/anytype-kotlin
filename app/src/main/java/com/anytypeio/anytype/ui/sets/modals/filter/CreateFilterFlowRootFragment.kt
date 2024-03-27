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
    private val space: String get() = arg(SPACE_ID_KEY)
    private val viewer: String get() = arg(VIEWER_KEY)

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
                    viewer = viewer,
                    relation = step.relation,
                    space = space
                )
            }
            else -> {
                CreateFilterFromSelectedValueFragment.new(
                    ctx = step.ctx,
                    viewer = viewer,
                    relation = step.relation,
                    space = space,
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
        val fr = SelectFilterRelationFragment.new(ctx = ctx, viewerId = viewer)
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
        fun new(ctx: Id, space: Id, viewer: Id): CreateFilterFlowRootFragment = CreateFilterFlowRootFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                SPACE_ID_KEY to space,
                VIEWER_KEY to viewer
            )
        }

        const val TAG_ROOT = "tag.root"
        private const val CTX_KEY = "arg.create-filter-flow-root.ctx"
        private const val SPACE_ID_KEY = "arg.create-filter-flow-root.space-id"
        private const val VIEWER_KEY = "arg.create-filter-flow-root.viewer"
    }
}