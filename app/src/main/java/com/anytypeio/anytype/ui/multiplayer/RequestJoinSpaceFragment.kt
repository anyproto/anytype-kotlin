package com.anytypeio.anytype.ui.multiplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_ui.features.multiplayer.JoinSpaceScreen
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.Announcement
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.Prompt
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.common.TypedViewState
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel.ErrorView
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
                val bottomSheetState = rememberModalBottomSheetState()
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
                    val showModal = vm.showEnableNotificationDialog.collectAsStateWithLifecycle().value
                    when(val state = vm.state.collectAsStateWithLifecycle().value) {
                        is TypedViewState.Loading, is TypedViewState.Success -> {
                            val isLoading: Boolean
                            val spaceName: String
                            val createdByName: String
                            if (state is TypedViewState.Loading) {
                                isLoading = true
                                spaceName = stringResource(R.string.three_dots_text_placeholder)
                                createdByName = stringResource(R.string.three_dots_text_placeholder)
                            }
                            else {
                                isLoading = vm.isRequestInProgress.collectAsStateWithLifecycle().value
                                with(state as TypedViewState.Success) {
                                    spaceName = state.data.spaceName
                                    createdByName = state.data.creatorName
                                }
                            }
                            if (!showModal) {
                                JoinSpaceScreen(
                                    isLoading = isLoading,
                                    onRequestJoinSpaceClicked = vm::onRequestToJoinClicked,
                                    spaceName = spaceName,
                                    createdByName = createdByName
                                )
                            } else {
                                ModalBottomSheet(
                                    onDismissRequest = {
                                        vm.onNotificationPromptDismissed()
                                    },
                                    dragHandle = {},
                                    containerColor = colorResource(id = R.color.background_secondary),
                                    sheetState = bottomSheetState
                                ) {
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
                            when(val err = state.error) {
                                is ErrorView.AlreadySpaceMember -> {
                                    Announcement(
                                        title = stringResource(id = R.string.multiplayer_already_space_member),
                                        subtitle = EMPTY_STRING_VALUE,
                                        actionButtonText = stringResource(id = R.string.multiplayer_open_space),
                                        cancelButtonText = stringResource(id = R.string.cancel),
                                        onLeftClicked = {
                                            dismiss()
                                        },
                                        onRightClicked = {
                                            vm.onOpenSpaceClicked(err.space)
                                        }
                                    )
                                }
                                is ErrorView.InvalidLink -> {
                                    GenericAlert(
                                        config = AlertConfig.WithOneButton(
                                            title = "This link does not seem to work",
                                            firstButtonText = stringResource(id = R.string.button_okay),
                                            firstButtonType = BUTTON_SECONDARY,
                                            description = EMPTY_STRING_VALUE,
                                            icon = AlertConfig.Icon(
                                                gradient = GRADIENT_TYPE_BLUE,
                                                icon = R.drawable.ic_alert_message
                                            )
                                        ),
                                        onFirstButtonClicked = {
                                            dismiss()
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
            RequestJoinSpaceViewModel.Command.Dismiss -> {
                dismiss()
            }
            RequestJoinSpaceViewModel.Command.Toast.RequestSent -> {
                toast(getString(R.string.multiplayer_request_sent_toast))
            }
            RequestJoinSpaceViewModel.Command.Toast.SpaceDeleted -> {
                toast(getString(R.string.multiplayer_error_space_deleted))
            }
            RequestJoinSpaceViewModel.Command.Toast.SpaceNotFound -> {
                toast(getString(R.string.multiplayer_error_space_not_found))
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

    companion object {
        fun args(link: String) = bundleOf(ARG_LINK_KEY to link)
        const val ARG_LINK_KEY = "arg.request-to-join-space.link"
    }
}