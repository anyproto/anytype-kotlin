package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_ui.foundation.Warning
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import com.anytypeio.anytype.ui_settings.account.LogoutWarningViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogoutWarningFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: LogoutWarningViewModel.Factory

    private val vm by viewModels<LogoutWarningViewModel> { factory }

    private val onBackupPhraseClicked = {
        vm.onBackupClicked()
        val bundle = bundleOf(KeychainPhraseDialog.ARG_SCREEN_TYPE to EventsDictionary.Type.beforeLogout)
        findNavController().navigate(R.id.keychainDialog, bundle)
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
                    Warning(
                        actionButtonText = stringResource(R.string.log_out),
                        cancelButtonText = stringResource(R.string.back_up_your_phrase),
                        title = stringResource(R.string.have_you_back_up_your_keychain),
                        subtitle = stringResource(R.string.you_will_need_to_sign_in),
                        onNegativeClick = onBackupPhraseClicked,
                        onPositiveClick = { vm.onLogoutClicked() },
                        isInProgress = vm.isLoggingOut.collectAsState().value
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.commands.collect { command ->
                        when (command) {
                            LogoutWarningViewModel.Command.Logout -> {
                                findNavController().navigate(R.id.actionLogout)
                            }
                        }
                    }
                }
                launch {
                    vm.isLoggingOut.collect { isLoggingOut ->
                        isCancelable = isLoggingOut == false
                    }
                }
            }
        }
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        dialog?.let { d ->
            d.setCanceledOnTouchOutside(cancelable)
            d.window?.decorView?.findViewById<View>(R.id.design_bottom_sheet)?.let {
                BottomSheetBehavior.from(it).isHideable = cancelable
            }
        }
    }

    override fun injectDependencies() {
        componentManager().logoutWarningComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().logoutWarningComponent.release()
    }
}