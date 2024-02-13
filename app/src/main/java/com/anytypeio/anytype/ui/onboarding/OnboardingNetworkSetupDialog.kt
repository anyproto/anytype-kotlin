package com.anytypeio.anytype.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.onboarding.screens.signin.NetworkSetupScreen
import com.anytypeio.anytype.ui.settings.typography

class OnboardingNetworkSetupDialog : BaseBottomSheetComposeFragment() {

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
                    config = NetworkModeConfig(),
                    onSelfHostNetworkClicked = {

                    },
                    onSetSelfHostConfigConfigClicked = {

                    },
                    onLocalOnlyClicked = {

                    },
                    onAnytypeNetworkClicked = {

                    }
                )
            }
        }
    }
}