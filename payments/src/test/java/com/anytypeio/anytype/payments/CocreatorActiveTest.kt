package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.membership.MembershipConstants
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
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import junit.framework.TestCase
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.Mockito

class CocreatorActiveTest : MembershipTestsSetup() {

    private fun commonTestSetup(): Pair<List<String>, List<MembershipTierData>> {
        val features = listOf("feature-${RandomString.make()}", "feature-${RandomString.make()}")
        val tiers = setupTierData(features)
        return Pair(features, tiers)
    }

    private fun setupTierData(features: List<String>): List<MembershipTierData> {
        return listOf(
            StubMembershipTierData(
                id = MembershipConstants.STARTER_ID,
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
            activeTier = TierId(MembershipConstants.CO_CREATOR_ID),
            status = status,
            dateEnds = 1714199910,
            paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
            anyName = anyName,
            tiers = tiers,
            formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
        )
    }

    @Test
    fun `when co-creator is active`() = runTest {
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
                TestCase.assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(false, tier.isActive)
                TestCase.assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Hidden,
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}