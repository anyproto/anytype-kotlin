package com.anytypeio.anytype.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel

class OnBoardingFragment : BaseComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Onboarding()
                }
            }
        }
    }

    @Composable
    private fun Onboarding() {
        val navController = rememberNavController()
        NavHost(navController, startDestination = OnboardingNavigation.auth) {
            composable(OnboardingNavigation.auth) {
                val component = componentManager().onboardingAuthComponent.get()
                val viewModel: OnboardingAuthViewModel = daggerViewModel {
                    component.getViewModel()
                }
                AuthScreenWrapper(
                    viewModel = viewModel,
                    navigateToInviteCode = {
                        navController.navigate(OnboardingNavigation.inviteCode)
                    },
                    navigateToLogin = {
                        navController.navigate(OnboardingNavigation.recovery)
                    }
                )
            }
            composable(OnboardingNavigation.inviteCode) {
                InviteCodeScreenWrapper()
            }
            composable(OnboardingNavigation.recovery) {
                RecoveryScreenWrapper()
            }
        }
    }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}

}