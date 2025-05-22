package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.membership.MembershipConstants.BUILDER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.STARTER_ID
import com.anytypeio.anytype.payments.models.BillingPriceInfo
import com.anytypeio.anytype.payments.models.MembershipPurchase
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
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class TierBuilderFallbackOnExplorerTest : MembershipTestsSetup() {

    private val dateEnds = 1714199910L

    private fun commonTestSetup(): Pair<List<String>, List<MembershipTierData>> {
        val features = listOf("feature-${RandomString.make()}", "feature-${RandomString.make()}")
        val tiers = setupTierData(features)
        return Pair(features, tiers)
    }

    private fun setupTierData(features: List<String>): List<MembershipTierData> {
        return listOf(
            StubMembershipTierData(
                id = STARTER_ID,
                androidProductId = null,
                features = features,
                periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                priceStripeUsdCents = 0
            ),
            StubMembershipTierData(
                id = BUILDER_ID,
                androidProductId = androidProductId,
                features = features,
                periodValue = 1,
                periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                priceStripeUsdCents = 9900
            )
        )
    }

    @Test
    fun `when updating active tier from builder to explorer`() = runTest {
        turbineScope {

            val (features, tiers) = commonTestSetup()

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


            val purchase = MembershipPurchase(accountId, listOf(androidProductId), MembershipPurchase.PurchaseState.PURCHASED)
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase), false))
            val flow = flow {
                emit(
                    MembershipStatus(
                        activeTier = TierId(BUILDER_ID),
                        status = Membership.Status.STATUS_ACTIVE,
                        dateEnds = dateEnds,
                        paymentMethod = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        anyName = "TestAnyName",
                        tiers = tiers,
                        formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                    )
                )
                delay(300)
                emit(
                    MembershipStatus(
                        activeTier = TierId(STARTER_ID),
                        status = Membership.Status.STATUS_ACTIVE,
                        dateEnds = 0L,
                        paymentMethod = MembershipPaymentMethod.METHOD_NONE,
                        anyName = "TestAnyName",
                        tiers = tiers,
                        formattedDateEnds = ""
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

            delay(100)
            viewModel.onTierClicked(TierId(BUILDER_ID))

            val secondMainItem = mainStateFlow.awaitItem()

            secondMainItem.let {
                assertIs<MembershipMainState.Default>(secondMainItem)
                val builderTier2 = secondMainItem.tiers.find { it.id == TierId(BUILDER_ID) }
                val explorerTier2 = secondMainItem.tiers.find { it.id == TierId(STARTER_ID) }
                assertEquals(true, builderTier2?.isActive)
                assertEquals(false, explorerTier2?.isActive)
            }

            val thirdMainItem = mainStateFlow.awaitItem()
            thirdMainItem.let {
                assertIs<MembershipMainState.Default>(thirdMainItem)
                val builderTier3 = thirdMainItem.tiers.find { it.id == TierId(BUILDER_ID) }
                val explorerTier3 = thirdMainItem.tiers.find { it.id == TierId(STARTER_ID) }
                assertEquals(false, builderTier3?.isActive)
                assertEquals(true, explorerTier3?.isActive)
            }

            val secondTierItem = tierStateFlow.awaitItem()
            secondTierItem.let {
                assertIs<MembershipTierState.Visible>(secondTierItem)
                validateTierView(
                    expectedId = BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Manage.Android.Enabled(productId = androidProductId),
                    tier = secondTierItem.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }

            val expectedConditionInfo = TierConditionInfo.Visible.PriceBilling(
                BillingPriceInfo(
                    formattedPrice = formattedPrice,
                    period = PeriodDescription(
                        amount = 1,
                        unit = PeriodUnit.YEARS
                    )
                )
            )

            val thirdTierItem = tierStateFlow.awaitItem()
            thirdTierItem.let {
                assertIs<MembershipTierState.Visible>(thirdTierItem)
                validateTierView(
                    expectedId = BUILDER_ID,
                    expectedActive = false,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Hidden,
                    tier = thirdTierItem.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}