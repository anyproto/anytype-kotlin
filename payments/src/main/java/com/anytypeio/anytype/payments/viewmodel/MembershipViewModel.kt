package com.anytypeio.anytype.payments.viewmodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.membership.EmailVerificationStatus
import com.anytypeio.anytype.core_models.membership.MembershipConstants.STARTER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.MEMBERSHIP_NAME_MIN_LENGTH
import com.anytypeio.anytype.core_models.membership.MembershipConstants.NONE_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.OLD_EXPLORER_ID
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.payments.GetMembershipEmailStatus
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.payments.VerifyMembershipEmailCode
import com.anytypeio.anytype.payments.mapping.toMainView
import com.anytypeio.anytype.payments.models.MembershipPurchase
import com.anytypeio.anytype.payments.models.Tier
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMembershipClickEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMembershipPurchaseEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMembershipScreenEvent
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
class MembershipViewModel(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val getAccount: GetAccount,
    private val membershipProvider: MembershipProvider,
    private val getMembershipPaymentUrl: GetMembershipPaymentUrl,
    private val isMembershipNameValid: IsMembershipNameValid,
    private val setMembershipEmail: SetMembershipEmail,
    private val verifyMembershipEmailCode: VerifyMembershipEmailCode,
    private val getMembershipEmailStatus: GetMembershipEmailStatus
) : ViewModel() {

    val viewState = MutableStateFlow<MembershipMainState>(MembershipMainState.Loading)
    val codeState = MutableStateFlow<MembershipEmailCodeState>(MembershipEmailCodeState.Hidden)
    val tierState = MutableStateFlow<MembershipTierState>(MembershipTierState.Hidden)
    val welcomeState = MutableStateFlow<WelcomeState>(WelcomeState.Hidden)
    val errorState = MutableStateFlow<MembershipErrorState>(MembershipErrorState.Hidden)

    val navigation = MutableSharedFlow<MembershipNavigation>(0)

    private val _showTierOnStart = MutableStateFlow(NONE_ID)

    /**
     * Local billing purchase data.
     */
    private val billingPurchases = billingClientLifecycle.subscriptionPurchases

    /**
     * ProductDetails for all known Products.
     */
    private val billingProducts = billingClientLifecycle.builderSubProductWithProductDetails

    private val _launchBillingCommand = MutableSharedFlow<BillingFlowParams>()
    val launchBillingCommand = _launchBillingCommand.asSharedFlow()

    val initBillingClient = MutableStateFlow(false)

    val anyNameState = TextFieldState(initialText = "")

    val anyEmailState = TextFieldState(initialText = "")

    private val forceRefreshTrigger = MutableStateFlow(false)

    init {
        Timber.i("MembershipViewModel, init")
        viewModelScope.launch {
            combine(
                _showTierOnStart.filter { it != NONE_ID },
                viewState.filterIsInstance<MembershipMainState.Default>()
            ) { tierId, _ -> tierId }.collect { tierId ->
                Timber.d("_showTierOnStart, get new value:$tierId")
                proceedWithShowingTier(TierId(tierId))
            }
        }

        viewModelScope.launch {
            val account = getAccount.async(Unit)
            val accountId = account.getOrNull()?.id.orEmpty()
            forceRefreshTrigger.flatMapLatest { forceRefresh ->
                combine(
                    membershipProvider.status(forceRefresh = forceRefresh)
                        .onEach { setupBillingClient(it) },
                    billingProducts,
                    billingPurchases
                ) { membershipStatus, billingProducts, billingPurchases ->
                    Timber.d("TierResult: " +
                            "\n----------------------------\nmembershipStatus:[$membershipStatus]," +
                            "\n----------------------------\nbillingProducts:[$billingProducts]," +
                            "\n----------------------------\nbillingPurchases:[$billingPurchases]")
                    MainResult(membershipStatus, billingProducts, billingPurchases)
                }
            }.collect { (membershipStatus, billingClientState, purchases) ->
                val newState = membershipStatus.toMainView(
                    billingClientState = billingClientState,
                    billingPurchaseState = purchases,
                    accountId = accountId
                )
                proceedWithUpdatingVisibleTier(newState)
                viewState.value = newState
            }
        }

        viewModelScope.launch {
            snapshotFlow { anyNameState.text }
                .debounce(NAME_VALIDATION_DELAY)
                .collectLatest {
                    proceedWithValidatingName(it.toString())
                }
        }

        viewModelScope.launch {
            billingPurchases.collectLatest { billingPurchaseState ->
                checkPurchaseStatus(billingPurchaseState)
            }
        }
    }

    fun showTierOnStart(tierId: String?) {
        val tier = tierId?.toIntOrNull()
        if (tier != null && tier != NONE_ID) {
            _showTierOnStart.value = tier
        }
    }

    private fun proceedWithUpdatingVisibleTier(mainState: MembershipMainState) {
        val actualTier = tierState.value
        if (actualTier is MembershipTierState.Visible && mainState is MembershipMainState.Default) {
            val tierView = mainState.tiers.find { it.id == actualTier.tier.id } ?: return
            tierState.value = MembershipTierState.Visible(tierView)
        }
    }

    private fun checkPurchaseStatus(billingPurchaseState: BillingPurchaseState) {
        if (billingPurchaseState is BillingPurchaseState.HasPurchases && billingPurchaseState.isNewPurchase) {
            Timber.d("Billing purchase state: $billingPurchaseState")
            //Got new purchase, force refresh membership status and tiers
            forceRefreshTrigger.value = true
            //Show success screen
            val tierView = (tierState.value as? MembershipTierState.Visible)?.tier ?: return
            proceedWithHideTier()
            welcomeState.value = WelcomeState.Initial(tierView)
            viewModelScope.launch {
                sendAnalyticsMembershipPurchaseEvent(
                    analytics = analytics,
                    tier = tierView.title
                )
            }
            proceedWithNavigation(MembershipNavigation.Welcome)
            // Reset force refresh trigger after a delay to allow normal updates
            viewModelScope.launch {
                delay(FORCE_REFRESH_RESET_DELAY_MS)
                forceRefreshTrigger.value = false
            }
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
        proceedWithShowingTier(tierId)
    }

    private fun proceedWithShowingTier(tierId: TierId) {
        val visibleTier = getTierById(tierId) ?: return
        tierState.value = MembershipTierState.Visible(visibleTier)
        viewModelScope.launch {
            sendAnalyticsMembershipScreenEvent(
                analytics = analytics,
                tier = visibleTier.title
            )
        }
        proceedWithNavigation(MembershipNavigation.Tier)
    }

    private fun proceedWithHideTier() {
        tierState.value = MembershipTierState.Hidden
        anyEmailState.clearText()
        anyNameState.clearText()
    }

    fun onTierAction(action: TierAction) {
        Timber.d("onTierAction: action:$action")
        when (action) {
            is TierAction.PayClicked -> {
                sendAnalyticsClickEvent(EventsDictionary.MembershipTierButton.PAY)
                onPayButtonClicked(action.tierId)
            }
            is TierAction.ManagePayment -> {
                sendAnalyticsClickEvent(EventsDictionary.MembershipTierButton.MANAGE)
                onManageTierClicked(action.tierId)
            }
            is TierAction.OpenUrl -> {
                sendAnalyticsClickEvent(EventsDictionary.MembershipTierButton.INFO)
                proceedWithNavigation(MembershipNavigation.OpenUrl(action.url))
            }
            TierAction.OpenEmail -> proceedWithSupportEmail()
            is TierAction.SubmitClicked -> {
                sendAnalyticsClickEvent(EventsDictionary.MembershipTierButton.SUBMIT)
                proceedWithSettingEmail(
                    email = anyEmailState.text.toString(),
                )
            }
            TierAction.OnResendCodeClicked -> {
                proceedWithSettingEmail(
                    email = anyEmailState.text.toString(),
                )
            }
            is TierAction.OnVerifyCodeClicked -> {
                proceedWithValidatingEmailCode(action.code)
            }
            TierAction.ChangeEmail -> {
                sendAnalyticsClickEvent(EventsDictionary.MembershipTierButton.CHANGE_EMAIL)
                val tierView = (tierState.value as? MembershipTierState.Visible)?.tier ?: return
                val updatedTierState = tierView.copy(
                    email = TierEmail.Visible.Enter,
                    buttonState = TierButton.Submit.Enabled
                )
                tierState.value = MembershipTierState.Visible(updatedTierState)
            }

            is TierAction.ContactUsError -> {
                sendAnalyticsClickEvent(EventsDictionary.MembershipTierButton.CONTACT_US)
                proceedWithSupportErrorEmail(action.error)
            }
        }
    }

    private fun proceedWithSupportEmail() {
        viewModelScope.launch {
            val anyId = getAccount.async(Unit).getOrNull()
            proceedWithNavigation(MembershipNavigation.OpenEmail(anyId?.id))
        }
    }

    private fun proceedWithSupportErrorEmail(error: String) {
        viewModelScope.launch {
            val anyId = getAccount.async(Unit).getOrNull()
            proceedWithNavigation(
                MembershipNavigation.OpenErrorEmail(
                    accountId = anyId?.id,
                    error = error
                )
            )
        }
    }

    private var validateNameJob: Job? = null

    private fun proceedWithValidatingName(name: String) {
        val tierView = (tierState.value as? MembershipTierState.Visible)?.tier ?: return
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
        setValidatingAnyNameState(tierView)
        validateNameJob = viewModelScope.launch {
            val params = IsMembershipNameValid.Params(
                tier = tierView.id.value,
                name = name
            )
            isMembershipNameValid.async(params).fold(
                onSuccess = {
                    Timber.d("Name is valid")
                    setValidatedAnyNameState(tierView, name)
                },
                onFailure = { error ->
                    Timber.w("Error validating name: $error")
                    setErrorAnyNameState(tierView, error)
                }
            )
        }
    }

    private fun setAnyNameStateToEnter(tier: Tier) {
        val updatedTierState = tier.copy(
            membershipAnyName = TierAnyName.Visible.Enter,
            buttonState = TierButton.Pay.Disabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setValidatingAnyNameState(tier: Tier) {
        val updatedTierState = tier.copy(
            membershipAnyName = TierAnyName.Visible.Validating,
            buttonState = TierButton.Pay.Disabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setErrorAnyNameState(tier: Tier, error: Throwable) {
        val state = if (error is MembershipErrors) {
            TierAnyName.Visible.Error(error)
        } else {
            TierAnyName.Visible.ErrorOther(error.message)
        }
        val updatedTierState = tier.copy(
            membershipAnyName = state,
            buttonState = TierButton.Pay.Disabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setValidatedAnyNameState(tier: Tier, validatedName: String) {
        val updatedTierState = tier.copy(
            membershipAnyName = TierAnyName.Visible.Validated(validatedName),
            buttonState = TierButton.Pay.Enabled
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setErrorEmailCodeState(error: Throwable) {
        val state = if (error is MembershipErrors.VerifyEmailCode) {
            MembershipEmailCodeState.Visible.Error(error)
        } else {
            MembershipEmailCodeState.Visible.ErrorOther(error.message)
        }
        codeState.value = state
    }

    private fun setErrorSettingEmailState(error: Throwable) {
        val tierView = (tierState.value as? MembershipTierState.Visible)?.tier ?: return
        val state = if (error is MembershipErrors.GetVerificationEmail) {
            TierEmail.Visible.Error(error)
        } else {
            TierEmail.Visible.ErrorOther(error.message)
        }
        val updatedTierState = tierView.copy(
            email = state
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setValidatingEmailState() {
        val tierView = (tierState.value as? MembershipTierState.Visible)?.tier ?: return
        val updatedTierState = tierView.copy(
            email = TierEmail.Visible.Validating
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun setValidatedEmailState() {
        val tierView = (tierState.value as? MembershipTierState.Visible)?.tier ?: return
        val updatedTierState = tierView.copy(
            email = TierEmail.Visible.Validated
        )
        tierState.value = MembershipTierState.Visible(updatedTierState)
    }

    private fun onManageTierClicked(tierId: TierId) {
        val tier = getTierById(tierId) ?: return
        Timber.d("Manage tier: $tier")
        val navigationCommand = when (tier.paymentMethod) {
            MembershipPaymentMethod.METHOD_NONE -> {
                MembershipNavigation.OpenUrl(null)
            }
            MembershipPaymentMethod.METHOD_STRIPE -> {
                MembershipNavigation.OpenUrl(tier.stripeManageUrl)
            }
            MembershipPaymentMethod.METHOD_CRYPTO -> {
                MembershipNavigation.OpenUrl(null)
            }
            MembershipPaymentMethod.METHOD_INAPP_APPLE -> {
                MembershipNavigation.OpenUrl(tier.iosManageUrl)
            }
            MembershipPaymentMethod.METHOD_INAPP_GOOGLE -> {
                val androidProductId = tier.androidProductId
                val url = if (androidProductId != null) {
                    //todo обернуть в функцию
                    "https://play.google.com/store/account/subscriptions?sku=$androidProductId&package=io.anytype.app"
                } else {
                    null
                }
                MembershipNavigation.OpenUrl(url)
            }
        }
        proceedWithNavigation(navigationCommand)
    }

    private fun onPayButtonClicked(tierId: TierId) {
        proceedWithPurchase(tierId, anyNameState.text.toString())
    }

    private fun proceedWithPurchase(tierId: TierId, name: String) {
        val tier = getTierById(tierId) ?: return
        val androidProductId = tier.androidProductId
        if (androidProductId == null) {
            Timber.e("Tier ${tier.id} has no androidProductId")
            return
        }
        val params = GetMembershipPaymentUrl.Params(
            tierId = tier.id.value,
            name = name
        )
        viewModelScope.launch {
            getMembershipPaymentUrl.async(params).fold(
                onSuccess = { response ->
                    Timber.d("Payment url: $response")
                    buyBasePlans(
                        billingId = response.billingId,
                        product = androidProductId
                    )
                },
                onFailure = { error ->
                    Timber.e("Error getting payment url: $error")
                }
            )
        }
    }

    private fun proceedWithGettingEmailStatus() {
        viewModelScope.launch {
            getMembershipEmailStatus.async(Unit).fold(
                onSuccess = { status ->
                    val tierView =
                        (tierState.value as? MembershipTierState.Visible)?.tier ?: return@fold
                    when (status) {
                        EmailVerificationStatus.STATUS_VERIFIED -> {
                            if (tierView.id.value == STARTER_ID || tierView.id.value == OLD_EXPLORER_ID) {
                                anyEmailState.clearText()
                                val updatedState = tierView.copy(
                                    email = TierEmail.Hidden,
                                    buttonState = TierButton.ChangeEmail
                                )
                                tierState.value = MembershipTierState.Visible(updatedState)
                            } else {
                                codeState.value = MembershipEmailCodeState.Visible.Initial
                                proceedWithNavigation(MembershipNavigation.Code)
                            }
                        }
                        else -> {}
                    }
                    Timber.d("Email status: $status")
                },
                onFailure = { error ->
                    Timber.e("Error getting email status: $error")
                }
            )
        }
    }

    private fun proceedWithSettingEmail(email: String) {
        setValidatingEmailState()
        val params = SetMembershipEmail.Params(email, true)
        viewModelScope.launch {
            setMembershipEmail.async(params).fold(
                onSuccess = {
                    Timber.d("Email set")
                    setValidatedEmailState()
                    codeState.value = MembershipEmailCodeState.Visible.Initial
                    proceedWithNavigation(MembershipNavigation.Code)
                },
                onFailure = { error ->
                    Timber.e("Error setting email: $error")
                    setErrorSettingEmailState(error)
                }
            )
        }
    }

    private fun proceedWithValidatingEmailCode(code: String) {
        codeState.value = MembershipEmailCodeState.Visible.Loading
        viewModelScope.launch {
            verifyMembershipEmailCode.async(VerifyMembershipEmailCode.Params(code)).fold(
                onSuccess = {
                    Timber.d("Email code verified")
                    // Force refresh membership status and tiers after email verification
                    forceRefreshTrigger.value = true
                    codeState.value = MembershipEmailCodeState.Visible.Success
                    delay(500)
                    proceedWithNavigation(MembershipNavigation.Dismiss)
                    proceedWithGettingEmailStatus()
                    // Reset force refresh trigger
                    viewModelScope.launch {
                        delay(FORCE_REFRESH_RESET_DELAY_MS)
                        forceRefreshTrigger.value = false
                    }
                },
                onFailure = { error ->
                    Timber.e("Error verifying email code: $error")
                    setErrorEmailCodeState(error)
                }
            )
        }
    }

    fun onDismissTier() {
        Timber.d("onDismissTier")
        proceedWithHideTier()
        proceedWithNavigation(MembershipNavigation.Dismiss)
    }

    fun onDismissCode() {
        Timber.d("onDismissCode")
        proceedWithNavigation(MembershipNavigation.Dismiss)
        codeState.value = MembershipEmailCodeState.Hidden
    }

    fun onDismissWelcome() {
        Timber.d("onDismissWelcome")
        proceedWithNavigation(MembershipNavigation.Dismiss)
        welcomeState.value = WelcomeState.Hidden
    }

    private fun proceedWithNavigation(navigationCommand: MembershipNavigation) {
        viewModelScope.launch {
            navigation.emit(navigationCommand)
        }
    }

    private fun getTierById(tierId: TierId): Tier? {
        val membershipStatus = (viewState.value as? MembershipMainState.Default) ?: return null
        return membershipStatus.tiers.find { it.id == tierId }
    }

    private fun showError(message: String) {
        errorState.value = MembershipErrorState.Show(message)
    }

    fun hideError() {
        errorState.value = MembershipErrorState.Hidden
    }

    private fun sendAnalyticsClickEvent(buttonType: EventsDictionary.MembershipTierButton) {
        viewModelScope.launch {
            sendAnalyticsMembershipClickEvent(
                analytics = analytics,
                buttonType = buttonType
            )
        }
    }

    //region Google Play Billing
    /**
     * Use the Google Play Billing Library to make a purchase.
     *
     * @param tag String representing tags associated with offers and base plans.
     * @param product Product being purchased.
     *
     */
    private fun buyBasePlans(billingId: String, product: String) {
        Timber.d("buyBasePlans: billingId:$billingId, product:$product")
        //todo check if the user has already purchased the product
        val isProductOnServer = false//serverHasSubscription(subscriptions.value, product)
        val billingPurchaseState = billingPurchases.value
        val isProductOnDevice = if (billingPurchaseState is BillingPurchaseState.HasPurchases) {
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
            (billingProducts.value as? BillingClientState.Connected)?.productDetails?.firstOrNull { it.productId == product }
                ?: run {
                    Timber.e("Could not find Basic product details by product id: $product")
                    errorState.value = MembershipErrorState.Show("Could not find Basic product details by product id: $product")
                    return
                }

        val offerToken: String? = builderSubProductDetails.subscriptionOfferDetails?.let { leastPricedOfferToken(it) }
        if (offerToken.isNullOrEmpty()) {
            Timber.e("Offer token for subscription is null or empty")
            errorState.value = MembershipErrorState.Show("Offer token for subscription is null or empty, couldn't proceed")
            return
        }
        launchFlow(
            billingId = billingId,
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
        var offerToken = ""
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
    ) : BillingFlowParams? {
        try {
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
        } catch (e: Exception) {
            showError(e.message ?: "Unknown error")
            return null
        }
    }

    /**
     * Launches the billing flow for a subscription product purchase.
     * A user can only have one subscription purchase on the device at a time. If the user
     * has more than one subscription purchase on the device, the app should not allow the
     * user to purchase another subscription.
     *
     * @param offerToken String representing the offer token of the lowest priced offer.
     * @param productDetails ProductDetails of the product being purchased.
     *
     */
    private fun launchFlow(
        billingId: String,
        offerToken: String,
        productDetails: ProductDetails
    ) {
        val billingPurchaseState = billingPurchases.value
        if (billingPurchaseState is BillingPurchaseState.HasPurchases && billingPurchaseState.purchases.size > EXPECTED_SUBSCRIPTION_PURCHASE_LIST_SIZE) {
            errorState.value = MembershipErrorState.Show("There are more than one subscription purchases on the device.")
            Timber.e("There are more than one subscription purchases on the device.")
            return

            TODO(
                "Handle this case better, such as by showing a dialog to the user or by " +
                        "programmatically getting the correct purchase token."
            )
        }

        viewModelScope.launch {
            val billingParams: BillingFlowParams? = billingFlowParamsBuilder(
                productDetails = productDetails,
                offerToken = offerToken,
                billingId = billingId
            )
            if (billingParams == null) {
                Timber.e("Billing params is null")
                errorState.value = MembershipErrorState.Show("Billing params is empty, couldn't proceed")
            } else {
                _launchBillingCommand.emit(billingParams)
            }
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
    private fun deviceHasGooglePlaySubscription(purchases: List<MembershipPurchase>?, product: String) =
        purchaseForProduct(purchases, product) != null

    /**
     * Return purchase for the provided Product, if it exists.
     */
    private fun purchaseForProduct(purchases: List<MembershipPurchase>?, product: String): MembershipPurchase? {
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
        const val FORCE_REFRESH_RESET_DELAY_MS = 1000L
        const val EXPECTED_SUBSCRIPTION_PURCHASE_LIST_SIZE = 1
        const val NAME_VALIDATION_DELAY = 300L
    }
}

data class MainResult(
    val membershipStatus: MembershipStatus,
    val billingClientState: BillingClientState,
    val purchases: BillingPurchaseState
)