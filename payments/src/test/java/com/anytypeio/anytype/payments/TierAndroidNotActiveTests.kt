package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.payments.constants.MembershipConstants
import com.anytypeio.anytype.payments.models.BillingPriceInfo
import com.anytypeio.anytype.payments.models.PeriodDescription
import com.anytypeio.anytype.payments.models.PeriodUnit
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPreview
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import junit.framework.TestCase.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.Mockito

/**
 * Tests for the not active tier with possible android subscription
 *
 * TierPreview = [Title|Subtitle|ConditionInfo.Price]
 * Tier = [Title|Subtitle|Features|AnyName|ConditionInfo.Price|ButtonPay]
 *
 * TierPreview has same fields as Tier except for Features, AnyName, ButtonState
 */
class TierAndroidNotActiveTests : MembershipTestsSetup() {

    private fun commonTestSetup(): Pair<List<String>, List<MembershipTierData>> {
        val features = listOf("feature-${RandomString.make()}", "feature-${RandomString.make()}")
        val tiers = setupTierData(features)
        return Pair(features, tiers)
    }

    private fun setupTierData(features: List<String>): List<MembershipTierData> {
        return listOf(
            StubMembershipTierData(
                id = MembershipConstants.EXPLORER_ID,
                androidProductId = null,
                features = features,
                periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                priceStripeUsdCents = 0
            ),
            StubMembershipTierData(
                id = MembershipConstants.BUILDER_ID,
                androidProductId = androidProductId,
                features = features,
                periodValue = 1,
                periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                priceStripeUsdCents = 9900
            ),
            StubMembershipTierData(
                id = MembershipConstants.CO_CREATOR_ID,
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

    private fun setupMembershipStatus(
        tiers: List<MembershipTierData>,
        anyName: String = "",
        status : Membership.Status = Membership.Status.STATUS_ACTIVE
    ): MembershipStatus {
        return MembershipStatus(
            activeTier = TierId(MembershipConstants.EXPLORER_ID),
            status = status,
            dateEnds = 1714199910,
            paymentMethod = MembershipPaymentMethod.METHOD_NONE,
            anyName = anyName,
            tiers = tiers,
            formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
        )
    }

    @Test
    fun `test loading billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubBilling(billingClientState = BillingClientState.Loading)
            stubMembershipProvider(setupMembershipStatus(tiers))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(TierConditionInfo.Visible.LoadingBillingClient, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, LOADING BILLING
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.LoadingBillingClient,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
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
            stubPurchaseState()
            stubBilling(billingClientState = BillingClientState.Error(errorMessage))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(TierConditionInfo.Visible.Error(errorMessage), tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, ERROR BILLING
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Error(errorMessage),
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test error product billing state when price is empty`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubMembershipProvider(setupMembershipStatus(tiers))
            stubPurchaseState()

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

            val expectedConditionInfo = TierConditionInfo.Visible.Error(MembershipConstants.ERROR_PRODUCT_PRICE)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, PRICE IS EMPTY
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test error product billing state when product not found`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubMembershipProvider(setupMembershipStatus(tiers))
            stubPurchaseState()

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

            val expectedConditionInfo = TierConditionInfo.Visible.Error(MembershipConstants.ERROR_PRODUCT_NOT_FOUND)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, PRODUCT NOT FOUND
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test success product billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
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
            Mockito.`when`(pricingPhaseList[0]?.billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.PriceBilling(
                BillingPriceInfo(
                    formattedPrice = formattedPrice,
                    period = PeriodDescription(amount = 1, unit = PeriodUnit.YEARS)
                )
            )

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, CORRECT PRICE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Enter,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test unsuccessful product billing state, when billing period is wrong`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
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
            Mockito.`when`(pricingPhaseList[0]?.billingPeriod).thenReturn("errorBillingPeriod")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.Error(MembershipConstants.ERROR_PRODUCT_PRICE)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, INCORRECT BILLING PERIOD
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test unsuccessful product billing state, when billing price is wrong`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubMembershipProvider(setupMembershipStatus(tiers))

            val product = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails = listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product.productId).thenReturn(androidProductId)
            Mockito.`when`(product?.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails)
            Mockito.`when`(subscriptionOfferDetails[0].pricingPhases).thenReturn(pricingPhases)
            Mockito.`when`(pricingPhases?.pricingPhaseList).thenReturn(pricingPhaseList)
            val formattedPrice = null // You can set any desired formatted price here
            Mockito.`when`(pricingPhaseList[0]?.formattedPrice).thenReturn(formattedPrice)
            Mockito.`when`(pricingPhaseList[0]?.billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.Error(MembershipConstants.ERROR_PRODUCT_PRICE)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, INCORRECT BILLING PRICE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Disabled,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `when any name already purchased should show it`() = runTest {
        val anyName = "anyName-${RandomString.make()}"
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubMembershipProvider(
                setupMembershipStatus(
                    tiers = tiers, anyName = anyName
                )
            )

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
            Mockito.`when`(pricingPhaseList[0]?.billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.PriceBilling(
                BillingPriceInfo(
                    formattedPrice = formattedPrice,
                    period = PeriodDescription(amount = 1, unit = PeriodUnit.YEARS)
                )
            )

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, HAS PURCHASED NAME
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Purchased(anyName),
                    expectedButtonState = TierButton.Pay.Enabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test pending state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubMembershipProvider(
                setupMembershipStatus(
                    tiers = tiers,
                    status = Membership.Status.STATUS_PENDING
                )
            )

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
            Mockito.`when`(pricingPhaseList[0]?.billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product)))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val expectedConditionInfo = TierConditionInfo.Visible.Pending

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                assertEquals(false, tier.isActive)
                assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, CORRECT PRICE, MEMBERSHIP PENDING
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}