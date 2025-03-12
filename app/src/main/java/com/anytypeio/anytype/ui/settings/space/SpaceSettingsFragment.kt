package com.anytypeio.anytype.ui.settings.space

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel.Command
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.ui.multiplayer.LeaveSpaceWarning
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.DeleteSpaceWarning
import com.anytypeio.anytype.ui_settings.space.new_settings.NewSpaceSettingsScreen
import com.anytypeio.anytype.ui_settings.space.new_settings.SpaceSettingsContainer
import java.io.File
import javax.inject.Inject
import timber.log.Timber

class SpaceSettingsFragment : BaseBottomSheetComposeFragment() {

    private val space get() = arg<Id>(ARG_SPACE_ID_KEY)

    @Inject
    lateinit var factory: SpaceSettingsViewModel.Factory

    @Inject
    lateinit var uriFileProvider: UriFileProvider

    private val vm by viewModels<SpaceSettingsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(), dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography,
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.context_menu_background)
                )
            ) {
                SpaceSettingsContainer(
                    uiState = vm.uiState.collectAsStateWithLifecycle().value,
                    uiEvent = vm::onUiEvent
                )
                LaunchedEffect(Unit) { vm.toasts.collect { toast(it) } }
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) dismiss()
                    }
                }
                LaunchedEffect(Unit) {
                    observeCommands()
                }
            }
        }
    }

    private suspend fun observeCommands() {
        vm.commands.collect { command ->
            when (command) {
                is Command.ShareSpaceDebug -> {
                    try {
                        shareFile(
                            uriFileProvider.getUriForFile(File(command.filepath))
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error while sharing space debug").also {
                            toast("Error while sharing space debug. Please try again later.")
                        }
                    }
                }
                is Command.ManageSharedSpace -> {
                    runCatching {
                        findNavController()
                            .navigate(
                                R.id.shareSpaceScreen,
                                ShareSpaceFragment.args(command.space)
                            )
                    }.onFailure {
                        Timber.e(it, "Error while opening share-space screen")
                    }
                }
                is Command.SharePrivateSpace -> {
                    runCatching {
                        findNavController()
                            .navigate(
                                R.id.shareSpaceScreen,
                                ShareSpaceFragment.args(command.space)
                            )
                    }.onFailure {
                        Timber.e(it, "Error while opening share-space screen")
                    }
                }
                is Command.ShowDeleteSpaceWarning -> {
                    val dialog = DeleteSpaceWarning.new()
                    dialog.onDeletionAccepted = {
                        dialog.dismiss()
                        vm.onDeleteSpaceAcceptedClicked()
                    }
                    dialog.onDeletionCancelled = {
                        vm.onDeleteSpaceWarningCancelled()
                    }
                    dialog.show(childFragmentManager, null)
                }
                is Command.ShowLeaveSpaceWarning -> {
                    val dialog = LeaveSpaceWarning.new()
                    dialog.onLeaveSpaceAccepted = {
                        dialog.dismiss()
                        vm.onLeaveSpaceAcceptedClicked()
                    }
                    dialog.onLeaveSpaceCancelled = {
                        vm.onDeleteSpaceWarningCancelled()
                    }
                    dialog.show(childFragmentManager, null)
                }
                is Command.ShowShareLimitReachedError -> {
                    toast(getString(R.string.multiplayer_toast_share_limit_reached))
                }
                Command.NavigateToMembership -> {
                    findNavController().navigate(R.id.paymentsScreen)
                }
                Command.NavigateToMembershipUpdate -> {
                    findNavController().navigate(R.id.membershipUpdateScreen)
                }
                is Command.ExitToVault -> {
                    runCatching {
                        findNavController()
                            .popBackStack(R.id.vaultScreen, false)
                    }.onFailure {
                        Timber.e(it, "Error while exiting to vault screen from space settings")
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expand()
        skipCollapsed()
    }

    override fun injectDependencies() {
        val vmParams = SpaceSettingsViewModel.VmParams(space = SpaceId(space))
        componentManager().spaceSettingsComponent.get(params = vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceSettingsComponent.release()
    }

    companion object {
        const val ARG_SPACE_ID_KEY = "arg.space-settings.space-id"
        fun args(space: SpaceId) = bundleOf(ARG_SPACE_ID_KEY to space.id)
    }
}