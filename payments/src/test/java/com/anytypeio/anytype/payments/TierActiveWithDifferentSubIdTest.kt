package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.membership.MembershipConstants
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

    // Randomly generated account ID for testing
    protected val accountIdDifferent = "accountIdDifferent-${RandomString.make()}"

    // Common test setup function to generate features and tiers
    private fun commonTestSetup(): Pair<List<String>, List<MembershipTierData>> {
        val features = listOf("feature-${RandomString.make()}", "feature-${RandomString.make()}")
        val tiers = setupTierData(features)
        return Pair(features, tiers)
    }

    // Setup tier data with predefined features
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
            )
        )
    }

    /**
     * Test Case: Display of Builder Tier when Subscription with a Different ID is Purchased
     *
     * Objective: Verify that the Builder Tier is NOT available for purchase when a subscription with a different ID has been purchased.
     *
     * Preconditions:
     *  • The user has an active subscription with an AccountId that is different from the Session AccountId.
     */
    @Test
    fun `when subscription with different AccountId is active`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            // Setup for two subscriptions with different IDs from Google Play

            // First production subscription, this ID is used in the Tier model
            val product1 = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails1 =
                listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases1 = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList1 = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product1.productId).thenReturn(androidProductId)
            Mockito.`when`(product1.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails1)
            Mockito.`when`(subscriptionOfferDetails1[0].pricingPhases).thenReturn(pricingPhases1)
            Mockito.`when`(pricingPhases1.pricingPhaseList).thenReturn(pricingPhaseList1)
            val formattedPrice1 = "$299"
            Mockito.`when`(pricingPhaseList1[0].formattedPrice).thenReturn(formattedPrice1)
            Mockito.`when`(pricingPhaseList1[0].billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product1)))

            // Mocking purchase
            val purchase = MembershipPurchase(accountIdDifferent, listOf(androidProductId), MembershipPurchase.PurchaseState.PURCHASED)
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase), false))

            // Mocking the flow of membership status
            val flow = flow {
                emit(
                    MembershipStatus(
                        activeTier = TierId(MembershipConstants.STARTER_ID),
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

            // Testing initial state and tier visibility
            val mainStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)

            val firstMainItem = mainStateFlow.awaitItem()
            assertIs<MembershipMainState.Loading>(firstMainItem)
            val firstTierItem = tierStateFlow.awaitItem()
            assertIs<MembershipTierState.Hidden>(firstTierItem)

            val secondMainItem = mainStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(secondMainItem)

            // Simulate user clicking on the Builder Tier
            delay(200)
            viewModel.onTierClicked(TierId(MembershipConstants.BUILDER_ID))

            advanceUntilIdle()

            // Verify that the Builder Tier is shown as available for purchase with the correct price and billing period
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

    @Test
    fun `when subscription with different ProductId is active`() =
        runTest {
            turbineScope {
                val (features, tiers) = commonTestSetup()

                val product1 = Mockito.mock(ProductDetails::class.java)
                val subscriptionOfferDetails1 =
                    listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
                val pricingPhases1 = Mockito.mock(ProductDetails.PricingPhases::class.java)
                val pricingPhaseList1 =
                    listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

                Mockito.`when`(product1.productId).thenReturn(androidProductId)
                Mockito.`when`(product1?.subscriptionOfferDetails)
                    .thenReturn(subscriptionOfferDetails1)
                Mockito.`when`(subscriptionOfferDetails1[0].pricingPhases)
                    .thenReturn(pricingPhases1)
                Mockito.`when`(pricingPhases1?.pricingPhaseList).thenReturn(pricingPhaseList1)
                val formattedPrice1 = "$999"
                Mockito.`when`(pricingPhaseList1[0]?.formattedPrice).thenReturn(formattedPrice1)
                Mockito.`when`(pricingPhaseList1[0]?.billingPeriod).thenReturn("P1Y")

                //вторая купленная на другой аккаунт подписка, этот id НЕ приходит в модели Тира
                val product2 = Mockito.mock(ProductDetails::class.java)
                val subscriptionOfferDetails2 =
                    listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
                val pricingPhases2 = Mockito.mock(ProductDetails.PricingPhases::class.java)
                val pricingPhaseList2 =
                    listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

                // Mock active subscription with an invalid ID
                val invalidProductId = "invalidProductId-${RandomString.make()}"
                Mockito.`when`(product2.productId).thenReturn(invalidProductId)
                Mockito.`when`(product2?.subscriptionOfferDetails)
                    .thenReturn(subscriptionOfferDetails2)
                Mockito.`when`(subscriptionOfferDetails2[0].pricingPhases)
                    .thenReturn(pricingPhases2)
                Mockito.`when`(pricingPhases2?.pricingPhaseList).thenReturn(pricingPhaseList2)
                val formattedPrice2 = "$111"
                Mockito.`when`(pricingPhaseList2[0]?.formattedPrice).thenReturn(formattedPrice2)
                Mockito.`when`(pricingPhaseList2[0]?.billingPeriod).thenReturn("P3Y")
                stubBilling(
                    billingClientState = BillingClientState.Connected(
                        listOf(
                            product2,
                            product1
                        )
                    )
                )

                val purchase = MembershipPurchase(accountId, listOf(invalidProductId), MembershipPurchase.PurchaseState.PURCHASED)
                stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase), false))

                val flow = flow {
                    emit(
                        MembershipStatus(
                            activeTier = TierId(MembershipConstants.STARTER_ID),
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
                        expectedButtonState = TierButton.HiddenWithText.DifferentPurchaseProductId,
                        tier = secondTierItem.tier,
                        expectedEmailState = TierEmail.Hidden
                    )
                }
            }
        }

    /**
     * Verify that the Builder Tier is available for purchase when there is no active subscription.
     */

    @Test
    fun `when no active subscription`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            val product1 = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails1 =
                listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases1 = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList1 = listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product1.productId).thenReturn(androidProductId)
            Mockito.`when`(product1.subscriptionOfferDetails).thenReturn(subscriptionOfferDetails1)
            Mockito.`when`(subscriptionOfferDetails1[0].pricingPhases).thenReturn(pricingPhases1)
            Mockito.`when`(pricingPhases1.pricingPhaseList).thenReturn(pricingPhaseList1)
            val formattedPrice1 = "$299"
            Mockito.`when`(pricingPhaseList1[0].formattedPrice).thenReturn(formattedPrice1)
            Mockito.`when`(pricingPhaseList1[0].billingPeriod).thenReturn("P1Y")
            stubBilling(billingClientState = BillingClientState.Connected(listOf(product1)))

            // Mock no active subscription
            stubPurchaseState(BillingPurchaseState.NoPurchases)

            val flow = flow {
                emit(
                    MembershipStatus(
                        activeTier = TierId(MembershipConstants.STARTER_ID),
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
                    expectedAnyName = TierAnyName.Visible.Enter,
                    expectedButtonState = TierButton.Pay.Disabled,
                    tier = secondTierItem.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }

    @Test
    fun `when there are more then one purchase`() = runTest {
        turbineScope {
            val (features, tiers) = commonTestSetup()

            val product1 = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails1 =
                listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases1 = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList1 =
                listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            Mockito.`when`(product1.productId).thenReturn(androidProductId)
            Mockito.`when`(product1?.subscriptionOfferDetails)
                .thenReturn(subscriptionOfferDetails1)
            Mockito.`when`(subscriptionOfferDetails1[0].pricingPhases)
                .thenReturn(pricingPhases1)
            Mockito.`when`(pricingPhases1?.pricingPhaseList).thenReturn(pricingPhaseList1)
            val formattedPrice1 = "$999"
            Mockito.`when`(pricingPhaseList1[0]?.formattedPrice).thenReturn(formattedPrice1)
            Mockito.`when`(pricingPhaseList1[0]?.billingPeriod).thenReturn("P1Y")

            //вторая купленная на другой аккаунт подписка, этот id НЕ приходит в модели Тира
            val product2 = Mockito.mock(ProductDetails::class.java)
            val subscriptionOfferDetails2 =
                listOf(Mockito.mock(ProductDetails.SubscriptionOfferDetails::class.java))
            val pricingPhases2 = Mockito.mock(ProductDetails.PricingPhases::class.java)
            val pricingPhaseList2 =
                listOf(Mockito.mock(ProductDetails.PricingPhase::class.java))

            // Mock active subscription with an invalid ID
            val invalidProductId = "invalidProductId-${RandomString.make()}"
            Mockito.`when`(product2.productId).thenReturn(invalidProductId)
            Mockito.`when`(product2?.subscriptionOfferDetails)
                .thenReturn(subscriptionOfferDetails2)
            Mockito.`when`(subscriptionOfferDetails2[0].pricingPhases)
                .thenReturn(pricingPhases2)
            Mockito.`when`(pricingPhases2?.pricingPhaseList).thenReturn(pricingPhaseList2)
            val formattedPrice2 = "$111"
            Mockito.`when`(pricingPhaseList2[0]?.formattedPrice).thenReturn(formattedPrice2)
            Mockito.`when`(pricingPhaseList2[0]?.billingPeriod).thenReturn("P3Y")
            stubBilling(
                billingClientState = BillingClientState.Connected(
                    listOf(
                        product2,
                        product1
                    )
                )
            )

            val purchase1 = MembershipPurchase(accountId, listOf(invalidProductId), MembershipPurchase.PurchaseState.PURCHASED)
            val purchase2 = MembershipPurchase(accountId, listOf(androidProductId), MembershipPurchase.PurchaseState.PURCHASED)
            stubPurchaseState(BillingPurchaseState.HasPurchases(listOf(purchase1, purchase2), false))

            val flow = flow {
                emit(
                    MembershipStatus(
                        activeTier = TierId(MembershipConstants.STARTER_ID),
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
                    expectedButtonState = TierButton.HiddenWithText.MoreThenOnePurchase,
                    tier = secondTierItem.tier,
                    expectedEmailState = TierEmail.Hidden
                )
            }
        }
    }
}