package com.anytypeio.anytype.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.NetworkModeConstants
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.PreferencesViewModel
import com.anytypeio.anytype.ui.onboarding.screens.signin.NetworkSetupScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class OnboardingNetworkSetupDialog : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: PreferencesViewModel.Factory
    private val vm by viewModels<PreferencesViewModel> { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.NetworkSettingDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =  ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                NetworkSetupScreen(
                    config = vm.networkModeState.collectAsStateWithLifecycle().value,
                    onSelfHostNetworkClicked = {
                        vm.proceedWithNetworkMode(NetworkModeConstants.NETWORK_MODE_CUSTOM)
                    },
                    onSetSelfHostConfigConfigClicked = {
                        toast("coming soon")
                    },
                    onLocalOnlyClicked = {
                        vm.proceedWithNetworkMode(NetworkModeConstants.NETWORK_MODE_LOCAL)
                    },
                    onAnytypeNetworkClicked = {
                        vm.proceedWithNetworkMode(NetworkModeConstants.NETWORK_MODE_DEFAULT)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun injectDependencies() {
        componentManager().appPreferencesComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().appPreferencesComponent.release()
    }
}