package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.payments.constants.TiersConstants
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierPreviewView
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import junit.framework.TestCase
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test

/**
 * Tier - active and free | without androidId
 * TierPreview = [Title|Subtitle|ConditionInfo.Valid]
 * Tier = [Title|Subtitle|Features|ConditionInfo.Valid|TierEmail|ButtonSubmit ot ButtonChange]
 */
class TierActiveAndFreeTests : MembershipTestsSetup() {

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
                priceStripeUsdCents = 0,
            )
        )
    }

    private fun setupMembershipStatus(
        tiers: List<MembershipTierData>,
        email: String
    ): MembershipStatus {
        return MembershipStatus(
            activeTier = TierId(TiersConstants.EXPLORER_ID),
            status = Membership.Status.STATUS_ACTIVE,
            dateEnds = 0,
            paymentMethod = MembershipPaymentMethod.METHOD_NONE,
            anyName = "",
            tiers = tiers,
            formattedDateEnds = "formattedDateEnds-${RandomString.make()}",
            userEmail = email
        )
    }

    @Test
    fun `when free plan is active, but without email, show email form`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubBilling(billingClientState = BillingClientState.Loading)
            stubMembershipProvider(setupMembershipStatus(tiers, ""))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.EXPLORER_ID }!!
                TestCase.assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Free(TierPeriod.Unlimited),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(TiersConstants.EXPLORER_ID))

            //STATE : EXPLORER, CURRENT, WITHOUT EMAIL
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Free(TierPeriod.Unlimited),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Submit.Disabled,
                    expectedId = TiersConstants.EXPLORER_ID,
                    expectedActive = true,
                    expectedEmailState = TierEmail.Visible.Enter
                )
            }
        }
    }

    @Test
    fun `when free plan is active, with email, don't show email form`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubBilling(billingClientState = BillingClientState.Loading)
            stubMembershipProvider(setupMembershipStatus(tiers, "test@any.io"))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.EXPLORER_ID }!!
                TestCase.assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Free(TierPeriod.Unlimited),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(TiersConstants.EXPLORER_ID))

            //STATE : EXPLORER, CURRENT, WITHOUT EMAIL
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Free(TierPeriod.Unlimited),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Hidden,
                    expectedId = TiersConstants.EXPLORER_ID,
                    expectedActive = true,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}