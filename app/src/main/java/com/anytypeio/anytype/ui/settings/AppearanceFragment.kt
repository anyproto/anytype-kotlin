package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui_settings.appearance.AppearanceScreen
import com.anytypeio.anytype.ui_settings.appearance.AppearanceViewModel
import javax.inject.Inject

class AppearanceFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: AppearanceViewModel.Factory

    private val vm by viewModels<AppearanceViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    AppearanceScreen(
                        light = { throttle { vm.onLight() } },
                        dark = { throttle { vm.onDark() } },
                        system = { throttle { vm.onSystem() } },
                        selectedMode = vm.selectedTheme.collectAsState().value
                    )
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().appearanceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().appearanceComponent.release()
    }
}