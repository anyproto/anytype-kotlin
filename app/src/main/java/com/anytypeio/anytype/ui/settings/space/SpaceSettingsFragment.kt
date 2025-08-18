package com.anytypeio.anytype.ui.settings.space

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel.Command
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.ui.multiplayer.LeaveSpaceWarning
import com.anytypeio.anytype.ui.multiplayer.ShareQrCodeSpaceInviteFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.AppDefaultObjectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.primitives.SpacePropertiesFragment
import com.anytypeio.anytype.ui.primitives.SpaceTypesFragment
import com.anytypeio.anytype.ui.settings.SpacesStorageFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.DeleteSpaceWarning
import com.anytypeio.anytype.ui.widgets.collection.CollectionFragment
import com.anytypeio.anytype.ui_settings.space.new_settings.SpaceSettingsContainer
import com.anytypeio.anytype.feature_chats.ui.NotificationPermissionContent
import com.anytypeio.anytype.ui.settings.DebugFragment
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSettingsFragment : BaseComposeFragment(), ObjectTypeSelectionListener {

    private val space get() = arg<Id>(ARG_SPACE_ID_KEY)

    @Inject
    lateinit var factory: SpaceSettingsViewModel.Factory

    @Inject
    lateinit var uriFileProvider: UriFileProvider

    private val vm by viewModels<SpaceSettingsViewModel> { factory }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = content {
        val showNotificationPermissionDialog = remember { mutableStateOf(false) }
        val notificationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Timber.d("Notification permission granted: $isGranted")
            if (isGranted) {
                vm.onNotificationPermissionGranted()
            } else {
                vm.onNotificationPermissionDenied()
            }
        }

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
                    if (isDismissed) findNavController().popBackStack()
                }
            }
            LaunchedEffect(Unit) {
                observeCommands(showNotificationPermissionDialog)
            }

            // Notification Permission Modal
            if (showNotificationPermissionDialog.value) {
                ModalBottomSheet(
                    onDismissRequest = { 
                        showNotificationPermissionDialog.value = false
                        vm.onNotificationPermissionDismissed()
                    },
                    sheetState = notificationSheetState,
                    containerColor = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = null
                ) {
                    NotificationPermissionContent(
                        onCancelClicked = { 
                            scope.launch {
                                notificationSheetState.hide()
                            }.invokeOnCompletion {
                                showNotificationPermissionDialog.value = false
                                vm.onNotificationPermissionDismissed()
                            }
                        },
                        onEnableNotifications = {
                            vm.onNotificationPermissionRequested()
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            scope.launch {
                                notificationSheetState.hide()
                            }.invokeOnCompletion {
                                showNotificationPermissionDialog.value = false
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun observeCommands(showNotificationPermissionDialog: MutableState<Boolean>) {
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
                is Command.OpenWallpaperPicker -> {
                    runCatching {
                        findNavController().navigate(R.id.wallpaperSetScreen)
                    }.onFailure {
                        Timber.e(it, "Error while opening space wallpaper picker")
                    }
                }
                is Command.SelectDefaultObjectType -> {
                   runCatching {
                       AppDefaultObjectTypeFragment.newInstance(
                           excludeTypes = command.excludedTypeIds
                       ).showChildFragment()
                   }.onFailure {
                       Timber.e(it, "Error while opening set-default-object-type screen")
                   }
                }
                is Command.ManageRemoteStorage -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.spacesStorageScreen,
                            args = SpacesStorageFragment.args(space = space)
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to execute nav command: $command")
                    }
                }
                is Command.ManageBin -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.homeScreenWidgets,
                            CollectionFragment.args(
                                subscription = Subscriptions.SUBSCRIPTION_BIN,
                                space = space
                            )
                        )
                    }.onFailure {
                        Timber.w(it, "Error while opening bin from widgets")
                    }
                }
                is Command.ShowInviteLinkQrCode -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.shareSpaceInviteQrCodeScreen,
                            ShareQrCodeSpaceInviteFragment.args(link = command.link)
                        )
                    }.onFailure {
                        Timber.w(it, "Error while showing invite QR code from space settings")
                    }
                }

                is Command.OpenPropertiesScreen -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.spacePropertiesScreen,
                            args = SpacePropertiesFragment.args(space = space)
                        )
                    }.onFailure {
                        Timber.e(it, "Error while opening space properties screen")
                    }
                }
                is Command.OpenTypesScreen -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.spaceTypesScreen,
                            args = SpaceTypesFragment.args(space = space)
                        )
                    }.onFailure {
                        Timber.e(it, "Error while opening space types screen")
                    }
                }
                
                is Command.OpenDebugScreen -> {
                    runCatching {
                        val debugFragment = DebugFragment.newInstance(command.spaceId)
                        debugFragment.show(childFragmentManager, DebugFragment::class.simpleName)
                    }.onFailure {
                        Timber.e(it, "Error while opening debug screen")
                    }
                }

                Command.RequestNotificationPermission -> {
                    showNotificationPermissionDialog.value = true
                }

                is Command.ShareInviteLink -> {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, command.link)
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(intent, null))
                }
            }
        }
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onSelectObjectType(objType)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun injectDependencies() {
        val vmParams = SpaceSettingsViewModel.VmParams(space = SpaceId(space))
        componentManager().spaceSettingsComponent.get(params = vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceSettingsComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do nothing. Compose code will handle insets.
    }

    companion object {
        const val ARG_SPACE_ID_KEY = "arg.space-settings.space-id"
        fun args(space: SpaceId) = bundleOf(ARG_SPACE_ID_KEY to space.id)
    }
}