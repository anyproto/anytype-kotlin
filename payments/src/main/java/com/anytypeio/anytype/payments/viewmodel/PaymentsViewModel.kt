package com.anytypeio.anytype.payments.viewmodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.ResolveMembershipName
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.MEMBERSHIP_NAME_MIN_LENGTH
import com.anytypeio.anytype.payments.mapping.toMainView
import com.anytypeio.anytype.payments.mapping.toView
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierView
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
class PaymentsViewModel(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val getAccount: GetAccount,
    private val membershipProvider: MembershipProvider,
    private val getMembershipPaymentUrl: GetMembershipPaymentUrl,
    private val isMembershipNameValid: IsMembershipNameValid,
    private val resolveMembershipName: ResolveMembershipName
) : ViewModel() {

    val viewState = MutableStateFlow<MembershipMainState>(MembershipMainState.Loading)
    val codeState = MutableStateFlow<PaymentsCodeState>(PaymentsCodeState.Hidden)
    val tierState = MutableStateFlow<MembershipTierState>(MembershipTierState.Hidden)
    val welcomeState = MutableStateFlow<PaymentsWelcomeState>(PaymentsWelcomeState.Hidden)
    val errorState = MutableStateFlow<PaymentsErrorState>(PaymentsErrorState.Hidden)

    val command = MutableStateFlow<PaymentsNavigation?>(null)

    /**
     * Local billing purchase data.
     */
    private val purchases = billingClientLifecycle.subscriptionPurchases

    /**
     * ProductDetails for all known Products.
     */

    private val _billingClientState =
        billingClientLifecycle.builderSubProductWithProductDetails

    private val _launchBillingCommand = MutableSharedFlow<BillingFlowParams>()
    val launchBillingCommand = _launchBillingCommand.asSharedFlow()

    val initBillingClient = MutableStateFlow(false)
    private val membershipStatusState = MutableStateFlow<MembershipStatus?>(null)
    private val showTierState = MutableStateFlow(0)

    @OptIn(ExperimentalFoundationApi::class)
    val anyNameState = TextFieldState(initialText = "")

    init {
        Timber.d("PaymentsViewModel init")
        viewModelScope.launch {
            combine(
                membershipProvider.status()
                    .onEach { setupBillingClient(it) }
                    .onEach { membershipStatusState.value = it },
                _billingClientState
            ) { membershipStatus, billingClientState ->
                membershipStatus to billingClientState
            }.collect { (membershipStatus, billingClientState) ->
                viewState.value = membershipStatus.toMainView(billingClientState)
            }
        }
        viewModelScope.launch {
            combine(
                membershipStatusState,
                _billingClientState,
                showTierState,
                purchases
            ) { membershipStatus, billingClientState, showTier, purchases ->
                TierResult(membershipStatus,billingClientState, showTier, purchases)
            }.collect { (membershipStatus, billingClientState, showTier, billingPurchasesState) ->
                if (showTier != 0) {
                    val tier = membershipStatus?.tiers?.find { it.id == showTier }
                    if (tier != null) {
                        val newTierState = MembershipTierState.Visible(
                            tier.toView(
                                membershipStatus = membershipStatus,
                                billingClientState = billingClientState,
                                billingPurchaseState = billingPurchasesState
                            )
                        )
                        Timber.d("Show tier: $newTierState")
                        tierState.value = newTierState
                        command.value = PaymentsNavigation.Tier
                    } else {
                        Timber.e("Tier $showTier not found in tiers list")
                        errorState.value = PaymentsErrorState.TierNotFound("Tier $showTier not found in tiers list")
                        tierState.value = MembershipTierState.Hidden
                    }
                } else {
                    tierState.value = MembershipTierState.Hidden
                    anyNameState.clearText()
                }
            }
        }
        viewModelScope.launch {
            anyNameState.textAsFlow()
                .debounce(NAME_VALIDATION_DELAY)
                .collectLatest {
                    proceedWithValidatingName(it.toString())
                }
        }
        viewModelScope.launch {
            purchases.collectLatest { billingPurchaseState ->
                logAcknowledgementStatus(billingPurchaseState)
            }
        }
    }

    private suspend fun logAcknowledgementStatus(billingPurchaseState: BillingPurchaseState) {
        when (billingPurchaseState) {
            is BillingPurchaseState.HasPurchases -> {
                //need to check if the purchase is acknowledged
                billingPurchaseState.purchases.forEach { purchase ->
                    if (!purchase.isAcknowledged) {
                        Timber.e("Purchase not acknowledged: ${purchase.purchaseToken}")
                    }
                }
            }
            else -> {}
        }
    }

    private fun setupBillingClient(membershipStatus: MembershipStatus) {
        if (initBillingClient.value) {
            Timber.d("Billing client already initialized")
            return
        }
        val androidProductIds = membershipStatus.tiers.mapNotNull { it.androidProductId }
        if (androidProductIds.isNotEmpty()) {
            billingClientLifecycle.setupSubIds(androidProductIds)
            initBillingClient.value = true
        }
    }

    fun onTierClicked(tierId: TierId) {
        Timber.d("onTierClicked: tierId:${tierId.value}")
        showTierState.value = tierId.value
    }

    fun onActionCode(code: String, tierId: TierId) {
        //todo implement
    }

    fun onTierAction(action: TierAction) {
        Timber.d("onTierAction: action:$action")
        when (action) {
            is TierAction.PayClicked -> onPayButtonClicked(action.tierId)
            is TierAction.ManagePayment -> onManageTierClicked(action.tierId)
            is TierAction.OpenUrl -> {
                command.value = PaymentsNavigation.OpenUrl(action.url)
            }
            TierAction.OpenEmail -> proceedWithSupportEmail()
        }
    }

    private fun proceedWithSupportEmail() {
        viewModelScope.launch {
            val anyId = getAccount.async(Unit).getOrNull()
            command.value = PaymentsNavigation.OpenEmail(anyId?.id)
        }
    }

    private var validateNameJob: Job? = null

    private fun proceedWithValidatingName(name: String) {
        val tierView = (tierState.value as? MembershipTierState.Visible)?.tierView ?: return
        if (name.length < MEMBERSHIP_NAME_MIN_LENGTH) {
            when (tierView.membershipAnyName) {
                is TierAnyName.Visible.Error -> setAnyNameStateToEnter(tierView)
                is TierAnyName.Visible.Validated -> setAnyNameStateToEnter(tierView)
                else -> {}
            }
            return
        }
        if (validateNameJob?.isActive == true) {
            validateNameJob?.cancel()
        }
        setAnyNameStateToValidating(tierView)
        validateNameJob = viewModelScope.launch {
            val params = IsMembershipNameValid.Params(
                tier = tierView.id.value,
                name = name
            )
            isMembershipNameValid.async(params).fold(
                onSuccess = {
                    Timber.d("Name is valid")
                    setAnyNameStateToValidated(tierView, name)
                },
                onFailure = { error ->
                    when (error) {
                        is MembershipErrors.IsNameValid.TooShort -> {
                            Timber.d("Name is too short")
                            setAnyNameStateToError(tierView, error.message)
                        }
                        is MembershipErrors.IsNameValid.TooLong -> {
                            Timber.d("Name is too long")
                            setAnyNameStateToError(tierView, error.message)
                        }
                        is MembershipErrors.IsNameValid.HasInvalidChars -> {
                            Timber.d("Name has invalid chars")
                            setAnyNameStateToError(tierView, error.message)
                        }
                        is MembershipErrors.IsNameValid.CanNotReserve -> {
                            Timber.d("Can not reserve name")
                            setAnyNameStateToError(tierView, error.message)
                        }
                        else -> {
                            Timber.e("Error validating name: $error")
                            setAnyNameStateToError(tierView, error.message ?: "Error validating name")
                        }
                    }
                }
            )
        }
    }

    private fun setAnyNameStateToEnter(tierView: TierView) {
        val updatedTierState = tierView.copy(
            membershipAnyName = TierAnyName.Visible.Enter,
            buttonState = TierButton.Pay.Disabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setAnyNameStateToValidating(tierView: TierView) {
        val updatedTierState = tierView.copy(
            membershipAnyName = TierAnyName.Visible.Validating,
            buttonState = TierButton.Pay.Disabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setAnyNameStateToError(tierView: TierView, message: String) {
        val updatedTierState = tierView.copy(
            membershipAnyName = TierAnyName.Visible.Error(message),
            buttonState = TierButton.Pay.Disabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setAnyNameStateToValidated(tierView: TierView, validatedName: String) {
        val updatedTierState = tierView.copy(
            membershipAnyName = TierAnyName.Visible.Validated(validatedName),
            buttonState = TierButton.Pay.Enabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun onManageTierClicked(tierId: TierId) {
        val membershipStatus = membershipStatusState.value ?: return
        val tier = membershipStatus.tiers.find { it.id == tierId.value } ?: return
        Timber.d("Manage tier: $tier")
        command.value = when (membershipStatus.paymentMethod) {
            MembershipPaymentMethod.METHOD_NONE -> {
                PaymentsNavigation.OpenUrl(null)
            }

            MembershipPaymentMethod.METHOD_STRIPE -> {
                PaymentsNavigation.OpenUrl(tier.stripeManageUrl)
            }

            MembershipPaymentMethod.METHOD_CRYPTO -> {
                PaymentsNavigation.OpenUrl(null)
            }

            MembershipPaymentMethod.METHOD_INAPP_APPLE -> {
                PaymentsNavigation.OpenUrl(tier.iosManageUrl)
            }

            MembershipPaymentMethod.METHOD_INAPP_GOOGLE -> {
                PaymentsNavigation.OpenUrl(MANAGE_SUBSCRIPTION_URL)
            }
        }
    }

    private fun onPayButtonClicked(tierId: TierId) {
        proceedWithResolveName(tierId, anyNameState.text.toString())
    }

    private fun proceedWithResolveName(tierId: TierId, name: String) {
        viewModelScope.launch {
            val params = ResolveMembershipName.Params(
                name = name
            )
            resolveMembershipName.async(params).fold(
                onSuccess = {
                    Timber.d("Name resolved")
                    proceedWithPurchase(tierId, name)
                },
                onFailure = { error ->
                    Timber.e("Error resolving name: $error")
                }
            )
        }
    }

    private suspend fun proceedWithPurchase(tierId: TierId, name: String) {
        val tier = membershipStatusState.value?.tiers?.find { it.id == tierId.value } ?: return
        Timber.d("Tier: $tier")
        val androidProductId = tier.androidProductId
        if (androidProductId == null) {
            Timber.e("Tier ${tier.id} has no androidProductId")
            return
        }
        getMembershipPaymentUrl.async(
            GetMembershipPaymentUrl.Params(
                tierId = tier.id,
                name = name
            )
        ).fold(
            onSuccess = { url ->
                Timber.d("Payment url: $url")
                buyBasePlans(
                    billingId = url.billingId,
                    product = androidProductId,
                    upDowngrade = false
                )
            },
            onFailure = { error ->
                Timber.e("Error getting payment url: $error")
            }
        )
    }

    fun onDismissTier() {
        Timber.d("onDismissTier")
        command.value = PaymentsNavigation.Dismiss
        showTierState.value = 0
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
        val billingPurchaseState = purchases.value
        val isProductOnDevice = if (billingPurchaseState is BillingPurchaseState.HasPurchases)  {
            deviceHasGooglePlaySubscription(billingPurchaseState.purchases, product)
        } else {
            false
        }
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
            (_billingClientState.value as? BillingClientState.Connected)?.productDetails?.firstOrNull { it.productId == product }
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
        val billingPurchaseState = purchases.value
        if (billingPurchaseState is BillingPurchaseState.HasPurchases && billingPurchaseState.purchases.size > EXPECTED_SUBSCRIPTION_PURCHASE_LIST_SIZE) {
            Timber.e("There are more than one subscription purchases on the device.")
            return

            TODO(
                "Handle this case better, such as by showing a dialog to the user or by " +
                        "programmatically getting the correct purchase token."
            )
        }

        val oldToken = (billingPurchaseState as? BillingPurchaseState.HasPurchases)?.purchases?.firstOrNull { it.purchaseToken.isNotEmpty() }?.purchaseToken ?: ""

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
        const val NAME_VALIDATION_DELAY = 300L
        const val MANAGE_SUBSCRIPTION_URL = "https://play.google.com/store/account/subscriptions?sku=id_android_sub_builder&package=com.anytypeio.anytype"
    }
}

data class TierResult(
    val membershipStatus: MembershipStatus?,
    val billingClientState: BillingClientState,
    val showTier: Int,
    val purchases: BillingPurchaseState
)