package com.anytypeio.anytype.payments.viewmodel

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.payments.constants.TiersConstants
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierPreviewView
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import junit.framework.TestCase.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.Mockito

class TierPayStateTests : MembershipTestsSetup() {

    private fun commonTestSetup(): Pair<List<String>, List<MembershipTierData>> {
        val features = listOf("feature-${RandomString.make()}", "feature-${RandomString.make()}")
        val tiers = setupTierData(features)
        return Pair(features, tiers)
    }

    private fun setupTierData(features: List<String>): List<MembershipTierData> {
        return listOf(
            StubMembershipTierData(
                id = TiersConstants.EXPLORER_ID,
                androidProductId = null,
                features = features,
                periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                priceStripeUsdCents = 0
            ),
            StubMembershipTierData(
                id = TiersConstants.BUILDER_ID,
                androidProductId = androidProductId,
                features = features,
                periodValue = 1,
                periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                priceStripeUsdCents = 9900
            ),
            StubMembershipTierData(
                id = TiersConstants.CO_CREATOR_ID,
                androidProductId = null,
                features = features,
                periodValue = 3,
                periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                priceStripeUsdCents = 29900
            ),
            StubMembershipTierData(
                id = 22,
                androidProductId = null,
                features = features,
                periodValue = 1,
                periodType = MembershipPeriodType.PERIOD_TYPE_MONTHS,
                priceStripeUsdCents = 1000
            )
        )
    }

    private fun setupMembershipStatus(tiers: List<MembershipTierData>): MembershipStatus {
        return MembershipStatus(
            activeTier = TierId(TiersConstants.EXPLORER_ID),
            status = Membership.Status.STATUS_ACTIVE,
            dateEnds = 1714199910,
            paymentMethod = MembershipPaymentMethod.METHOD_NONE,
            anyName = "",
            tiers = tiers,
            formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
        )
    }

    @Test
    fun `test loading billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubBilling(billingClientState = BillingClientState.Loading)
            stubMembershipProvider(setupMembershipStatus(tiers))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView = result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isCurrent)
                assertEquals(TierConditionInfo.Visible.LoadingBillingClient, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, LOADING BILLING
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.LoadingBillingClient,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled
                )
            }
        }
    }

    @Test
    fun `test error billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()
            val errorMessage = "error-${RandomString.make()}"

            stubMembershipProvider(setupMembershipStatus(tiers))
            stubBilling(billingClientState = BillingClientState.Error(errorMessage))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView = result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isCurrent)
                assertEquals(TierConditionInfo.Visible.Error(errorMessage), tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, ERROR BILLING
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Error(errorMessage),
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled
                )
            }
        }
    }

    @Test
    fun `test error product billing state when price is empty`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubMembershipProvider(setupMembershipStatus(tiers))

            val product = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails = listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product.productId).thenReturn(androidProductId)
            Mockito.`when`(product?.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails)
            Mockito.`when`(subscriptionOfferDetails[0].pricingPhases).thenReturn(pricingPhases)
            Mockito.`when`(pricingPhases?.pricingPhaseList).thenReturn(pricingPhaseList)
            val formattedPrice = "" // You can set any desired formatted price here
            Mockito.`when`(pricingPhaseList[0]?.formattedPrice).thenReturn(formattedPrice)
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.Error(TiersConstants.ERROR_PRODUCT_PRICE)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView = result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isCurrent)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, PRICE IS EMPTY
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled
                )
            }
        }
    }

    @Test
    fun `test error product billing state when product not found`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubMembershipProvider(setupMembershipStatus(tiers))

            val product = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails = listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product.productId).thenReturn(RandomString.make())
            Mockito.`when`(product?.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails)
            Mockito.`when`(subscriptionOfferDetails[0].pricingPhases).thenReturn(pricingPhases)
            Mockito.`when`(pricingPhases?.pricingPhaseList).thenReturn(pricingPhaseList)
            val formattedPrice = "" // You can set any desired formatted price here
            Mockito.`when`(pricingPhaseList[0]?.formattedPrice).thenReturn(formattedPrice)
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.Error(TiersConstants.ERROR_PRODUCT_NOT_FOUND)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView = result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isCurrent)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, PRODUCT NOT FOUND
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled
                )
            }
        }
    }

    @Test
    fun `test success product billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubMembershipProvider(setupMembershipStatus(tiers))

            val product = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails = listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product.productId).thenReturn(androidProductId)
            Mockito.`when`(product?.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails)
            Mockito.`when`(subscriptionOfferDetails[0].pricingPhases).thenReturn(pricingPhases)
            Mockito.`when`(pricingPhases?.pricingPhaseList).thenReturn(pricingPhaseList)
            val formattedPrice = "$9.99" // You can set any desired formatted price here
            Mockito.`when`(pricingPhaseList[0]?.formattedPrice).thenReturn(formattedPrice)
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.Price(formattedPrice, TierPeriod.Year(1))

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView = result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isCurrent)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, CORRECT PRICE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Enter,
                    expectedButtonState = TierButton.Pay.Disabled
                )
            }
        }
    }
}