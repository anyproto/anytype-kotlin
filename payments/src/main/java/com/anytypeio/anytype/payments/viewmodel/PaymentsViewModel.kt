package com.anytypeio.anytype.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.presentation.membership.models.TierId
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class PaymentsViewModel(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val getAccount: GetAccount,
    private val membershipProvider: MembershipProvider,
    private val getMembershipPaymentUrl: GetMembershipPaymentUrl
) : ViewModel() {

    val viewState = MutableStateFlow<PaymentsMainState>(PaymentsMainState.Loading)
    val codeState = MutableStateFlow<PaymentsCodeState>(PaymentsCodeState.Hidden)
    val tierState = MutableStateFlow<PaymentsTierState>(PaymentsTierState.Hidden)
    val welcomeState = MutableStateFlow<PaymentsWelcomeState>(PaymentsWelcomeState.Hidden)

    val command = MutableStateFlow<PaymentsNavigation?>(null)

    /**
     * Local billing purchase data.
     */
    private val purchases = billingClientLifecycle.subscriptionPurchases

    /**
     * ProductDetails for all known Products.
     */
    private val builderSubProductWithProductDetails =
        billingClientLifecycle.builderSubProductWithProductDetails

    private val _launchBillingCommand = MutableSharedFlow<BillingFlowParams>()
    val launchBillingCommand = _launchBillingCommand.asSharedFlow()

    val initBillingClient = MutableStateFlow(false)

    init {
        Timber.d("PaymentsViewModel init")
        proceedWithGettingMembershipAndProducts()
    }

    private fun proceedWithGettingMembershipAndProducts() {
        viewModelScope.launch {
            combine(
                membershipProvider.status(),
                builderSubProductWithProductDetails
            ) { membershipStatus, productDetails ->
                setupBillingClient(membershipStatus)
                membershipStatus to productDetails
            }.collect { (membershipStatus, products) ->
                viewState.value = toViewState(
                    membershipStatus = membershipStatus,
                    products = products
                )
            }
        }
    }

    private fun setupBillingClient(membershipStatus: MembershipStatus) {
        val androidProductIds = membershipStatus.tiers.mapNotNull { it.androidProductId }
        if (androidProductIds.isNotEmpty()) {
            billingClientLifecycle.setupSubIds(androidProductIds)
            initBillingClient.value = true
        }
    }

    private fun toViewState(
        membershipStatus: MembershipStatus,
        products: List<ProductDetails>
    ): PaymentsMainState {
        val activeTierId = membershipStatus.activeTier.value
        if (activeTierId == 0) {
            Timber.e("Membership has no active tier")
            return PaymentsMainState.ErrorState("No active subscription found, please contact support.")
        }
        val activeTier = membershipStatus.tiers.firstOrNull { it.id == activeTierId }
        if (activeTier == null) {
            Timber.e("Active tier $activeTierId not found in tiers list")
            return PaymentsMainState.ErrorState("No active subscription found, please contact support.")
        }
        val tiers = membershipStatus.toTiersView(
            products = products
        )
        return if (hasBanner(activeTierId)) {
            PaymentsMainState.Default.WithBanner(
                membershipStatus = membershipStatus,
                tiers = tiers
            )
        } else {
            PaymentsMainState.Default.WithoutBanner(
                membershipStatus = membershipStatus,
                tiers = tiers
            )
        }
    }

    fun onTierClicked(tier: Tier) {
        Timber.d("onTierClicked: tierId:$tier")
        tierState.value = PaymentsTierState.Visible.Initial(tier = tier)
        command.value = PaymentsNavigation.Tier
    }

    fun onActionCode(code: String, tierId: TierId) {
//        Timber.d("onActionCode: tierId:$tierId, code:$code, _tiers:${_tiers}")
//        viewModelScope.launch {
//            codeState.value = PaymentsCodeState.Visible.Loading(tierId = tierId)
//            welcomeState.value =
//                PaymentsWelcomeState.Initial(tier = _tiers.first { it.id == tierId })
//            val updatedTiers = _tiers.map {
//                val isCurrent = it.id == tierId
//                when (it) {
//                    is Tier.Builder -> it.copy(isCurrent = isCurrent)
//                    is Tier.CoCreator -> it.copy(isCurrent = isCurrent)
//                    is Tier.Custom -> it.copy(isCurrent = isCurrent)
//                    is Tier.Explorer -> it.copy(isCurrent = isCurrent)
//                }
//            }
//            _tiers.clear()
//            _tiers.addAll(updatedTiers)
//            viewState.value = PaymentsMainState.PaymentSuccess(_tiers)
//            command.value = PaymentsNavigation.Welcome
//        }
    }

    fun onSubmitEmailButtonClicked(tierId: TierId, email: String) {
        Timber.d("onSubmitEmailButtonClicked: email:$email")
        codeState.value = PaymentsCodeState.Visible.Initial(tierId = tierId)
        command.value = PaymentsNavigation.Code
    }

    fun onPayButtonClicked(tierId: TierId, name: String) {
        Timber.d("onPayButtonClicked: tierId:$tierId, name:$name")
//        val tier = tierState.value as? PaymentsTierState.Visible ?: return
//        val androidProductId = tier.tier.
//        if (androidProductId == null) {
//            Timber.e("Tier $tierId has no androidProductId")
//            return
//        }
//        viewModelScope.launch {
//            getMembershipPaymentUrl.async(
//                GetMembershipPaymentUrl.Params(
//                    tierId = tierId.value,
//                    name = ""
//                )
//            ).fold(
//                onSuccess = { url ->
//                    Timber.d("Payment url: $url")
//                    buyBasePlans(
//                        billingId = url.billingId,
//                        product = androidProductId,
//                        upDowngrade = false
//                    )
//                },
//                onFailure = { error ->
//                    Timber.e("Error getting payment url: $error")
//                }
//            )
//        }
    }

    fun onDismissTier() {
        Timber.d("onDismissTier")
        command.value = PaymentsNavigation.Dismiss
    }

    fun onDismissCode() {
        Timber.d("onDismissCode")
        command.value = PaymentsNavigation.Dismiss
    }

    fun onDismissWelcome() {
        Timber.d("onDismissWelcome")
        command.value = PaymentsNavigation.Dismiss
    }

    private fun hasBanner(activeTierId: Int) = activeTierId == EXPLORER_ID

    //region Google Play Billing
    /**
     * Use the Google Play Billing Library to make a purchase.
     *
     * @param tag String representing tags associated with offers and base plans.
     * @param product Product being purchased.
     * @param upDowngrade Boolean indicating if the purchase is an upgrade or downgrade and
     * when converting from one base plan to another.
     *
     */
    private fun buyBasePlans(billingId: String, product: String, upDowngrade: Boolean) {
        Timber.d("buyBasePlans: billingId:$billingId, product:$product, upDowngrade:$upDowngrade")
        //todo check if the user has already purchased the product
        val isProductOnServer = false//serverHasSubscription(subscriptions.value, product)
        val isProductOnDevice = deviceHasGooglePlaySubscription(purchases.value, product)
        Timber.d(
            "Billing product:$product - isProductOnServer: $isProductOnServer," +
                    " isProductOnDevice: $isProductOnDevice"
        )

        when {
            isProductOnDevice && isProductOnServer -> {
                Timber.d("User is trying to top up prepaid subscription: $product. ")
            }

            isProductOnDevice && !isProductOnServer -> {
                Timber.d(
                    "The Google Play Billing Library APIs indicate that " +
                            "this Product is already owned, but the purchase token is not " +
                            "registered with the server."
                )
            }

            !isProductOnDevice && isProductOnServer -> {
                Timber.w(
                    "WHOA! The server says that the user already owns " +
                            "this item: $product. This could be from another Google account. " +
                            "You should warn the user that they are trying to buy something " +
                            "from Google Play that they might already have access to from " +
                            "another purchase, possibly from a different Google account " +
                            "on another device.\n" +
                            "You can choose to block this purchase.\n" +
                            "If you are able to cancel the existing subscription on the server, " +
                            "you should allow the user to subscribe with Google Play, and then " +
                            "cancel the subscription after this new subscription is complete. " +
                            "This will allow the user to seamlessly transition their payment " +
                            "method from an existing payment method to this Google Play account."
                )
                return
            }
        }

        val builderSubProductDetails =
            builderSubProductWithProductDetails.value.firstOrNull { it.productId == product }
                ?: run {
                    Timber.e("Could not find Basic product details by product id: $product")
                    return
                }

        val builderOffers =
            builderSubProductDetails.subscriptionOfferDetails?.let { offerDetailsList ->
                retrieveEligibleOffers(
                    offerDetails = offerDetailsList
                )
            }

        val offerToken: String = builderOffers?.let { leastPricedOfferToken(it) }.toString()
        launchFlow(
            billingId = billingId,
            upDowngrade = upDowngrade,
            offerToken = offerToken,
            productDetails = builderSubProductDetails
        )
    }

    /**
     * Calculates the lowest priced offer amongst all eligible offers.
     * In this implementation the lowest price of all offers' pricing phases is returned.
     * It's possible the logic can be implemented differently.
     * For example, the lowest average price in terms of month could be returned instead.
     *
     * @param offerDetails List of of eligible offers and base plans.
     *
     * @return the offer id token of the lowest priced offer.
     *
     */
    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>
    ): String {
        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        if (offerDetails.isNotEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros < lowestPrice) {
                        lowestPrice = price.priceAmountMicros.toInt()
                        leastPricedOffer = offer
                        offerToken = leastPricedOffer.offerToken
                    }
                }
            }
        }
        return offerToken

        TODO("Replace this with least average priced offer implementation")
    }

    /**
     * Retrieves all eligible base plans and offers using tags from ProductDetails.
     *
     * @param offerDetails offerDetails from a ProductDetails returned by the library.
     * @param tag string representing tags associated with offers and base plans.
     *
     * @return the eligible offers and base plans in a list.
     *
     */
    private fun retrieveEligibleOffers(
        offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>, tag: String = ""
    ):
            List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(tag)) {
                eligibleOffers.add(offerDetail)
            }
        }
        return eligibleOffers
    }

    /**
     * BillingFlowParams Builder for normal purchases.
     *
     * @param productDetails ProductDetails object returned by the library.
     * @param offerToken the least priced offer's offer id token returned by
     * [leastPricedOfferToken].
     *
     * @return [BillingFlowParams] builder.
     */
    private suspend fun billingFlowParamsBuilder(
        billingId: String,
        productDetails: ProductDetails,
        offerToken: String
    ):
            BillingFlowParams {
        val anyId = getAccount.async(Unit).getOrNull()
        return BillingFlowParams.newBuilder()
            .setObfuscatedAccountId(anyId?.id.orEmpty())
            .setObfuscatedProfileId(billingId)
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            ).build()
    }

    /**
     * BillingFlowParams Builder for upgrades and downgrades.
     *
     * @param productDetails ProductDetails object returned by the library.
     * @param offerToken the least priced offer's offer id token returned by
     * [leastPricedOfferToken].
     * @param oldToken the purchase token of the subscription purchase being upgraded or downgraded.
     *
     * @return [BillingFlowParams] builder.
     */
    private fun upDowngradeBillingFlowParamsBuilder(
        productDetails: ProductDetails, offerToken: String, oldToken: String
    ): BillingFlowParams {
        return BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            ).setSubscriptionUpdateParams(
                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(oldToken)
                    .setSubscriptionReplacementMode(
                        BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE
                    ).build()
            ).build()
    }

    /**
     * Launches the billing flow for a subscription product purchase.
     * A user can only have one subscription purchase on the device at a time. If the user
     * has more than one subscription purchase on the device, the app should not allow the
     * user to purchase another subscription.
     *
     * @param upDowngrade Boolean indicating if the purchase is an upgrade or downgrade and
     * when converting from one base plan to another.
     * @param offerToken String representing the offer token of the lowest priced offer.
     * @param productDetails ProductDetails of the product being purchased.
     *
     */
    private fun launchFlow(
        billingId: String,
        upDowngrade: Boolean,
        offerToken: String,
        productDetails: ProductDetails
    ) {

        if (purchases.value.size > EXPECTED_SUBSCRIPTION_PURCHASE_LIST_SIZE) {
            Timber.e("There are more than one subscription purchases on the device.")
            return

            TODO(
                "Handle this case better, such as by showing a dialog to the user or by " +
                        "programmatically getting the correct purchase token."
            )
        }

        val oldToken =
            purchases.value.firstOrNull { it.purchaseToken.isNotEmpty() }?.purchaseToken ?: ""

        viewModelScope.launch {
            val billingParams: BillingFlowParams = if (upDowngrade) {
                upDowngradeBillingFlowParamsBuilder(
                    productDetails = productDetails,
                    offerToken = offerToken,
                    oldToken = oldToken
                )
            } else {
                billingFlowParamsBuilder(
                    productDetails = productDetails,
                    offerToken = offerToken,
                    billingId = billingId
                )
            }
            _launchBillingCommand.emit(billingParams)
        }
    }

    /**
     * This will return true if the Google Play Billing APIs have a record for the subscription.
     * This will not always match the server's record of the subscription for this app user.
     *
     * Example: App user buys the subscription on a different device with a different Google
     * account. The server will show that this app user has the subscription, even if the
     * Google account on this device has not purchased the subscription.
     * In this example, the method will return false.
     *
     * Example: The app user changes by signing out and signing into the app with a different
     * email address. The server will show that this app user does not have the subscription,
     * even if the Google account on this device has purchased the subscription.
     * In this example, the method will return true.
     */
    private fun deviceHasGooglePlaySubscription(purchases: List<Purchase>?, product: String) =
        purchaseForProduct(purchases, product) != null

    /**
     * Return purchase for the provided Product, if it exists.
     */
    private fun purchaseForProduct(purchases: List<Purchase>?, product: String): Purchase? {
        purchases?.let {
            for (purchase in it) {
                if (purchase.products[0] == product) {
                    return purchase
                }
            }
        }
        return null
    }


    companion object {
        const val EXPECTED_SUBSCRIPTION_PURCHASE_LIST_SIZE = 1
    }
}