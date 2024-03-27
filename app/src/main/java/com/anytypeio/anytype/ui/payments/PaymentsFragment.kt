package com.anytypeio.anytype.ui.payments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.screens.CodeScreen
import com.anytypeio.anytype.screens.MainPaymentsScreen
import com.anytypeio.anytype.screens.PaymentWelcomeScreen
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class PaymentsFragment : BaseBottomSheetComposeFragment(), PurchasesUpdatedListener {

    @Inject
    lateinit var factory: PaymentsViewModelFactory
    private val vm by viewModels<PaymentsViewModel> { factory }
    private lateinit var navController: NavHostController

    @Inject
    lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            tierClicked = {
                //vm::onTierClicked
                setupBilling()
            }
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
        componentManager().paymentsComponent.get(this).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().paymentsComponent.release()
    }

    //region Billing
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Timber.d("onPurchasesUpdated: billingResult:$billingResult, purchases:$purchases")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Toast.makeText(requireContext(), "Purchase: $purchase", Toast.LENGTH_LONG).show()
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(requireContext(), "User canceled", Toast.LENGTH_LONG).show()
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            Toast.makeText(requireContext(), "Error: ${billingResult.responseCode}", Toast.LENGTH_LONG).show()
            // Handle any other error codes.
        }
    }

    private fun setupBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d("Billing", "Billing service disconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Billing setup finished")
                    Timber.d("Billing setup finished")
                    processPurchases()
                    // The BillingClient is ready. You can query purchases here.
                }
            }
        })
    }

    private fun processPurchases() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("test_123456789")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().apply {
            setProductList(productList)
        }.build()
        lifecycleScope.launch {
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                Log.d("Billing", "Billing result: $billingResult")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showProducts(productDetailsList.getOrNull(0))
                    val productDetails = productDetailsList.getOrNull(0)
                    if (productDetails != null) {
                        startPurchaseFlow(productDetails)
                    }
                    // Process the result.
                    Log.d("Billing", "Product details: $productDetailsList")
                } else {
                    Log.d("Billing", "Product details error: $billingResult")
                }
            }
        }
    }

    private fun showProducts(details: ProductDetails?) {
        lifecycleScope.launch {
            Toast.makeText(requireContext(), "Product details: id:[${details?.productId}]", Toast.LENGTH_LONG).show()
        }
    }

    private fun startPurchaseFlow(details: ProductDetails) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("test_123456789")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)
        val activity = requireActivity()

        val token = details.subscriptionOfferDetails?.getOrNull(0)?.offerToken
        val productDetailsParamsList = listOf(
            token?.let {
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(details)
                    // For One-time product, "setOfferToken" method shouldn't be called.
                    // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .setOfferToken(it)
                    .build()
            }
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId("obfuscatedAccountId188888")
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
    //endregion
}