package com.anytypeio.anytype.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.screens.CodeScreen
import com.anytypeio.anytype.screens.MainPaymentsScreen
import com.anytypeio.anytype.screens.TierScreen
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.viewmodel.PaymentsNavigation
import com.anytypeio.anytype.viewmodel.PaymentsViewModel
import com.anytypeio.anytype.viewmodel.PaymentsViewModelFactory
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject

class PaymentsFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: PaymentsViewModelFactory
    private val vm by viewModels<PaymentsViewModel> { factory }
    private lateinit var navController: NavHostController

    @OptIn(ExperimentalMaterialNavigationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(context = requireContext(), dialog = requireDialog()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    val bottomSheetNavigator = rememberBottomSheetNavigator()
                    navController = rememberNavController(bottomSheetNavigator)
                    SetupNavigation(bottomSheetNavigator, navController)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += subscribe(vm.command) { command ->
            when (command) {
                PaymentsNavigation.Tier -> navController.navigate(PaymentsNavigation.Tier.route)
                PaymentsNavigation.Code -> navController.navigate(PaymentsNavigation.Code.route)
                PaymentsNavigation.Dismiss -> navController.popBackStack()
                else -> {}
            }
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    private fun SetupNavigation(
        bottomSheetNavigator: BottomSheetNavigator,
        navController: NavHostController
    ) {
        ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
            NavigationGraph(navController = navController)
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    private fun NavigationGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = PaymentsNavigation.Main.route) {
            composable(PaymentsNavigation.Main.route) {
                MainPaymentsScreen()
            }
            bottomSheet(PaymentsNavigation.Tier.route) {
                TierScreen()
            }
            bottomSheet(PaymentsNavigation.Code.route) {
                CodeScreen()
            }
        }
    }

    @Composable
    private fun MainPaymentsScreen() {
        MainPaymentsScreen(
            state = vm.viewState.collectAsStateWithLifecycle().value,
            tierClicked = vm::onTierClicked
        )
    }

    @Composable
    private fun TierScreen() {
        TierScreen(
            tier = vm.selectedTier.collectAsStateWithLifecycle().value,
            onDismiss = vm::onDismissTier,
            actionPay = vm::onPayButtonClicked
        )
    }

    @Composable
    private fun CodeScreen() {
        CodeScreen(
            state = vm.codeViewState.collectAsStateWithLifecycle().value,
            actionResend = { },
            actionCode = vm::onActionCode,
            onDismiss = vm::onDismissCode
        )
    }

    override fun injectDependencies() {
        componentManager().paymentsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().paymentsComponent.release()
    }
}