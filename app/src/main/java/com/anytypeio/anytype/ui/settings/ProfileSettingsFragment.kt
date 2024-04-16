package com.anytypeio.anytype.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_utils.ext.GetImageContract
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.MediaPermissionHelper
import com.anytypeio.anytype.ui.auth.account.DeleteAccountWarning
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import com.anytypeio.anytype.ui_settings.account.ProfileSettingsScreen
import com.anytypeio.anytype.ui_settings.account.ProfileSettingsViewModel
import javax.inject.Inject
import timber.log.Timber

class ProfileSettingsFragment : BaseBottomSheetComposeFragment() {

    private val space: Id get() = arg(SPACE_ID_KEY)

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
        jobs += lifecycleScope.subscribe(vm.debugSyncReportUri) { uri ->
            if (uri != null) {
                shareFile(uri)
            }
        }
        return ComposeDialogView(
            context = requireContext(),
            dialog = requireDialog()
        ).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    ProfileSettingsScreen(
                        onKeychainPhraseClicked = onKeychainPhraseClicked,
                        onLogoutClicked = onLogoutClicked,
                        isLogoutInProgress = vm.isLoggingOut.collectAsState().value,
                        onNameChange = { vm.onNameChange(it) },
                        onProfileIconClick = { proceedWithIconClick() },
                        account = vm.profileData.collectAsStateWithLifecycle().value,
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
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(PADDING_TOP)
    }

    override fun onStart() {
        super.onStart()
        subscribe(vm.toasts) {
            toast(it)
        }
    }

    private fun proceedWithAccountDeletion() {
        vm.proceedWithAccountDeletion()
        val dialog = DeleteAccountWarning()
        dialog.onDeletionAccepted = {
            dialog.dismiss()
            vm.onDeleteAccountClicked()
        }
        dialog.show(childFragmentManager, null)
    }

    private fun proceedWithIconClick() {
        permissionHelper.openFilePicker(Mimetype.MIME_IMAGE_ALL, null)
    }

    private fun openGallery() {
        getContent.launch(SELECT_IMAGE_CODE)
    }

    private val getContent = registerForActivityResult(GetImageContract()) { uri: Uri? ->
        if (uri != null) {
            try {
                val path = uri.parseImagePath(requireContext())
                vm.onPickedImageFromDevice(path = path, space = space)
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

private const val PADDING_TOP = 28

private const val SELECT_IMAGE_CODE = 1
