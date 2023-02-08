package com.anytypeio.anytype.ui.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.widgets.SelectWidgetTypeViewModel
import javax.inject.Inject

class SelectWidgetTypeFragment : BaseBottomSheetComposeFragment() {

    private val vm by viewModels<SelectWidgetTypeViewModel> { factory }

    @Inject
    lateinit var factory: SelectWidgetTypeViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            SelectWidgetTypeScreen(
                views = emptyList(),
                onViewClicked = {}
            )
        }
    }

    override fun injectDependencies() {
        componentManager().selectWidgetTypeSubcomponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectWidgetTypeSubcomponent.release()
    }
}