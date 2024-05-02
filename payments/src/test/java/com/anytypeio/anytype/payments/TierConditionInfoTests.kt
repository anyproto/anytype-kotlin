package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.payments.constants.TiersConstants
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Assert.assertEquals
import org.junit.Test

class TierConditionInfoTests : MembershipTestsSetup() {

    @Test
    fun `when tier not active and free 1`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                    priceStripeUsdCents = 0,
                    periodValue = 0
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Unlimited
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier not active and free 2`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNKNOWN,
                    priceStripeUsdCents = 0,
                    periodValue = 0
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Unknown
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier not active and free 3`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                    priceStripeUsdCents = 0,
                    periodValue = 3
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Year(3)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier not active and not free 1`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                    priceStripeUsdCents = 9999,
                    periodValue = 0
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Price(
                    price = "$99.99",
                    period = TierPeriod.Unlimited
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier not active and not free 2`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNKNOWN,
                    priceStripeUsdCents = 9999,
                    periodValue = 3
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Price(
                    price = "$99.99",
                    period = TierPeriod.Unknown,
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier not active and not free 3`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                    priceStripeUsdCents = 9999,
                    periodValue = 3
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Price(
                    price = "$99.99",
                    period = TierPeriod.Year(3),
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier active and free 1`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                    priceStripeUsdCents = 0,
                    periodValue = 0
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.EXPLORER_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Unlimited
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier active and free 2`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNKNOWN,
                    priceStripeUsdCents = 0,
                    periodValue = 0
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.EXPLORER_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Unknown
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier active and free 3`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                    priceStripeUsdCents = 0,
                    periodValue = 2
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.EXPLORER_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Year(2)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `when tier active and not for free 1`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.BUILDER_ID,
                    name = "Builder",
                    periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                    priceStripeUsdCents = 999,
                    periodValue = 2
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.BUILDER_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.BUILDER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Valid(
                    dateEnds = 1714199910,
                    payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
                    period = TierPeriod.Year(2)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert free 4 year tier`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                    priceStripeUsdCents = 0,
                    periodValue = 4
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Year(4)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert free 3 months tier`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_MONTHS,
                    priceStripeUsdCents = 0,
                    periodValue = 3
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Month(3)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert free 12 weeks tier`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_WEEKS,
                    priceStripeUsdCents = 0,
                    periodValue = 12
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isActive)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Week(12)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert free 7 days tier`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_DAYS,
                    priceStripeUsdCents = 0,
                    periodValue = 7
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isActive)
            assertEquals(
                TierConditionInfo.Visible.Free(
                    period = TierPeriod.Day(7)
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert free unknown period tier`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNKNOWN,
                    priceStripeUsdCents = 0,
                    periodValue = 7
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isActive)
            assertEquals(
                TierConditionInfo.Visible.Free(period = TierPeriod.Unknown),
                tier.conditionInfo
            )
        }
    }
    //endregion

    //region PAID NON ACTIVE TIERS

    @Test
    fun `should convert price unknown period tier`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNKNOWN,
                    priceStripeUsdCents = 0,
                    periodValue = 7
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isActive)
            assertEquals(
                TierConditionInfo.Visible.Free(period = TierPeriod.Unknown),
                tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert price tier with unlimited period`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED,
                    priceStripeUsdCents = 999, // Example price in cents
                    periodValue = 0
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isActive)
            assertEquals(
                TierConditionInfo.Visible.Price(
                    price = "$9.99", // Example price in cents
                    period = TierPeriod.Unlimited
                ), tier.conditionInfo
            )
        }
    }

    @Test
    fun `should convert price tier with 3 years period`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = TiersConstants.EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
                    priceStripeUsdCents = 4999, // Example price in cents
                    periodValue = 3
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(TiersConstants.CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState(BillingPurchaseState.Loading)

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers[0]

            //Asserts
            assertEquals(TiersConstants.EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isActive)
            assertEquals(
                TierConditionInfo.Visible.Price(
                    price = "$49.99", // Example price in cents
                    period = TierPeriod.Year(3)
                ), tier.conditionInfo
            )
        }
    }
}