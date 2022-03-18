package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.ui_settings.main.MainSettingScreen
import javax.inject.Inject

class MainSettingFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: MainSettingsViewModel.Factory

    private val vm by viewModels<MainSettingsViewModel> { factory }

    private val onAccountAndDataClicked = {
        vm.onAccountAndDataClicked()
        findNavController().navigate(R.id.actionOpenAccountAndDataScreen)
    }

    private val onAboutAppClicked = {
        vm.onAboutClicked()
        findNavController().navigate(R.id.actionOpenAboutAppScreen)
    }

    private val onPersonalizationClicked = {
        vm.onPersonalizationClicked()
        findNavController().navigate(R.id.actionOpenPersonalizationScreen)
    }

    private val onAppearanceClicked = {
        vm.onAppearanceClicked()
        findNavController().navigate(R.id.actionOpenAppearanceScreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    MainSettingScreen(
                        onAccountAndDataClicked = onAccountAndDataClicked,
                        onAboutAppClicked = onAboutAppClicked,
                        onAppearanceClicked = onAppearanceClicked,
                        onPersonalizationClicked = onPersonalizationClicked
                    )
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().mainSettingsComponent.get().inject(this)
    }
    override fun releaseDependencies() {
        componentManager().mainSettingsComponent.release()
    }
}