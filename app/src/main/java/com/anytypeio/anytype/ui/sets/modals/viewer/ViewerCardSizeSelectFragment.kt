package com.anytypeio.anytype.ui.sets.modals.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentViewerCardSizeSelectBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.viewer.ViewerCardSizeSelectViewModel
import javax.inject.Inject

class ViewerCardSizeSelectFragment : BaseBottomSheetFragment<FragmentViewerCardSizeSelectBinding>() {

    @Inject
    lateinit var factory: ViewerCardSizeSelectViewModel.Factory
    private val vm: ViewerCardSizeSelectViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CTX_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCardSmall.setOnClickListener {
            vm.onSmallCardClicked(ctx)
        }
        binding.btnCardLarge.setOnClickListener {
            vm.onLargeCardClicked(ctx)
        }
        with(lifecycleScope) {
            subscribe(vm.state) { state ->
                when (state) {
                    ViewerCardSizeSelectViewModel.STATE_DISMISSED -> dismiss()
                    ViewerCardSizeSelectViewModel.STATE_LARGE_CARD_SELECTED -> {
                        with(binding) {
                            smallCardCheckbox.invisible()
                            largeCardCheckbox.visible()
                        }
                    }
                    ViewerCardSizeSelectViewModel.STATE_SMALL_CARD_SELECTED -> {
                        with(binding) {
                            smallCardCheckbox.visible()
                            largeCardCheckbox.invisible()
                        }
                    }
                    ViewerCardSizeSelectViewModel.STATE_IDLE -> {
                        with(binding) {
                            smallCardCheckbox.invisible()
                            largeCardCheckbox.invisible()
                        }
                    }
                    else -> toast("Unexpected state: $state")
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().viewerCardSizeSelectComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerCardSizeSelectComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentViewerCardSizeSelectBinding = FragmentViewerCardSizeSelectBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.viewer-card-size-select.ctx"
    }
}