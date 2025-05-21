package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.membership.MembershipConstants
import com.anytypeio.anytype.payments.models.MembershipPurchase
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierPreview
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import junit.framework.TestCase
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test

class TierAndroidBillingUnavailableTest : MembershipTestsSetup() {

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
                priceStripeUsdCents = 9901,
                androidManageUrl = "https://anytype.io/pricing"
            ),
            StubMembershipTierData(
                id = MembershipConstants.CO_CREATOR_ID,
                androidProductId = null,
                features = features,
                periodValue = 3,
                periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                priceStripeUsdCents = 29900
            )
        )
    }

    private fun setupMembershipStatus(tiers: List<MembershipTierData>): MembershipStatus {
        return MembershipStatus(
            activeTier = TierId(MembershipConstants.STARTER_ID),
            status = Membership.Status.STATUS_ACTIVE,
            dateEnds = 0,
            paymentMethod = MembershipPaymentMethod.METHOD_NONE,
            anyName = "",
            tiers = tiers,
            formattedDateEnds = ""
        )
    }

    /**
     * Tier - not active and non free | with androidId (Builder) | billing library unavailable
     * TierPreview = [Title|Subtitle|ConditionInfo.Price]
     * Tier = [Title|Subtitle|Features|ConditionInfo.Price|ButtonInfo]
     */
    @Test
    fun `test billing unavailable`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubMembershipProvider(setupMembershipStatus(tiers))
            stubPurchaseState(purchaseState = BillingPurchaseState.Loading)
            stubBilling(billingClientState = BillingClientState.NotAvailable)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val conditionInfo = TierConditionInfo.Visible.Price(
                price = "$99.01",
                period = TierPeriod.Year(1)
            )

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                TestCase.assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(false, tier.isActive)
                TestCase.assertEquals(conditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT ACTIVE, BILLING UNAVAILABLE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = conditionInfo,
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Info.Enabled("https://anytype.io/pricing"),
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    /**
     * Tier - not active and non free | with androidId (Builder) | billing library unavailable | purchase is exist
     * TierPreview = [Title|Subtitle|ConditionInfo.Price]
     * Tier = [Title|Subtitle|Features|ConditionInfo.Price|ButtonInfo]
     */
    @Test
    fun `test billing unavailable, but purchase is exist`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            val purchase = MembershipPurchase(accountId, listOf(androidProductId),
                MembershipPurchase.PurchaseState.PURCHASED)
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase), false))

            stubMembershipProvider(setupMembershipStatus(tiers))
            stubBilling(billingClientState = BillingClientState.NotAvailable)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val conditionInfo = TierConditionInfo.Visible.Price(
                price = "$99.01",
                period = TierPeriod.Year(1)
            )

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview = result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                TestCase.assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(false, tier.isActive)
                TestCase.assertEquals(conditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, NOT ACTIVE, BILLING UNAVAILABLE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tier = result.tier,
                    expectedFeatures = features,
                    expectedConditionInfo = conditionInfo,
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Info.Enabled("https://anytype.io/pricing"),
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}