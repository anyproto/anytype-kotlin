package com.anytypeio.anytype.ui.vault

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavOptions.Builder
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.openAppSettings
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.vault.VaultCommand
import com.anytypeio.anytype.presentation.vault.VaultErrors
import com.anytypeio.anytype.presentation.vault.VaultNavigation
import com.anytypeio.anytype.presentation.vault.VaultUiState.Companion.MAX_PINNED_SPACES
import com.anytypeio.anytype.presentation.vault.VaultViewModel
import com.anytypeio.anytype.presentation.vault.VaultViewModelFactory
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.multiplayer.LeaveSpaceWarning
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.payments.MembershipFragment
import com.anytypeio.anytype.ui.settings.space.SpaceSettingsFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment.Companion.ARG_SPACE_TYPE
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment.Companion.TYPE_CHAT
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment.Companion.TYPE_SPACE
import com.anytypeio.anytype.ui.spaces.DeleteSpaceWarning
import com.google.zxing.integration.android.IntentIntegrator
import javax.inject.Inject
import timber.log.Timber

class VaultFragment : BaseComposeFragment() {

    private val deepLink: String? get() = argOrNull(DEEP_LINK_KEY)

    @Inject
    lateinit var factory: VaultViewModelFactory

    private val vm by viewModels<VaultViewModel> { factory }

    private val qrCodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val r = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (r != null && r.contents != null) {
            vm.onQrCodeScanned(qrCode = r.contents)
        } else {
            if (r == null) {
                Timber.w("QR code scan cancelled by user")
            } else {
                Timber.w("QR code scan failed: no contents found")
            }
            vm.onQrScannerError()
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchQrScanner()
        } else {
            vm.onShowCameraPermissionSettingsDialog()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                val onMuteSpace: (String) -> Unit = { spaceTargetId ->
                    vm.setSpaceNotificationState(spaceTargetId, NotificationState.MENTIONS)
                }
                val onUnmuteSpace: (String) -> Unit = { spaceTargetId ->
                    vm.setSpaceNotificationState(spaceTargetId, NotificationState.ALL)
                }

                VaultScreen(
                    uiState = vm.uiState.collectAsStateWithLifecycle().value,
                    showNotificationBadge = vm.isNotificationDisabled.collectAsStateWithLifecycle().value,
                    onSpaceClicked = vm::onSpaceClicked,
                    onCreateSpaceClicked = vm::onChooseSpaceTypeClicked,
                    onSettingsClicked = vm::onSettingsClicked,
                    profile = vm.profileView.collectAsStateWithLifecycle().value,
                    onMuteSpace = onMuteSpace,
                    onUnmuteSpace = onUnmuteSpace,
                    onPinSpace = vm::onPinSpaceClicked,
                    onUnpinSpace = vm::onUnpinSpaceClicked,
                    onOrderChanged = vm::onOrderChanged,
                    onDragEnd = vm::onDragEnd,
                    onSpaceSettings = vm::onSpaceSettingsClicked
                )
                val notificationError = vm.notificationError.collectAsStateWithLifecycle().value
                if (notificationError != null) {
                    BaseAlertDialog(
                        dialogText = notificationError,
                        buttonText = getString(R.string.button_ok),
                        onButtonClick = { vm.clearNotificationError() },
                        onDismissRequest = { vm.clearNotificationError() }
                    )
                }

                val vaultErrors = vm.vaultErrors.collectAsStateWithLifecycle().value
                when (vaultErrors) {
                    VaultErrors.MaxPinnedSpacesReached -> {
                        BaseAlertDialog(
                            dialogText = stringResource(
                                R.string.vault_max_pinned_limit_reached,
                                MAX_PINNED_SPACES
                            ),
                            buttonText = getString(R.string.button_ok),
                            onButtonClick = { vm.clearVaultError() },
                            onDismissRequest = { vm.clearVaultError() }
                        )
                    }

                    VaultErrors.Hidden -> {
                        //do nothing
                    }

                    VaultErrors.QrCodeIsNotValid -> {
                        AlertScreenModals(
                            title = getString(R.string.vault_qr_invalid_title),
                            description = getString(R.string.vault_qr_invalid_description),
                            firstButtonText = getString(R.string.vault_qr_try_again),
                            onAction = vm::onModalTryAgainClicked,
                            onDismiss = vm::onModalCancelClicked
                        )
                    }

                    VaultErrors.QrScannerError -> {
                        AlertScreenModals(
                            title = getString(R.string.vault_qr_scan_error_title),
                            description = getString(R.string.vault_qr_scan_error_description),
                            firstButtonText = getString(R.string.vault_qr_try_again),
                            onAction = vm::onModalTryAgainClicked,
                            onDismiss = vm::onModalCancelClicked
                        )
                    }
                    
                    VaultErrors.CameraPermissionDenied -> {
                        AlertScreenModals(
                            title = getString(R.string.camera_permission_required_title),
                            description = getString(R.string.camera_permission_settings_message),
                            firstButtonText = getString(R.string.open_settings),
                            secondButtonText = getString(R.string.cancel),
                            onAction = {
                                requireContext().openAppSettings()
                                vm.clearVaultError()
                            },
                            onDismiss = vm::clearVaultError
                        )
                    }
                }

                if (vm.showChooseSpaceType.collectAsStateWithLifecycle().value) {
                    ChooseSpaceTypeScreen(
                        onCreateChatClicked = {
                            vm.onCreateChatClicked()
                        },
                        onCreateSpaceClicked = {
                            vm.onCreateSpaceClicked()
                        },
                        onJoinViaQrClicked = {
                            vm.onJoinViaQrClicked()
                        },
                        onDismiss = {
                            vm.onChooseSpaceTypeDismissed()
                        }
                    )
                }
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command -> proceed(command) }
            }
            LaunchedEffect(Unit) {
                vm.navigations.collect { command -> proceed(command) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    private fun proceed(command: VaultCommand) {
        when (command) {
            is VaultCommand.EnterSpaceHomeScreen -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        HomeScreenFragment.args(
                            space = command.space.id,
                            deeplink = null
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening space from vault")
                }
            }

            is VaultCommand.EnterSpaceLevelChat -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenChatFromVault,
                        ChatFragment.args(
                            space = command.space.id,
                            ctx = command.chat
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening space-level chat from vault")
                }
            }

            is VaultCommand.CreateNewSpace -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionCreateSpaceFromVault,
                        bundleOf(ARG_SPACE_TYPE to TYPE_SPACE)
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening create space screen from vault")
                }
            }

            VaultCommand.CreateChat -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionCreateChatFromVault,
                        bundleOf(ARG_SPACE_TYPE to TYPE_CHAT)
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening create chat screen from vault")
                }
            }

            is VaultCommand.OpenProfileSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.profileSettingsScreen,
                        null
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening profile settings from vault")
                }
            }

            is VaultCommand.Deeplink.Invite -> {
                findNavController().navigate(
                    R.id.requestJoinSpaceScreen,
                    RequestJoinSpaceFragment.args(link = command.link)
                )
            }

            is VaultCommand.Deeplink.GalleryInstallation -> {
                findNavController().navigate(
                    R.id.galleryInstallationScreen,
                    GalleryInstallationFragment.args(
                        deepLinkType = command.deepLinkType,
                        deepLinkSource = command.deepLinkSource
                    )
                )
            }

            is VaultCommand.Deeplink.MembershipScreen -> {
                findNavController().navigate(
                    R.id.paymentsScreen,
                    MembershipFragment.args(command.tierId),
                    Builder().setLaunchSingleTop(true).build()
                )
            }

            is VaultCommand.Deeplink.DeepLinkToObjectNotWorking -> {
                toast(
                    getString(R.string.multiplayer_deeplink_to_your_object_error)
                )
            }

            is VaultCommand.ShowDeleteSpaceWarning -> {
                val fragment = DeleteSpaceWarning().apply {
                    arguments = DeleteSpaceWarning.args(command.space)
                }
                fragment.onDeletionAccepted = {
                    fragment.dismiss()
                    vm.onDeleteSpaceAcceptedClicked(it)
                }
                fragment.onDeletionCancelled = {
                    fragment.dismiss()
                    vm.onDeleteSpaceWarningCancelled()
                }
                fragment.show(childFragmentManager, null)
            }

            is VaultCommand.ShowLeaveSpaceWarning -> {
                val fragment = LeaveSpaceWarning().apply {
                    arguments = LeaveSpaceWarning.args(command.space)
                }
                fragment.onLeaveSpaceAccepted = {
                    fragment.dismiss()
                    vm.onLeaveSpaceAcceptedClicked(it)
                }
                fragment.onLeaveSpaceCancelled = {
                    fragment.dismiss()
                    vm.onLeaveSpaceWarningCancelled()
                }
                fragment.show(childFragmentManager, null)
            }

            is VaultCommand.OpenSpaceSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.action_open_space_settings,
                        SpaceSettingsFragment.args(space = command.space)
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening space settings")
                }
            }
            
            VaultCommand.ScanQrCode -> {
                handleCameraPermissionAndScan()
            }
            
            is VaultCommand.NavigateToRequestJoinSpace -> {
                runCatching {
                    findNavController().navigate(
                        R.id.requestJoinSpaceScreen,
                        RequestJoinSpaceFragment.args(link = command.link)
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigating to request join space")
                }
            }
        }
    }

    private fun proceed(destination: VaultNavigation) {
        when (destination) {
            is VaultNavigation.OpenObject -> runCatching {
                findNavController().navigate(
                    R.id.actionOpenSpaceFromVault,
                    HomeScreenFragment.args(
                        space = destination.space,
                        deeplink = null
                    )
                )
                navigation().openDocument(
                    target = destination.ctx,
                    space = destination.space
                )
            }.onFailure {
                Timber.e(it, "Error while opening object from vault")
            }

            is VaultNavigation.OpenSet -> runCatching {
                findNavController().navigate(
                    R.id.actionOpenSpaceFromVault,
                    HomeScreenFragment.args(
                        space = destination.space,
                        deeplink = null
                    )
                )
                navigation().openObjectSet(
                    target = destination.ctx,
                    space = destination.space,
                    view = destination.view
                )
            }.onFailure {
                Timber.e(it, "Error while opening set or collection from vault")
            }

            is VaultNavigation.OpenChat -> {
                findNavController().navigate(
                    R.id.actionOpenSpaceFromVault,
                    HomeScreenFragment.args(
                        space = destination.space,
                        deeplink = null
                    )
                )
                navigation().openChat(
                    target = destination.ctx,
                    space = destination.space
                )
            }

            is VaultNavigation.OpenDateObject -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        HomeScreenFragment.args(
                            space = destination.space,
                            deeplink = null
                        )
                    )
                    navigation().openDateObject(
                        objectId = destination.ctx,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening date object from widgets")
                }
            }

            is VaultNavigation.OpenParticipant -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        HomeScreenFragment.args(
                            space = destination.space,
                            deeplink = null
                        )
                    )
                    navigation().openParticipantObject(
                        objectId = destination.ctx,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening participant object from widgets")
                }
            }

            is VaultNavigation.OpenType -> {
                Timber.e("Illegal command: type cannot be opened from vault")
            }

            is VaultNavigation.OpenUrl -> {
                try {
                    ActivityCustomTabsHelper.openUrl(
                        activity = requireActivity(),
                        url = destination.url
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Error opening bookmark URL: ${destination.url}")
                    toast("Failed to open URL")
                }
            }

            is VaultNavigation.ShowError -> {
                toast(destination.message)
            }
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    override fun onResume() {
        super.onResume()
        proceedWithDeepLinks()
        vm.processPendingDeeplink()
    }

    private fun proceedWithDeepLinks() {
        val deepLinkFromFragmentArgs = deepLink
        if (deepLinkFromFragmentArgs != null) {
            Timber.d("Deeplink  from fragment args")
            vm.onResume(DefaultDeepLinkResolver.resolve(deepLinkFromFragmentArgs))
            arguments?.putString(DEEP_LINK_KEY, null)
        } else {
            vm.onResume(null)
        }
    }

    override fun injectDependencies() {
        componentManager().vaultComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().vaultComponent.release()
    }

    private fun handleCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchQrScanner()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun launchQrScanner() {
        qrCodeLauncher.launch(
            IntentIntegrator
                .forSupportFragment(this)
                .setBeepEnabled(false)
                .createScanIntent()
        )
    }

    companion object {
        private const val SHOW_MNEMONIC_KEY = "arg.vault-screen.show-mnemonic"
        private const val DEEP_LINK_KEY = "arg.vault-screen.deep-link"
        fun args(deeplink: String?): Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink
        )
    }
}