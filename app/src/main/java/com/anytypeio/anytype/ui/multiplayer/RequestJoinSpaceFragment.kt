package com.anytypeio.anytype.ui.multiplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_ui.features.multiplayer.JoinSpaceScreen
import com.anytypeio.anytype.core_ui.features.multiplayer.JoinSpaceWithoutApproveScreen
import com.anytypeio.anytype.core_ui.features.multiplayer.JoiningLoadingState
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.common.TypedViewState
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel.ErrorView
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.notifications.NotificationPermissionPromptDialog
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class RequestJoinSpaceFragment : BaseBottomSheetComposeFragment() {

    private val link get() = arg<Id>(ARG_LINK_KEY)

    @Inject
    lateinit var factory: RequestJoinSpaceViewModel.Factory

    private val vm by viewModels<RequestJoinSpaceViewModel> { factory }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val bottomSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                val scope = rememberCoroutineScope()

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    Timber.d("Permission granted: $isGranted")
                    activity?.onRequestPermissionsResult(
                        NotificationPermissionPromptDialog.REQUEST_CODE,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        if (isGranted)
                            intArrayOf(PackageManager.PERMISSION_GRANTED)
                        else
                            intArrayOf(PackageManager.PERMISSION_DENIED)
                    )
                    scope.launch {
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        vm.onNotificationPromptDismissed()
                    }
                }
                MaterialTheme(typography = typography) {
                    val showModal =
                        vm.showEnableNotificationDialog.collectAsStateWithLifecycle().value
                    val isLoadingInvite =
                        vm.showLoadingInviteProgress.collectAsStateWithLifecycle().value
                    when (val state = vm.state.collectAsStateWithLifecycle().value) {
                        is TypedViewState.Loading, is TypedViewState.Success -> {
                            val isLoading: Boolean
                            val spaceName: String
                            val createdByName: String
                            val withoutApprove: Boolean
                            if (state is TypedViewState.Loading) {
                                isLoading = true
                                spaceName = stringResource(R.string.three_dots_text_placeholder)
                                createdByName = stringResource(R.string.three_dots_text_placeholder)
                                withoutApprove = false
                            } else {
                                isLoading =
                                    vm.isRequestInProgress.collectAsStateWithLifecycle().value
                                with(state as TypedViewState.Success) {
                                    spaceName = state.data.spaceName
                                    createdByName = state.data.creatorName
                                    withoutApprove = state.data.withoutApprove
                                }
                            }
                            ModalBottomSheet(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                onDismissRequest = {
                                    if (isLoadingInvite) {
                                        vm.onCancelLoadingInviteClicked()
                                        safeDismiss()
                                    } else {
                                        safeDismiss()
                                    }
                                },
                                dragHandle = {},
                                containerColor = colorResource(id = R.color.background_secondary),
                                sheetState = bottomSheetState
                            ) {
                                if (isLoadingInvite) {
                                    JoiningLoadingState(
                                        onCancelLoadingInviteClicked = {
                                            vm.onCancelLoadingInviteClicked()
                                            safeDismiss()
                                        }
                                    )
                                } else if (!showModal) {
                                    if (withoutApprove) {
                                        JoinSpaceWithoutApproveScreen(
                                            isLoading = isLoading,
                                            onRequestJoinSpaceClicked = vm::onRequestToJoinClicked,
                                            onCancelClicked = {
                                                vm.onCancelJoinSpaceRequestClicked()
                                                safeDismiss()
                                            },
                                            spaceName = spaceName,
                                            createdByName = createdByName
                                        )
                                    } else {
                                        JoinSpaceScreen(
                                            isLoading = isLoading,
                                            onRequestJoinSpaceClicked = vm::onRequestToJoinClicked,
                                            spaceName = spaceName,
                                            createdByName = createdByName,
                                            onCancelClicked = {
                                                vm.onCancelJoinSpaceRequestClicked()
                                                safeDismiss()
                                            }
                                        )
                                    }
                                } else {
                                    Prompt(
                                        title = stringResource(R.string.notifications_prompt_get_notified),
                                        description = stringResource(R.string.notifications_prompt_description),
                                        primaryButtonText = stringResource(R.string.notifications_prompt_primary_button_text),
                                        secondaryButtonText = stringResource(R.string.notifications_prompt_secondary_button_text),
                                        onPrimaryButtonClicked = {
                                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        },
                                        onSecondaryButtonClicked = {
                                            scope.launch {
                                                bottomSheetState.hide()
                                            }.invokeOnCompletion {
                                                vm.onNotificationPromptDismissed()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        is TypedViewState.Error -> {
                            when (val err = state.error) {
                                is ErrorView.InviteNotFound -> {
                                    GenericAlert(
                                        config = AlertConfig.WithOneButton(
                                            title = stringResource(R.string.multiplayer_error_invite_not_found_title),
                                            description = stringResource(R.string.multiplayer_error_invite_not_found),
                                            firstButtonText = stringResource(id = R.string.button_okay),
                                            firstButtonType = BUTTON_SECONDARY,
                                            icon = R.drawable.ic_popup_lock_56
                                        ),
                                        onFirstButtonClicked = {
                                            safeDismiss()
                                        }
                                    )
                                }
                                is ErrorView.InvalidLink -> {
                                    GenericAlert(
                                        config = AlertConfig.WithOneButton(
                                            title = stringResource(R.string.multiplayer_error_invalid_link_title),
                                            description = stringResource(R.string.multiplayer_error_invite_bad_content),
                                            firstButtonText = stringResource(id = R.string.button_okay),
                                            firstButtonType = BUTTON_SECONDARY,
                                            icon = R.drawable.ic_popup_lock_56
                                        ),
                                        onFirstButtonClicked = {
                                            safeDismiss()
                                        }
                                    )
                                }
                                is ErrorView.SpaceDeleted -> {
                                    GenericAlert(
                                        config = AlertConfig.WithOneButton(
                                            title = stringResource(R.string.multiplayer_error_space_deleted_title),
                                            description = stringResource(R.string.multiplayer_error_space_is_deleted),
                                            firstButtonText = stringResource(id = R.string.button_okay),
                                            firstButtonType = BUTTON_SECONDARY,
                                            icon = R.drawable.ic_popup_lock_56
                                        ),
                                        onFirstButtonClicked = {
                                            safeDismiss()
                                        }
                                    )
                                }
                                is ErrorView.RequestAlreadySent -> {
                                    GenericAlert(
                                        config = AlertConfig.WithOneButton(
                                            title = stringResource(R.string.multiplayer_invite_link_request_already_sent),
                                            firstButtonText = stringResource(id = R.string.button_okay),
                                            firstButtonType = BUTTON_SECONDARY,
                                            description = EMPTY_STRING_VALUE,
                                            icon = R.drawable.ic_popup_feedback_56
                                        ),
                                        onFirstButtonClicked = {
                                            safeDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    LaunchedEffect(Unit) {
                        vm.toasts.collect { toast(it) }
                    }
                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            proceedWithCommand(command)
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithCommand(command: RequestJoinSpaceViewModel.Command) {
        when (command) {
            is RequestJoinSpaceViewModel.Command.Dismiss -> {
                safeDismiss()
            }
            is RequestJoinSpaceViewModel.Command.Toast.RequestSent -> {
                toast(getString(R.string.multiplayer_request_sent_toast))
            }
            is RequestJoinSpaceViewModel.Command.Toast.SpaceDeleted -> {
                toast(getString(R.string.multiplayer_error_space_deleted))
            }
            is RequestJoinSpaceViewModel.Command.Toast.SpaceNotFound -> {
                toast(getString(R.string.multiplayer_error_space_not_found))
            }
            is RequestJoinSpaceViewModel.Command.ShowGenericMultiplayerError -> {
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
            is RequestJoinSpaceViewModel.Command.SwitchToSpace -> {
                runCatching {
                    findNavController().popBackStack(R.id.vaultScreen, false)
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        WidgetsScreenFragment.args(
                            space = command.space.id,
                            deeplink = null
                        )
                    )
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().requestToJoinSpaceComponent.get(
            RequestJoinSpaceViewModel.Params(link = link)
        ).inject(fragment = this)
    }

    override fun releaseDependencies() {
        componentManager().requestToJoinSpaceComponent.release()
    }

    /**
     * Safe dismiss method that prevents IllegalStateException when fragment
     * is not in proper state for dismissal (e.g., after onSaveInstanceState)
     */
    private fun safeDismiss() {
        try {
            // Check if fragment is still attached and can safely perform transactions
            if (isAdded && !isStateSaved && !isRemoving) {
                dismiss()
            } else {
                Timber.d("Skipping dismiss - fragment not in valid state for dismissal")
            }
        } catch (e: IllegalStateException) {
            Timber.w(e, "Failed to dismiss fragment safely")
        }
    }

    companion object {
        fun args(link: String) = bundleOf(ARG_LINK_KEY to link)
        const val ARG_LINK_KEY = "arg.request-to-join-space.link"
    }
}