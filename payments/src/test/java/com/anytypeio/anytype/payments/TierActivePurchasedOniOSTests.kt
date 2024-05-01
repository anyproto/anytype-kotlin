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
 * Tier - active and non free | without androidId | purchased through ios
 *
 */
class TierActivePurchasedOniOSTests : MembershipTestsSetup() {

    // Date when the membership ends
    private val dateEnds = 1714199910L
    // URL for managing iOS payments
    private val iosManageUrl = "iosManageUrl-${RandomString.make()}"

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
                features = features,
                periodValue = 1,
                periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                priceStripeUsdCents = 9900,
                iosManageUrl = iosManageUrl
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
            activeTier = TierId(TiersConstants.BUILDER_ID),
            status = Membership.Status.STATUS_ACTIVE,
            dateEnds = dateEnds,
            paymentMethod = MembershipPaymentMethod.METHOD_INAPP_APPLE,
            anyName = "TestAnyName",
            tiers = tiers,
            formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
        )
    }

    @Test
    fun `when payed plan is active, show proper valid and enabled manage button`() = runTest {
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
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                TestCase.assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_APPLE,
                        period = TierPeriod.Year(1),
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        dateEnds = dateEnds,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_APPLE,
                        period = TierPeriod.Year(1),
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Manage.External.Enabled(iosManageUrl),
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `when payed plan is active from crypto, show proper valid and disabled manage button`() =
        runTest {
            turbineScope {
                val (features, tiers) = commonTestSetup()

                stubPurchaseState()
                stubBilling(billingClientState = BillingClientState.Loading)
                stubMembershipProvider(
                    MembershipStatus(
                        activeTier = TierId(TiersConstants.BUILDER_ID),
                        status = Membership.Status.STATUS_ACTIVE,
                        dateEnds = dateEnds,
                        paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                        anyName = "TestAnyName",
                        tiers = tiers,
                        formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                    )
                )

                val viewModel = buildViewModel()
                val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
                val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

                assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
                assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

                viewStateFlow.awaitItem().let { result ->
                    assertIs<MembershipMainState.Default>(result)
                    val tier: TierPreviewView =
                        result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                    TestCase.assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                    TestCase.assertEquals(true, tier.isActive)
                    TestCase.assertEquals(
                        TierConditionInfo.Visible.Valid(
                            dateEnds = dateEnds,
                            payedBy = MembershipPaymentMethod.METHOD_CRYPTO,
                            period = TierPeriod.Year(1),
                        ),
                        tier.conditionInfo
                    )
                }

                viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

                tierStateFlow.awaitItem().let { result ->
                    assertIs<MembershipTierState.Visible>(result)
                    validateTierView(
                        tierView = result.tierView,
                        expectedFeatures = features,
                        expectedConditionInfo = TierConditionInfo.Visible.Valid(
                            dateEnds = dateEnds,
                            payedBy = MembershipPaymentMethod.METHOD_CRYPTO,
                            period = TierPeriod.Year(1),
                        ),
                        expectedAnyName = TierAnyName.Hidden,
                        expectedButtonState = TierButton.Manage.External.Disabled,
                        expectedId = TiersConstants.BUILDER_ID,
                        expectedActive = true,
                        expectedEmailState = TierEmail.Hidden
                    )
                }
            }
        }
}