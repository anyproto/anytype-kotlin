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
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_utils.ext.toast
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

    private val vm by viewModels<AccountAndDataViewModel> { factory }

    private val onKeychainPhraseClicked = {
        val bundle = bundleOf(KeychainPhraseDialog.ARG_SCREEN_TYPE to EventsDictionary.Type.screenSettings)
        findNavController().navigate(R.id.keychainDialog, bundle)
    }

    private val onLogoutClicked = {
        findNavController().navigate(R.id.logoutWarningScreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    AccountAndDataScreen(
                        onKeychainPhraseClicked = onKeychainPhraseClicked,
                        onClearFileCachedClicked = { proceedWithClearFileCacheWarning() },
                        onDeleteAccountClicked = { proceedWithAccountDeletion() },
                        onLogoutClicked = onLogoutClicked,
                        onPinCodeClicked = { toast(resources.getString(R.string.coming_soon)) },
                        isLogoutInProgress = vm.isLoggingOut.collectAsState().value,
                        isClearCacheInProgress = vm.isClearFileCacheInProgress.collectAsState().value
                    )
                }
            }
        }
    }

    private fun proceedWithClearFileCacheWarning() {
        vm.onClearCacheButtonClicked()
        val dialog = ClearCacheAlertFragment.new()
        dialog.onClearAccepted = { vm.onClearFileCacheAccepted() }
        dialog.show(childFragmentManager, null)
    }

    private fun proceedWithAccountDeletion() {
        // TODO release this feature when it's ready on all our platforms!
        if (BuildConfig.DEBUG) {
            val dialog = DeleteAccountWarning()
            dialog.onDeletionAccepted = {
                dialog.dismiss()
                vm.onDeleteAccountClicked()
            }
            dialog.show(childFragmentManager, null)
        } else {
            toast(resources.getString(R.string.coming_soon))
        }
    }

    override fun injectDependencies() {
        componentManager().accountAndDataComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().accountAndDataComponent.release()
    }
}

