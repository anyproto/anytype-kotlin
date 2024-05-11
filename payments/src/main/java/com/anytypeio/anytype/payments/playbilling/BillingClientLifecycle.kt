package com.anytypeio.anytype.payments.playbilling

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BillingClientLifecycle(
    private val dispatchers: AppCoroutineDispatchers,
    private val applicationContext: Context,
    private val scope: CoroutineScope
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    ProductDetailsResponseListener, PurchasesResponseListener {

    private val _subscriptionPurchases =
        MutableStateFlow<BillingPurchaseState>(BillingPurchaseState.Loading)

    /**
     * Purchases are collectable. This list will be updated when the Billing Library
     * detects new or existing purchases.
     */
    val subscriptionPurchases = _subscriptionPurchases.asStateFlow()

    /**
     * Cached in-app product purchases details.
     */
    private var cachedPurchasesList: List<Purchase>? = null

    /**
     * ProductDetails for all known products.
     */
    private val _builderSubProductWithProductDetails =
        MutableStateFlow<BillingClientState>(BillingClientState.Loading)
    val builderSubProductWithProductDetails: StateFlow<BillingClientState> =
        _builderSubProductWithProductDetails

    /**
     * Instantiate a new BillingClient instance.
     */
    private lateinit var billingClient: BillingClient

    private val subscriptionIds = mutableListOf<String>()

    fun setupSubIds(ids: List<String>) {
        subscriptionIds.clear()
        subscriptionIds.addAll(ids)
    }

    override fun onCreate(owner: LifecycleOwner) {
        Timber.d("ON_CREATE")
        // Create a new BillingClient in onCreate().
        // Since the BillingClient can only be used once, we need to create a new instance
        // after ending the previous connection to the Google Play Store in onDestroy().
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases() // Not used for subscriptions.
            .build()
        if (!billingClient.isReady) {
            Timber.d("BillingClient: Start connection...")
            billingClient.startConnection(this)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Timber.d("ON_DESTROY")
        if (billingClient.isReady) {
            Timber.d("BillingClient can only be used once -- closing connection")
            // BillingClient can only be used once.
            // After calling endConnection(), we must create a new BillingClient.
            billingClient.endConnection()
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            // The billing client is ready.
            // You can query product details and purchases here.
            querySubscriptionProductDetails()
            querySubscriptionPurchases()
        } else {
            Timber.e("onBillingSetupFinished: BillingResponse $responseCode")
            _builderSubProductWithProductDetails.value =
                BillingClientState.Error("BillingResponse $responseCode")
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.d("onBillingServiceDisconnected")
        // TODO: Try connecting again with exponential backoff.
        // billingClient.startConnection(this)
    }

    /**
     * In order to make purchases, you need the [ProductDetails] for the item or subscription.
     * This is an asynchronous call that will receive a result in [onProductDetailsResponse].
     *
     * querySubscriptionProductDetails uses method calls from GPBL 5.0.0. PBL5, released in May 2022,
     * is backwards compatible with previous versions.
     * To learn more about this you can read:
     * https://developer.android.com/google/play/billing/compatibility
     */
    private fun querySubscriptionProductDetails() {
        Timber.d("querySubscriptionProductDetails")
        val params = QueryProductDetailsParams.newBuilder()

        val productList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
        for (product in subscriptionIds) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        params.setProductList(productList).let { productDetailsParams ->
            billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
        }

    }

    /**
     * Receives the result from [querySubscriptionProductDetails].
     *
     * Store the ProductDetails and post them in the [explorerSubProductWithProductDetails] and
     * [builderSubProductWithProductDetails]. This allows other parts of the app to use the
     *  [ProductDetails] to show product information and make purchases.
     *
     * onProductDetailsResponse() uses method calls from GPBL 5.0.0. PBL5, released in May 2022,
     * is backwards compatible with previous versions.
     * To learn more about this you can read:
     * https://developer.android.com/google/play/billing/compatibility
     */
    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.d("onProductDetailsResponse: ${productDetailsList.size} product(s)")
            processProductDetails(productDetailsList)
        } else {
            Timber.e("onProductDetailsResponse: ${billingResult.responseCode}")
            _builderSubProductWithProductDetails.value =
                BillingClientState.Error("onProductDetailsResponse: ${billingResult.responseCode}")
        }
    }

    /**
     * This method is used to process the product details list returned by the [BillingClient]and
     * post the details to the [explorerSubProductWithProductDetails] and
     * [builderSubProductWithProductDetails] live data.
     *
     * @param productDetailsList The list of product details.
     *
     */
    private fun processProductDetails(productDetailsList: MutableList<ProductDetails>) {
        val expectedProductDetailsCount = subscriptionIds.size
        if (productDetailsList.isEmpty()) {
            Timber.e("Expected ${expectedProductDetailsCount}, Found null ProductDetails.")
            postProductDetails(emptyList())
        } else {
            postProductDetails(productDetailsList)
        }
    }

    /**
     * This method is used to post the product details to the [explorerSubProductWithProductDetails]
     * and [builderSubProductWithProductDetails] live data.
     *
     * @param productDetailsList The list of product details.
     *
     */
    private fun postProductDetails(productDetailsList: List<ProductDetails>) {
        val result = mutableListOf<ProductDetails>()
        productDetailsList.forEach { productDetails ->
            when (productDetails.productType) {
                BillingClient.ProductType.SUBS -> {
                    if (subscriptionIds.contains(productDetails.productId)) {
                        Timber.d("Subscription ProductDetails: $productDetails")
                        result.add(productDetails)
                    }
                }
            }
        }
        if (result.isNotEmpty()) {
            _builderSubProductWithProductDetails.value = BillingClientState.Connected(result)
        } else {
            Timber.e("No product details found for subscriptionIds: $subscriptionIds")
            _builderSubProductWithProductDetails.value =
                BillingClientState.Error("No product details found for subscriptionIds: $subscriptionIds")
        }
    }

    /**
     * Query Google Play Billing for existing subscription purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    private fun querySubscriptionPurchases() {
        if (!billingClient.isReady) {
            Timber.w("querySubscriptionPurchases: BillingClient is not ready")
            billingClient.startConnection(this)
        }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(), this
        )
    }

    /**
     * Callback from the billing library when queryPurchasesAsync is called.
     */
    override fun onQueryPurchasesResponse(
        billingResult: BillingResult,
        purchasesList: MutableList<Purchase>
    ) {
        processPurchases(purchasesList)
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases == null) {
                    Timber.d("onPurchasesUpdated: null purchase list")
                    processPurchases(null)
                } else {
                    processPurchases(purchases)
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.i("onPurchasesUpdated: User canceled the purchase")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.i("onPurchasesUpdated: The user already owns this item")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Timber.e(
                    "onPurchasesUpdated: Developer error means that Google Play does " +
                            "not recognize the configuration."
                )
            }
            else -> {
                Timber.e("onPurchasesUpdated: BillingResponseCode $responseCode")
            }
        }
    }

    /**
     * Send purchase to StateFlow, which will trigger network call to verify the subscriptions
     * on the sever.
     */
    private fun processPurchases(purchasesList: List<Purchase>?) {
        Timber.d("processPurchases: ${purchasesList?.size} purchase(s)")
        purchasesList?.let { list ->
            if (isUnchangedPurchaseList(list)) {
                Timber.d("processPurchases: Purchase list has not changed")
                return
            }
            scope.launch(dispatchers.io) {
                val subscriptionPurchaseList = list.filter { purchase ->
                    purchase.products.any { product ->
                        product in subscriptionIds
                    }
                }
                if (subscriptionPurchaseList.isEmpty()) {
                    Timber.d("processPurchases: No subscription purchases found")
                    _subscriptionPurchases.emit(BillingPurchaseState.NoPurchases)
                } else {
                    _subscriptionPurchases.emit(
                        BillingPurchaseState.HasPurchases(
                            subscriptionPurchaseList
                        )
                    )
                }
            }
            logAcknowledgementStatus(list)
        }
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */
    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>): Boolean {
        val isUnchanged = purchasesList == cachedPurchasesList
        if (!isUnchanged) {
            cachedPurchasesList = purchasesList
        }
        return isUnchanged
    }

    /**
     * Log the number of purchases that are acknowledge and not acknowledged.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * When the purchase is first received, it will not be acknowledge.
     * This application sends the purchase token to the server for registration. After the
     * purchase token is registered to an account, the Android app acknowledges the purchase token.
     * The next time the purchase list is updated, it will contain acknowledged purchases.
     */
    private fun logAcknowledgementStatus(purchasesList: List<Purchase>) {
        var acknowledgedCounter = 0
        var unacknowledgedCounter = 0
        for (purchase in purchasesList) {
            if (purchase.isAcknowledged) {
                acknowledgedCounter++
            } else {
                unacknowledgedCounter++
            }
        }
        Timber.d(
            "logAcknowledgementStatus: acknowledged=$acknowledgedCounter " +
                    "unacknowledged=$unacknowledgedCounter"
        )
    }

    /**
     * Launching the billing flow.
     *
     * Launching the UI to make a purchase requires a reference to the Activity.
     */
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        if (!billingClient.isReady) {
            Timber.e("launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }
}

sealed class BillingClientState {
    data object Loading : BillingClientState()
    data class Error(val message: String) : BillingClientState()
    //Connected state is suppose that we have non empty list of product details
    data class Connected(val productDetails: List<ProductDetails>) : BillingClientState()
}

sealed class BillingPurchaseState {
    data object Loading : BillingPurchaseState()
    data class HasPurchases(val purchases: List<Purchase>) : BillingPurchaseState()
    data object NoPurchases : BillingPurchaseState()
}