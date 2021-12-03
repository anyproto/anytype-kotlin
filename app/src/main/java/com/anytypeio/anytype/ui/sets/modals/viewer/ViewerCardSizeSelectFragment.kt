package com.anytypeio.anytype.ui.sets.modals.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.viewer.ViewerCardSizeSelectViewModel
import kotlinx.android.synthetic.main.fragment_viewer_card_size_select.*
import javax.inject.Inject

class ViewerCardSizeSelectFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ViewerCardSizeSelectViewModel.Factory
    private val vm: ViewerCardSizeSelectViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CTX_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewer_card_size_select, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCardSmall.setOnClickListener {
            vm.onSmallCardClicked(ctx)
        }
        btnCardLarge.setOnClickListener {
            vm.onLargeCardClicked(ctx)
        }
        with(lifecycleScope) {
            subscribe(vm.state) { state ->
                when(state) {
                    ViewerCardSizeSelectViewModel.STATE_DISMISSED -> dismiss()
                    ViewerCardSizeSelectViewModel.STATE_LARGE_CARD_SELECTED -> {
                        smallCardCheckbox.invisible()
                        largeCardCheckbox.visible()
                    }
                    ViewerCardSizeSelectViewModel.STATE_SMALL_CARD_SELECTED -> {
                        smallCardCheckbox.visible()
                        largeCardCheckbox.invisible()
                    }
                    ViewerCardSizeSelectViewModel.STATE_IDLE -> {
                        smallCardCheckbox.invisible()
                        largeCardCheckbox.invisible()
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

    companion object {
        const val CTX_KEY = "arg.viewer-card-size-select.ctx"
    }
}