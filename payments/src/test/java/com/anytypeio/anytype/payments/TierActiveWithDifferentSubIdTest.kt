package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
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
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import kotlin.test.assertIs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class TierActiveWithDifferentSubIdTest : MembershipTestsSetup() {

    /**
     * Test Case: Display of Builder Tier when Subscription with a Different ID is Purchased
     *
     * Objective: Verify that the Builder Tier is available for purchase when a subscription with a different ID has been purchased.
     *
     * Preconditions:
     *
     * 	•	The user has an active subscription with an AccountId that is different from the Session AccountId.
     *
     */

    protected val accountIdDifferent = "accountIdDifferent-${RandomString.make()}"

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
            )
        )
    }

    @Test
    fun `when showing not active tier`() = runTest {
        turbineScope {

            val (features, tiers) = commonTestSetup()

            // Настройка 2-х подписок с разными ID из Google Play

            //первая продакшн подписка, этот id приходит в модели Тира
            val product1 = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails1 =
                listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases1 = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList1 = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product1.productId).thenReturn(androidProductId)
            Mockito.`when`(product1?.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails1)
            Mockito.`when`(subscriptionOfferDetails1[0].pricingPhases).thenReturn(pricingPhases1)
            Mockito.`when`(pricingPhases1?.pricingPhaseList).thenReturn(pricingPhaseList1)
            val formattedPrice1 = "$299"
            Mockito.`when`(pricingPhaseList1[0]?.formattedPrice).thenReturn(formattedPrice1)
            Mockito.`when`(pricingPhaseList1[0]?.billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product1)))

            val purchase = Mockito.mock(Purchase::class.java)
            Mockito.`when`(purchase.products).thenReturn(listOf(androidProductId))
            Mockito.`when`(purchase.isAcknowledged).thenReturn(true)
            val purchaseJson =
                "{\"accountId\":\"$accountIdDifferent\", \"productId\":\"$androidProductId\"}"
            Mockito.`when`(purchase.originalJson).thenReturn(purchaseJson)
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase), false))

            val flow = flow {
                emit(
                    MembershipStatus(
                        activeTier = TierId(MembershipConstants.EXPLORER_ID),
                        status = Membership.Status.STATUS_ACTIVE,
                        dateEnds = 0L,
                        paymentMethod = MembershipPaymentMethod.METHOD_NONE,
                        anyName = "",
                        tiers = tiers,
                        formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                    )
                )
            }
            membershipProvider.stub {
                onBlocking { status() } doReturn flow
            }

            val validPeriod = TierPeriod.Year(1)

            val viewModel = buildViewModel()

            val mainStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            val firstMainItem = mainStateFlow.awaitItem()
            assertIs<MembershipMainState.Loading>(firstMainItem)
            val firstTierItem = tierStateFlow.awaitItem()
            assertIs<MembershipTierState.Hidden>(firstTierItem)

            val secondMainItem = mainStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(secondMainItem)

            delay(200)
            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            advanceUntilIdle()

            val expectedConditionInfo = TierConditionInfo.Visible.PriceBilling(
                BillingPriceInfo(
                    formattedPrice = formattedPrice1,
                    period = PeriodDescription(
                        amount = 1,
                        unit = PeriodUnit.YEARS
                    )
                )
            )
            val secondTierItem = tierStateFlow.awaitItem()
            secondTierItem.let {
                assertIs<MembershipTierState.Visible>(secondTierItem)
                validateTierView(
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.HiddenWithText.DifferentPurchaseAccountId,
                    tier = secondTierItem.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}