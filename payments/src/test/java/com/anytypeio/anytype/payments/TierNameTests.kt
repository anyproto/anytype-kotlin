package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.payments.constants.TiersConstants
import com.anytypeio.anytype.payments.models.BillingPriceInfo
import com.anytypeio.anytype.payments.models.PeriodDescription
import com.anytypeio.anytype.payments.models.PeriodUnit
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPreviewView
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.payments.viewmodel.TierAction
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import junit.framework.TestCase
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.mockito.Mockito
import org.mockito.kotlin.verifyNoInteractions

class TierNameTests : MembershipTestsSetup() {

    private val builderTierId = TierId(TiersConstants.BUILDER_ID)

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

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `test naming chain`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            stubPurchaseState()
            stubMembershipProvider(setupMembershipStatus(tiers))

            val product = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails =
                listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
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
                val tier: TierPreviewView =
                    result.tiers.find { it.id.value == TiersConstants.BUILDER_ID }!!
                TestCase.assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
                TestCase.assertEquals(false, tier.isActive)
                TestCase.assertEquals(expectedConditionInfo, tier.conditionInfo)
            }

            viewModel.onTierClicked(builderTierId)

            //STATE : BUILDER, NOT CURRENT, BUILDER PRODUCT, CORRECT PRICE
            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Enter,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }

            //START TO ENTER ANY NAME
            viewModel.onTierAction(action = TierAction.UpdateName(builderTierId, "a"))

            tierStateFlow.expectNoEvents()

            verifyNoInteractions(isMembershipNameValid)

            //UPDATE NAME TO 6 CHARACTERS
            viewModel.onTierAction(action = TierAction.UpdateName(builderTierId, "anyNam"))

            tierStateFlow.ensureAllEventsConsumed()

            verifyNoInteractions(isMembershipNameValid)

            //UPDATE NAME TO 7 CHARACTERS
            Mockito.`when`(
                isMembershipNameValid.async(
                    IsMembershipNameValid.Params(
                        TiersConstants.BUILDER_ID,
                        "anyName"
                    )
                )
            )
                .thenReturn(
                    Resultat.failure(MembershipErrors.IsNameValid.HasInvalidChars("has invalid chars"))
                )

            viewModel.onTierAction(action = TierAction.UpdateName(builderTierId, "anyName"))

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Validating,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Error("has invalid chars"),
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }

            //UPDATE NAME TO 6 CHARACTERS

            viewModel.onTierAction(action = TierAction.UpdateName(builderTierId, "anyNam"))

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Enter,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }

            //UPDATE NAME TO 8 CHARACTERS
            Mockito.`when`(
                isMembershipNameValid.async(
                    IsMembershipNameValid.Params(
                        TiersConstants.BUILDER_ID,
                        "anyNamee"
                    )
                )
            )
                .thenReturn(
                    Resultat.success(Unit)
                )

            viewModel.onTierAction(action = TierAction.UpdateName(builderTierId, "anyNamee"))

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Validating,
                    expectedButtonState = TierButton.Pay.Disabled,
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }

            tierStateFlow.awaitItem().let { result ->
                assertIs<MembershipTierState.Visible>(result)
                validateTierView(
                    tierView = result.tierView,
                    expectedFeatures = features,
                    expectedConditionInfo = expectedConditionInfo,
                    expectedAnyName = TierAnyName.Visible.Validated("anyNamee"),
                    expectedButtonState = TierButton.Pay.Enabled,
                    expectedId = TiersConstants.BUILDER_ID,
                    expectedActive = false,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}