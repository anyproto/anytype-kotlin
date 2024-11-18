package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class CreateSpaceFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateSpaceViewModel.Factory

    private val vm by viewModels<CreateSpaceViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                CreateSpaceScreen(
                    spaceIconView = vm.spaceIconView.collectAsState().value,
                    onCreate = vm::onCreateSpace,
                    onSpaceIconClicked = vm::onSpaceIconClicked,
                    isLoading = vm.isInProgress.collectAsState()
                )
                LaunchedEffect(Unit) { vm.toasts.collect { toast(it) } }
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) findNavController().popBackStack()
                    }
                }
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        when(command) {
                            is CreateSpaceViewModel.Command.SwitchSpace -> {
                                runCatching {
                                    findNavController().navigate(R.id.exitToVaultAction)
                                    findNavController().navigate(
                                        R.id.actionOpenSpaceFromVault,
                                        args = HomeScreenFragment.args(
                                            space = command.space.id,
                                            deeplink = null
                                        )
                                    )
                                    if (command.showMultiplayerTooltip) {
                                        findNavController().navigate(R.id.multiplayerFeatureDialog)
                                    }
                                }.onFailure {
                                    Timber.e(it, "Error while exiting to vault or opening created space")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun injectDependencies() {
        componentManager().createSpaceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createSpaceComponent.release()
    }
}