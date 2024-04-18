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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.screens.CodeScreen
import com.anytypeio.anytype.payments.screens.MainPaymentsScreen
import com.anytypeio.anytype.payments.screens.PaymentWelcomeScreen
import com.anytypeio.anytype.payments.screens.TierScreen
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.payments.viewmodel.PaymentsNavigation
import com.anytypeio.anytype.payments.viewmodel.PaymentsViewModel
import com.anytypeio.anytype.payments.viewmodel.PaymentsViewModelFactory
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

    @Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.subscribe(vm.initBillingClient) { init ->
            if (init) {
                lifecycle.addObserver(billingClientLifecycle)
            }
        }
    }

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
                PaymentsNavigation.Welcome -> {
                    navController.popBackStack(PaymentsNavigation.Main.route, false)
                    navController.navigate(PaymentsNavigation.Welcome.route)
                }
                PaymentsNavigation.Dismiss -> navController.popBackStack()
                else -> {}
            }
        }
        jobs += subscribe(vm.launchBillingCommand) { event ->
            billingClientLifecycle.launchBillingFlow(
                activity = requireActivity(),
                params = event
            )
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
                InitMainPaymentsScreen()
            }
            bottomSheet(PaymentsNavigation.Tier.route) {
                InitTierScreen()
            }
            bottomSheet(PaymentsNavigation.Code.route) {
                InitCodeScreen()
            }
            bottomSheet(PaymentsNavigation.Welcome.route) {
                InitWelcomeScreen()
            }
        }
    }

    @Composable
    private fun InitMainPaymentsScreen() {
        skipCollapsed()
        expand()
        MainPaymentsScreen(
            state = vm.viewState.collectAsStateWithLifecycle().value,
            tierClicked = vm::onTierClicked
        )
    }

    @Composable
    private fun InitTierScreen() {
        TierScreen(
            state = vm.tierState.collectAsStateWithLifecycle().value,
            onDismiss = vm::onDismissTier,
            actionPay = vm::onPayButtonClicked,
            actionSubmitEmail = vm::onSubmitEmailButtonClicked
        )
    }

    @Composable
    private fun InitCodeScreen() {
        CodeScreen(
            state = vm.codeState.collectAsStateWithLifecycle().value,
            actionResend = { },
            actionCode = vm::onActionCode,
            onDismiss = vm::onDismissCode
        )
    }

    @Composable
    private fun InitWelcomeScreen() {
        PaymentWelcomeScreen(
            state = vm.welcomeState.collectAsStateWithLifecycle().value,
            onDismiss = vm::onDismissWelcome
        )
    }

    override fun injectDependencies() {
        componentManager().paymentsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().paymentsComponent.release()
    }
}