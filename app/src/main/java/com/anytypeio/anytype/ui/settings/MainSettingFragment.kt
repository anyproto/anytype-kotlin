package com.anytypeio.anytype.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel.Command
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel.Event
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase.Companion.ARG_CONTEXT_ID_KEY
import com.anytypeio.anytype.ui.sets.ARG_SHOW_REMOVE_BUTTON
import com.anytypeio.anytype.ui.settings.system.SettingsActivity
import com.anytypeio.anytype.ui_settings.main.MainSettingScreen
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private val onSpaceImageClicked = {
        vm.onOptionClicked(Event.OnSpaceImageClicked)
    }

    private val onNameSet = { name: String ->
        vm.onNameSet(name)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(),
        dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                MainSettingScreen(
                    workspace = vm.workspaceAndAccount.collectAsStateWithLifecycle().value,
                    onAccountAndDataClicked = onAccountAndDataClicked,
                    onAboutAppClicked = onAboutAppClicked,
                    onAppearanceClicked = onAppearanceClicked,
                    onDebugClicked = onDebugClicked,
                    onPersonalizationClicked = onPersonalizationClicked,
                    showDebugMenu = featureToggles.isTroubleshootingMode,
                    onSpaceIconClick = onSpaceImageClicked,
                    onNameSet = onNameSet
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val offsetFromTop = PADDING_TOP
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            expandedOffset = offsetFromTop
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command -> processCommands(command) }
            }
        }
    }

    private fun processCommands(command: Command) {
        when (command) {
            is Command.OpenAboutScreen -> {
                safeNavigate(R.id.actionOpenAboutAppScreen)
            }
            is Command.OpenAccountAndDataScreen -> {
                safeNavigate(R.id.actionOpenAccountAndDataScreen)
            }
            is Command.OpenAppearanceScreen -> {
                safeNavigate(R.id.actionOpenAppearanceScreen)
            }
            is Command.OpenPersonalizationScreen -> {
                safeNavigate(R.id.actionOpenPersonalizationScreen)
            }
            is Command.OpenDebugScreen -> {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
            }
            is Command.OpenSpaceImageSet -> {
                safeNavigate(
                    R.id.actionOpenImagePickerScreen, bundleOf(
                        ARG_CONTEXT_ID_KEY to command.id,
                        ARG_SHOW_REMOVE_BUTTON to false
                    )
                )
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

private const val PADDING_TOP = 54