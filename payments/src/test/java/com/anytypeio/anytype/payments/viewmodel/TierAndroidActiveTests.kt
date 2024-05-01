package com.anytypeio.anytype.payments.viewmodel

import app.cash.turbine.turbineScope
import com.android.billingclient.api.Purchase
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
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipTestsSetup
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import junit.framework.TestCase
import kotlin.random.Random
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.Mockito

/**
 * Tests for the active tier with active android subscription
 * TierPreview = [Title|Subtitle|ConditionInfo.Valid]
 * Tier = [Title|Subtitle|Features|ConditionInfo.Valid|ButtonManage]
 */
class TierAndroidActiveTests : MembershipTestsSetup() {

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
            activeTier = TierId(TiersConstants.BUILDER_ID),
            status = Membership.Status.STATUS_ACTIVE,
            dateEnds = 1714199910,
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
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                TestCase.assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        period = validPeriod,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, CURRENT, LOADING PURCHASES
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        period = validPeriod,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Manage.Android.Disabled,
                    tierView = result.tierView,
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
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                TestCase.assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        period = validPeriod,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, CURRENT, EMPTY PURCHASES, NOTHING TO MANAGE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        period = validPeriod,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Manage.Android.Disabled,
                    tierView = result.tierView,
                )
            }
        }
    }

    @Test
    fun `test success billing state`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubBilling()
            val purchase = Mockito.mock(Purchase::class.java)
            Mockito.`when`(purchase.products).thenReturn(listOf(androidProductId))
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase)))
            stubMembershipProvider(setupMembershipStatus(tiers))

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())

            val validPeriod = TierPeriod.Year(1)

            viewStateFlow.awaitItem().let { result ->
                assertIs<MembershipMainState.Default>(result)
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                TestCase.assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(true, tier.isActive)
                TestCase.assertEquals(
                    TierConditionInfo.Visible.Valid(
                        period = validPeriod,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                    ),
                    tier.conditionInfo
                )
            }

            viewModel.onTierClicked(TierId(TiersConstants.BUILDER_ID))

            //STATE : BUILDER, CURRENT, PURCHASE SUCCESS
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = true,
                    expectedFeatures = features,
                    expectedConditionInfo = TierConditionInfo.Visible.Valid(
                        period = validPeriod,
                        payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                    ),
                    expectedAnyName = TierAnyName.Hidden,
                    expectedButtonState = TierButton.Manage.Android.Enabled(androidProductId),
                    tierView = result.tierView,
                )
            }
        }
    }
}

fun StubMembershipTierData(
    id: Int = Random.nextInt(),
    name: String = "name-${RandomString.make()}",
    description: String = "description-${RandomString.make()}",
    isTest: Boolean = false,
    periodType: MembershipPeriodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
    periodValue: Int = 0,
    priceStripeUsdCents: Int = 0,
    anyNamesCountIncluded: Int = Random.nextInt(),
    anyNameMinLength: Int = Random.nextInt(),
    features: List<String> = emptyList(),
    colorStr: String = "colorStr-${RandomString.make()}",
    stripeProductId: String? = null,
    stripeManageUrl: String? = null,
    iosProductId: String? = null,
    iosManageUrl: String? = null,
    androidProductId: String? = null,
    androidManageUrl: String? = null
): MembershipTierData = MembershipTierData(
    id = id,
    name = name,
    description = description,
    isTest = isTest,
    periodType = periodType,
    periodValue = periodValue,
    priceStripeUsdCents = priceStripeUsdCents,
    anyNamesCountIncluded = anyNamesCountIncluded,
    anyNameMinLength = anyNameMinLength,
    features = features,
    colorStr = colorStr,
    stripeProductId = stripeProductId,
    stripeManageUrl = stripeManageUrl,
    iosProductId = iosProductId,
    iosManageUrl = iosManageUrl,
    androidProductId = androidProductId,
    androidManageUrl = androidManageUrl
)