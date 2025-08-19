package com.anytypeio.anytype.ui.multiplayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.multiplayer.ShareSpaceScreen
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceErrors
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel.Command
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import javax.inject.Inject
import timber.log.Timber

class ShareSpaceFragment : BaseBottomSheetComposeFragment() {

    private val space get() = arg<String>(SPACE_ID_KEY)

    @Inject
    lateinit var factory: ShareSpaceViewModel.Factory

    private val vm by viewModels<ShareSpaceViewModel> { factory }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ShareSpaceScreen(
                    isCurrentUserOwner = vm.isCurrentUserOwner.collectAsStateWithLifecycle().value,
                    onShareInviteLinkClicked = vm::onShareInviteLinkClicked,
                    members = vm.members.collectAsStateWithLifecycle().value,
                    onViewRequestClicked = vm::onViewRequestClicked,
                    onCanEditClicked = vm::onCanEditClicked,
                    onCanViewClicked = vm::onCanViewClicked,
                    onRemoveMemberClicked = vm::onRemoveMemberClicked,
                    onShareQrCodeClicked = vm::onShareQrCodeClicked,
                    incentiveState = vm.showIncentive.collectAsStateWithLifecycle().value,
                    onIncentiveClicked = vm::onIncentiveClicked,
                    isLoadingInProgress = vm.isLoadingInProgress.collectAsStateWithLifecycle().value,
                    onMemberClicked = vm::onMemberClicked,
                    inviteLinkAccessLevel = vm.inviteLinkAccessLevel.collectAsStateWithLifecycle().value,
                    inviteLinkAccessLoading = vm.inviteLinkAccessLoading.collectAsStateWithLifecycle().value,
                    confirmationDialogLevel = vm.inviteLinkConfirmationDialog.collectAsStateWithLifecycle().value,
                    onInviteLinkAccessLevelSelected = vm::onInviteLinkAccessLevelSelected,
                    onInviteLinkAccessChangeConfirmed = vm::onInviteLinkAccessChangeConfirmed,
                    onInviteLinkAccessChangeCancel = vm::onInviteLinkAccessChangeCancel,
                    onCopyInviteLinkClicked = vm::onCopyInviteLinkClicked
                )
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        proceedWithCommand(command)
                    }
                }
                LaunchedEffect(Unit) {
                    vm.toasts.collect { toast(it) }
                }
                val errors = vm.shareSpaceErrors.collectAsStateWithLifecycle().value
                when (errors) {
                    is ShareSpaceErrors.Error -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_generic, errors.msg),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
                    }
                    ShareSpaceErrors.Hidden -> {
                        // Hidden errors should not show a dialog
                    }
                    ShareSpaceErrors.LimitReached -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_limit_reached),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
                    }
                    ShareSpaceErrors.NotShareable -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_not_shareable),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
                    }
                    ShareSpaceErrors.RequestFailed -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_request_failed),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
                    }
                    ShareSpaceErrors.SpaceIsDeleted -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_space_deleted),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
                    }

                    ShareSpaceErrors.IncorrectPermissions -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_incorrect_permissions),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
                    }
                    ShareSpaceErrors.NoSuchSpace -> {
                        BaseAlertDialog(
                            dialogText = stringResource(R.string.share_space_error_no_such_space),
                            buttonText = stringResource(R.string.button_ok),
                            onButtonClick = { vm.dismissShareSpaceErrors() },
                            onDismissRequest = { vm.dismissShareSpaceErrors() }
                        )
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

    private fun proceedWithCommand(command: Command) {
        when (command) {
            is Command.ShareInviteLink -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, command.link)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, null))
            }
            is Command.ShareQrCode -> {
                runCatching {
                    findNavController().navigate(
                        resId = R.id.shareSpaceInviteQrCodeScreen,
                        args = ShareQrCodeSpaceInviteFragment.args(
                            link = command.link
                        )
                    )
                }.onFailure {
                    Timber.d(it, "Error while navigation")
                }
            }
            is Command.ViewJoinRequest -> {
                runCatching {
                    findNavController().navigate(
                        resId = R.id.spaceJoinRequestScreen,
                        args = SpaceJoinRequestFragment.args(
                            space = command.space,
                            member = command.member,
                            analyticsRoute = EventsDictionary.Routes.settings
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
            is Command.ShowHowToShareSpace -> {
                runCatching {
                    findNavController().navigate(R.id.howToShareSpaceScreen)
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
            is Command.ShowRemoveMemberWarning -> {
                runCatching {
                    val dialog = RemoveMemberWarning.new(name = command.name)
                    dialog.onAccepted = {
                        vm.onRemoveMemberAccepted(command.identity).also {
                            dialog.dismiss()
                        }
                    }
                    dialog.show(childFragmentManager, null)
                }.onFailure {
                    Timber.e(it, "Error while showing remove member warning")
                }
            }
            is Command.Dismiss -> {
                dismiss()
            }
            is Command.ToastPermission -> {
                val msg = getString(R.string.multiplayer_toast_permission_not_allowed)
                toast(msg)
            }
            is Command.ShowMembershipScreen -> {
                runCatching {
                    findNavController().navigate(R.id.paymentsScreen)
                }.onFailure {
                    Timber.e(it, "Error while navigation: $command")
                }
            }
            is Command.ShowMembershipUpgradeScreen -> {
                runCatching {
                    findNavController().navigate(R.id.membershipUpdateScreen)
                }.onFailure {
                    Timber.e(it, "Error while navigation: $command")
                }
            }
            is Command.ShowMultiplayerError -> {
                when(command.error) {
                    is MultiplayerError.Generic.LimitReached -> {
                        toast(resources.getString(R.string.multiplayer_error_limit_reached))
                    }
                    is MultiplayerError.Generic.NotShareable -> {
                        toast(resources.getString(R.string.multiplayer_error_not_shareable))
                    }
                    is MultiplayerError.Generic.RequestFailed -> {
                        toast(resources.getString(R.string.multiplayer_error_request_failed))
                    }
                    is MultiplayerError.Generic.SpaceIsDeleted -> {
                        toast(resources.getString(R.string.multiplayer_error_space_is_deleted))
                    }
                    is MultiplayerError.Generic.IncorrectPermissions -> {
                        toast(resources.getString(R.string.share_space_error_incorrect_permissions))
                    }
                    is MultiplayerError.Generic.NoSuchSpace -> {
                        toast(resources.getString(R.string.share_space_error_no_such_space))
                    }
                }
            }
            is Command.OpenParticipantObject -> {
                runCatching {
                    findNavController().navigate(
                        R.id.participantScreen,
                        ParticipantFragment.args(
                            space = command.space.id,
                            objectId = command.objectId
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation: $command")
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().shareSpaceComponent.get(SpaceId(space)).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().shareSpaceComponent.release()
    }

    companion object {
        const val SPACE_ID_KEY = "arg.share-space.space-id-key"
        fun args(space: SpaceId): Bundle = bundleOf(
            SPACE_ID_KEY to space.id
        )
    }
}