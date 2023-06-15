package com.anytypeio.anytype.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.GetImageContract
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.auth.account.DeleteAccountWarning
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import com.anytypeio.anytype.ui_settings.account.ProfileScreen
import com.anytypeio.anytype.ui_settings.account.ProfileViewModel
import javax.inject.Inject
import timber.log.Timber

class ProfileFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ProfileViewModel.Factory

    @Inject
    lateinit var toggles: FeatureToggles

    private val vm by viewModels<ProfileViewModel> { factory }

    private val onKeychainPhraseClicked = {
        val bundle =
            bundleOf(KeychainPhraseDialog.ARG_SCREEN_TYPE to EventsDictionary.Type.screenSettings)
        safeNavigate(R.id.keychainDialog, bundle)
    }

    private val onLogoutClicked = {
        safeNavigate(R.id.logoutWarningScreen)
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
                    ProfileScreen(
                        onKeychainPhraseClicked = onKeychainPhraseClicked,
                        onDeleteAccountClicked = { throttle { proceedWithAccountDeletion() } },
                        onLogoutClicked = onLogoutClicked,
                        onSpaceDebugClicked = { throttle { vm.onSpaceDebugClicked() } },
                        isLogoutInProgress = vm.isLoggingOut.collectAsState().value,
                        isDebugSpaceReportInProgress = vm.isDebugSpaceReportInProgress.collectAsState().value,
                        isShowDebug = true,
                        onNameChange = { vm.onNameChange(it) },
                        onProfileIconClick = { proceedWithIconClick() },
                        account = vm.profileData.collectAsStateWithLifecycle().value,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(PADDING_TOP)
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
        if (!hasExternalStoragePermission()) {
            permissionReadStorage.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            openGallery()
        }
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    private val permissionReadStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[Manifest.permission.READ_EXTERNAL_STORAGE]
            if (readResult == true) {
                openGallery()
            } else {
                toast(R.string.permission_read_denied)
            }
        }

    private fun openGallery() {
        getContent.launch(SELECT_IMAGE_CODE)
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
            toast("Error while upload cover image, URI is null")
            Timber.e("Error while upload cover image, URI is null")
        }
    }

    override fun injectDependencies() {
        componentManager().profileComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().profileComponent.release()
    }
}

private const val PADDING_TOP = 54

private const val SELECT_IMAGE_CODE = 1
