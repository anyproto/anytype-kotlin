package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.auth.account.DeleteAccountWarning
import com.anytypeio.anytype.ui.dashboard.ClearCacheAlertFragment
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import com.anytypeio.anytype.ui_settings.account.AccountAndDataScreen
import com.anytypeio.anytype.ui_settings.account.AccountAndDataViewModel
import javax.inject.Inject

class AccountAndDataFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: AccountAndDataViewModel.Factory

    @Inject
    lateinit var toggles: FeatureToggles

    private val vm by viewModels<AccountAndDataViewModel> { factory }

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
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    AccountAndDataScreen(
                        onKeychainPhraseClicked = onKeychainPhraseClicked,
                        onClearFileCachedClicked = { throttle { proceedWithClearFileCacheWarning() } },
                        onDeleteAccountClicked = { throttle { proceedWithAccountDeletion() } },
                        onLogoutClicked = onLogoutClicked,
                        onDebugSyncReportClicked = { throttle { vm.onDebugSyncReportClicked() } },
                        isLogoutInProgress = vm.isLoggingOut.collectAsState().value,
                        isClearCacheInProgress = vm.isClearFileCacheInProgress.collectAsState().value,
                        isDebugSyncReportInProgress = vm.isDebugSyncReportInProgress.collectAsState().value,
                        isShowDebug = toggles.isTroubleshootingMode
                    )
                }
            }
        }
    }

    private fun proceedWithClearFileCacheWarning() {
        vm.onClearCacheButtonClicked()
        val dialog = ClearCacheAlertFragment.new()
        dialog.onClearAccepted = { vm.onClearFileCacheAccepted() }
        dialog.showChildFragment()
    }

    private fun proceedWithAccountDeletion() {
        val dialog = DeleteAccountWarning()
        dialog.onDeletionAccepted = {
            dialog.dismiss()
            vm.onDeleteAccountClicked()
        }
        dialog.showChildFragment()
    }

    override fun injectDependencies() {
        componentManager().accountAndDataComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().accountAndDataComponent.release()
    }
}

