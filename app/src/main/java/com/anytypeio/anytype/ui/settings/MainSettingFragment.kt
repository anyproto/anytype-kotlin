package com.anytypeio.anytype.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel.Command
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel.Event
import com.anytypeio.anytype.ui.settings.system.SettingsActivity
import com.anytypeio.anytype.ui_settings.main.MainSettingScreen
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainSettingFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: MainSettingsViewModel.Factory

    @Inject
    lateinit var featureToggles: FeatureToggles

    private val vm by viewModels<MainSettingsViewModel> { factory }

    private val onAccountAndDataClicked = {
        vm.onOptionClicked(Event.OnAccountAndDataClicked)
    }

    private val onAboutAppClicked = {
        vm.onOptionClicked(Event.OnAboutClicked)
    }

    private val onPersonalizationClicked = {
        vm.onOptionClicked(Event.OnPersonalizationClicked)
    }

    private val onAppearanceClicked = {
        vm.onOptionClicked(Event.OnAppearanceClicked)
    }

    private val onDebugClicked = {
        vm.onOptionClicked(Event.OnDebugClicked)
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
                        onDebugClicked = onDebugClicked,
                        onPersonalizationClicked = onPersonalizationClicked,
                        showDebugMenu = featureToggles.isTroubleshootingMode
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command -> processCommands(command) }
            }
        }
    }

    private fun processCommands(command: Command) {
        when (command) {
            Command.OpenAboutScreen -> {
                safeNavigate(R.id.actionOpenAboutAppScreen)
            }
            Command.OpenAccountAndDataScreen -> {
                safeNavigate(R.id.actionOpenAccountAndDataScreen)
            }
            Command.OpenAppearanceScreen -> {
                safeNavigate(R.id.actionOpenAppearanceScreen)
            }
            Command.OpenPersonalizationScreen -> {
                safeNavigate(R.id.actionOpenPersonalizationScreen)
            }
            Command.OpenDebugScreen -> {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
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