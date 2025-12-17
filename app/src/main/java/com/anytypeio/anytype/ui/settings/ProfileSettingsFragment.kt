package com.anytypeio.anytype.ui.settings

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.features.profile.ProfileQrCodeScreen
import com.anytypeio.anytype.core_utils.ext.GetImageContract
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.openNotificationSettings
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.device.launchMediaPicker
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.MediaPermissionHelper
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import com.anytypeio.anytype.ui.qrcode.QrScannerActivity
import com.anytypeio.anytype.ui_settings.account.AnyIdInfoSheet
import com.anytypeio.anytype.ui_settings.account.NotificationSettingsScreen
import com.anytypeio.anytype.ui_settings.account.ProfileSettingsScreen
import com.anytypeio.anytype.ui_settings.account.ProfileSettingsViewModel
import javax.inject.Inject
import timber.log.Timber

class ProfileSettingsFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ProfileSettingsViewModel.Factory

    @Inject
    lateinit var toggles: FeatureToggles

    private val vm by viewModels<ProfileSettingsViewModel> { factory }

    private val onKeychainPhraseClicked = {
        val bundle =
            bundleOf(KeychainPhraseDialog.ARG_SCREEN_TYPE to EventsDictionary.Type.screenSettings)
        safeNavigate(R.id.keychainDialog, bundle)
    }

    private val onLogoutClicked = {
        safeNavigate(R.id.logoutWarningScreen)
    }

    private lateinit var permissionHelper: MediaPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHelper = MediaPermissionHelper(
            fragment = this,
            onPermissionDenied = { toast(R.string.permission_read_denied) },
            onPermissionSuccess = { _, _ -> openGallery() }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(
            context = requireContext(),
            dialog = requireDialog()
        ).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    var showNotificationSettingsModal by remember { mutableStateOf(false) }
                    var showAnyIdInfoSheet by remember { mutableStateOf(false) }
                    val notificationsDisabled = vm.notificationsDisabled.collectAsStateWithLifecycle().value
                    val accountProfile = vm.profileData.collectAsStateWithLifecycle().value
                    ProfileSettingsScreen(
                        onKeychainPhraseClicked = onKeychainPhraseClicked,
                        onLogoutClicked = onLogoutClicked,
                        isLogoutInProgress = vm.isLoggingOut.collectAsState().value,
                        onNameChange = { vm.onNameChange(it) },
                        onProfileIconClick = { proceedWithIconClick() },
                        account = accountProfile,
                        onAppearanceClicked = throttledClick(
                            onClick = {
                                findNavController().navigate(R.id.appearanceScreen)
                            }
                        ),
                        onDataManagementClicked = throttledClick(
                            onClick = {
                                findNavController().navigate(R.id.filesStorageScreen)
                            }
                        ),
                        onAboutClicked = throttledClick(
                            onClick = {
                                findNavController().navigate(R.id.aboutAppScreen)
                            }
                        ),
                        onMembershipClicked = throttledClick(
                            onClick = {
                                findNavController().navigate(R.id.paymentsScreen)
                            }
                        ),
                        membershipStatus = vm.membershipStatusState.collectAsStateWithLifecycle().value,
                        showMembership = vm.showMembershipState.collectAsStateWithLifecycle().value,
                        onSpacesClicked = throttledClick(
                            onClick = {
                                runCatching {
                                    findNavController().navigate(R.id.spaceListScreen)
                                }
                            }
                        ),
                        clearProfileImage = { vm.onClearProfileImage() },
                        onDebugClicked = {
                            runCatching {
                                findNavController().navigate(R.id.debugScreen)
                            }
                        },
                        isDebugEnabled = vm.isDebugEnabled.collectAsStateWithLifecycle().value,
                        onMiscSectionClicked = vm::onMiscSectionClicked,
                        notificationsDisabled = notificationsDisabled,
                        onOpenNotificationSettings = { showNotificationSettingsModal = true },
                        onMySitesClicked = {
                            runCatching {
                                findNavController().navigate(R.id.mySitesScreen)
                            }
                        },
                        onIdentityClicked = {
                            val account = accountProfile
                            if (account is AccountProfile.Data && account.globalName.isNullOrEmpty()) {
                                showAnyIdInfoSheet = true
                            } else {
                                findNavController().navigate(R.id.paymentsScreen)
                            }
                        },
                        onShowQrCodeClicked = { vm.onShowQrCodeClicked() }
                    )
                    if (showNotificationSettingsModal) {
                        NotificationSettingsScreen(
                            isDisabled = notificationsDisabled,
                            onDismiss = {
                                showNotificationSettingsModal = false
                            },
                            onOpenSettings = {
                                showNotificationSettingsModal = false
                                requireContext().openNotificationSettings()
                            }
                        )
                    }
                    if (showAnyIdInfoSheet) {
                        AnyIdInfoSheet(
                            onExplorePlans = {
                                showAnyIdInfoSheet = false
                                findNavController().navigate(R.id.paymentsScreen)
                            },
                            onDismiss = {
                                showAnyIdInfoSheet = false
                            }
                        )
                    }
                    val profileQrCodeState =
                        vm.profileQrCodeState.collectAsStateWithLifecycle().value
                    ProfileQrCodeScreen(
                        state = profileQrCodeState,
                        onShare = { link -> shareProfileLink(link) },
                        onCopyLink = { link -> copyProfileLink(link) },
                        onScanQrCode = { launchQrScanner() },
                        onDismiss = { vm.onHideQrCodeScreen() }
                    )
                }
            }
        }
    }

    private fun shareProfileLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun copyProfileLink(link: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Anytype Profile Link", link)
        clipboard.setPrimaryClip(clip)
        toast(R.string.profile_qr_link_copied)
    }

    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val qrCode = result.data?.getStringExtra(QrScannerActivity.SCAN_RESULT)
            if (qrCode != null) {
                vm.onQrCodeScanned(qrCode)
            }
        }
    }

    private fun launchQrScanner() {
        val intent = Intent(requireContext(), QrScannerActivity::class.java)
        qrScannerLauncher.launch(intent)
    }

    private fun handleScannedQrCode(qrCode: String) {
        // Start the activity with the scanned QR code as a deep link
        // This will trigger the MainActivity's onNewIntent handling
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(qrCode)).apply {
            setClass(requireContext(), requireActivity()::class.java)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun onStart() {
        super.onStart()
        subscribe(vm.toasts) {
            toast(it)
        }
        jobs += lifecycleScope.subscribe(vm.debugSyncReportUri) { uri ->
            if (uri != null) {
                shareFile(uri)
            }
        }
        jobs += lifecycleScope.subscribe(vm.commands) { command ->
            when (command) {
                is ProfileSettingsViewModel.Command.ShareLink -> {
                    shareProfileLink(command.link)
                }

                is ProfileSettingsViewModel.Command.ProcessScannedQrCode -> {
                    // Dismiss the bottom sheet and let the activity handle the deep link
                    dismiss()
                    handleScannedQrCode(command.qrCode)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        jobs.cancel()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshPermissionState()
    }

    private fun proceedWithIconClick() {
        launchMediaPicker(
            pickMedia = pickMedia,
            permissionHelper = permissionHelper,
            mediaType = PickVisualMedia.ImageOnly,
            fallbackMimeType = Mimetype.MIME_IMAGE_ALL
        )
    }

    private fun openGallery() {
        getContent.launch(SELECT_IMAGE_CODE)
    }

    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                val path = uri.parseImagePath(requireContext())
                vm.onPickedImageFromDevice(path = path)
            } catch (e: Exception) {
                toast("Error while parsing path for media file")
                Timber.e(e, "Error while parsing path for cover image")
            }
        } else {
            Timber.i("No media selected")
        }
    }

    private val getContent = registerForActivityResult(GetImageContract()) { uri: Uri? ->
        if (uri != null) {
            try {
                val path = uri.parseImagePath(requireContext())
                vm.onPickedImageFromDevice(path = path)
            } catch (e: Exception) {
                toast("Error while parsing path for cover image")
                Timber.d(e, "Error while parsing path for cover image")
            }
        } else {
            Timber.e("Error while upload cover image, URI is null")
        }
    }

    override fun injectDependencies() {
        componentManager().profileComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().profileComponent.release()
    }

    companion object {
        const val SPACE_ID_KEY = "arg.profile-settings.space-id"
    }
}

private const val SELECT_IMAGE_CODE = 1
