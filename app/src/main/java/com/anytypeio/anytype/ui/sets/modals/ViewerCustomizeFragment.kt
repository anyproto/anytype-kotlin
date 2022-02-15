package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentViewerCustomizeBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ViewerCustomizeViewModel
import com.anytypeio.anytype.presentation.sets.ViewerCustomizeViewState
import javax.inject.Inject

class ViewerCustomizeFragment : BaseBottomSheetFragment<FragmentViewerCustomizeBinding>() {

    private val ctx get() = argString(CONTEXT_ID_KEY)
    private val viewer get() = argString(VIEWER_ID_KEY)

    @Inject
    lateinit var factory: ViewerCustomizeViewModel.Factory
    private val vm: ViewerCustomizeViewModel by viewModels { factory }

    lateinit var filtersCount: TextView
    lateinit var sortsCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filtersCount = view.findViewById(R.id.filterCount)
        sortsCount = view.findViewById(R.id.sortsCount)
        lifecycleScope.subscribe(vm.viewState) {
            observeViewState(it)
        }
        binding.itemFilter.root.setOnClickListener {
            withParent<ViewerBottomSheetRootFragment> { transitToFilter() }
        }
        binding.itemSort.root.setOnClickListener {
            withParent<ViewerBottomSheetRootFragment> { transitToSorting() }
        }
        binding.itemRelations.root.setOnClickListener {
            withParent<ViewerBottomSheetRootFragment> { transitToRelations() }
        }
        vm.onViewCreated(viewerId = viewer)
    }

    private fun observeViewState(viewState: ViewerCustomizeViewState) {
        when (viewState) {
            ViewerCustomizeViewState.Init -> {
            }
            is ViewerCustomizeViewState.InitGrid -> {
                binding.itemTable.root.show()
                if (viewState.isShowFilterSize) {
                    filtersCount.text = viewState.filterSize
                } else {
                    filtersCount.invisible()
                }
                if (viewState.isShowSortsSize) {
                    sortsCount.text = viewState.sortsSize
                } else {
                    sortsCount.invisible()
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().viewerCustomizeComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerCustomizeComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentViewerCustomizeBinding = FragmentViewerCustomizeBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CONTEXT_ID_KEY = "arg.viewer.customize.context"
        const val VIEWER_ID_KEY = "arg.viewer.customize.viewer_id"

        fun new(ctx: Id, viewer: Id) = ViewerCustomizeFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx,
                VIEWER_ID_KEY to viewer
            )
        }
    }
}