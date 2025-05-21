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

/**
 * Tier - active and non free | with androidId | purchased through Android
 * TierPreview = [Title|Subtitle|ConditionInfo.Valid]
 * Tier = [Title|Subtitle|Features|ConditionInfo.Valid|ButtonManage]
 */
class TierAndroidActiveTests : MembershipTestsSetup() {

    private val dateEnds = 1714199910L

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

    private fun setupMembershipStatus(tiers: List<MembershipTierData>): MembershipStatus {
        return MembershipStatus(
            activeTier = TierId(MembershipConstants.BUILDER_ID),
            status = Membership.Status.STATUS_ACTIVE,
            dateEnds = dateEnds,
            paymentMethod = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
            anyName = "TestAnyName",
            tiers = tiers,
            formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
        )
    }

    @Test
    fun `test loading billing purchase state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)
            stubMembershipProvider(setupMembershipStatus(tiers))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val validPeriod = TierPeriod.Year(1)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview =
                    result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                TestCase.assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, CURRENT, LOADING PURCHASES
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.HiddenWithText.ManageOnAnotherAccount,
                    tier = result.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test empty billing purchase state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubBilling()
            stubPurchaseState(BillingPurchaseState.NoPurchases)
            stubMembershipProvider(setupMembershipStatus(tiers))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val validPeriod = TierPeriod.Year(1)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview =
                    result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                TestCase.assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, CURRENT, EMPTY PURCHASES, NOTHING TO MANAGE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.HiddenWithText.ManageOnAnotherAccount,
                    tier = result.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `test success billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubBilling()
            val purchase = MembershipPurchase(accountId, listOf(androidProductId), MembershipPurchase.PurchaseState.PURCHASED)
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase), false))
            stubMembershipProvider(setupMembershipStatus(tiers))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val validPeriod = TierPeriod.Year(1)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreview =
                    result.tiersPreview.find { it.id.value == MembershipConstants.BUILDER_ID }!!
                TestCase.assertEquals(MembershipConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            //STATE : BUILDER, CURRENT, PURCHASE SUCCESS
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    expectedId = MembershipConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                        period = validPeriod
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Manage.Android.Enabled(androidProductId),
                    tier = result.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}