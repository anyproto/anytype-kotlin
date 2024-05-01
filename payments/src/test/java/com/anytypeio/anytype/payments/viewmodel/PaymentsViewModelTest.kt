package com.anytypeio.anytype.payments.viewmodel

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.payments.constants.TiersConstants.ACTIVE_TIERS_WITH_BANNERS
import com.anytypeio.anytype.payments.constants.TiersConstants.BUILDER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.CO_CREATOR_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class PaymentsViewModelTest : MembershipTestsSetup() {

    private val mTiers = listOf(
        StubMembershipTierData(
            id = EXPLORER_ID,
        ),
        StubMembershipTierData(
            id = BUILDER_ID,
            androidProductId = androidProductId
        ),
        StubMembershipTierData(
            id = CO_CREATOR_ID
        )
    )

    @Test
    fun `should be in loading state before first members status`() = runTest {
        turbineScope {
            stubMembershipProvider(null)
            stubBilling()
            stubPurchaseState()

            val viewModel = buildViewModel()

            val errorFlow = viewModel.errorState.testIn(backgroundScope)
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)
            val welcomeStateFlow = viewModel.welcomeState.testIn(backgroundScope)
            val codeStateFlow = viewModel.codeState.testIn(backgroundScope)

            assertIs<PaymentsErrorState.Hidden>(errorFlow.awaitItem())
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())
            assertIs<PaymentsWelcomeState.Hidden>(welcomeStateFlow.awaitItem())
            assertIs<PaymentsCodeState.Hidden>(codeStateFlow.awaitItem())
            viewStateFlow.ensureAllEventsConsumed()
            errorFlow.ensureAllEventsConsumed()
            tierStateFlow.ensureAllEventsConsumed()
            welcomeStateFlow.ensureAllEventsConsumed()
            codeStateFlow.ensureAllEventsConsumed()
        }
    }

    @Test
    fun `should init billing after getting members status`() = runTest {
        turbineScope {
            stubMembershipProvider(membershipStatus(mTiers))
            stubBilling()
            stubPurchaseState()

            val viewModel = buildViewModel()

            val initBillingFlow = viewModel.initBillingClient.testIn(backgroundScope)

            assertFalse(initBillingFlow.awaitItem())
            assertTrue(initBillingFlow.awaitItem())
            initBillingFlow.ensureAllEventsConsumed()

            verify(billingClientLifecycle, times(1)).setupSubIds(listOf(androidProductId))
        }
    }

    @Test
    fun `should not billing if no android id is presented`() = runTest {
        turbineScope {
            stubMembershipProvider(
                membershipStatus(
                    listOf(
                        StubMembershipTierData(
                            id = EXPLORER_ID,
                        ),
                        StubMembershipTierData(
                            id = BUILDER_ID,
                        ),
                        StubMembershipTierData(
                            id = CO_CREATOR_ID
                        )
                    )
                )
            )
            stubBilling()
            stubPurchaseState()

            val viewModel = buildViewModel()

            val initBillingFlow = viewModel.initBillingClient.testIn(backgroundScope)

            assertFalse(initBillingFlow.awaitItem())
            initBillingFlow.ensureAllEventsConsumed()
        }
    }

    @Test
    fun `should show with banner when active tiers are none or explorer`() = runTest {
        turbineScope {
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(ACTIVE_TIERS_WITH_BANNERS.random()),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_NONE,
                    anyName = "",
                    tiers = mTiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState()

            val viewModel = buildViewModel()

            val errorFlow = viewModel.errorState.testIn(backgroundScope)
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)
            val welcomeStateFlow = viewModel.welcomeState.testIn(backgroundScope)
            val codeStateFlow = viewModel.codeState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<PaymentsErrorState.Hidden>(errorFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())
            assertIs<PaymentsWelcomeState.Hidden>(welcomeStateFlow.awaitItem())
            assertIs<PaymentsCodeState.Hidden>(codeStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            assertTrue(result.showBanner)

            viewStateFlow.ensureAllEventsConsumed()
            errorFlow.ensureAllEventsConsumed()
            tierStateFlow.ensureAllEventsConsumed()
            welcomeStateFlow.ensureAllEventsConsumed()
            codeStateFlow.ensureAllEventsConsumed()
        }
    }

    @Test
    fun `should don't show banner when active tiers are builder or co-creator or else`() = runTest {
        turbineScope {
            val tierId = listOf(2, 3, BUILDER_ID, CO_CREATOR_ID, 6, 7, 8, 9, 10).random()
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(tierId),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_NONE,
                    anyName = "",
                    tiers = mTiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()
            stubPurchaseState()

            val viewModel = buildViewModel()

            val errorFlow = viewModel.errorState.testIn(backgroundScope)
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            val tierStateFlow = viewModel.tierState.testIn(backgroundScope)
            val welcomeStateFlow = viewModel.welcomeState.testIn(backgroundScope)
            val codeStateFlow = viewModel.codeState.testIn(backgroundScope)

            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<PaymentsErrorState.Hidden>(errorFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())
            assertIs<PaymentsWelcomeState.Hidden>(welcomeStateFlow.awaitItem())
            assertIs<PaymentsCodeState.Hidden>(codeStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            assertFalse(result.showBanner)

            viewStateFlow.ensureAllEventsConsumed()
            errorFlow.ensureAllEventsConsumed()
            tierStateFlow.ensureAllEventsConsumed()
            welcomeStateFlow.ensureAllEventsConsumed()
            codeStateFlow.ensureAllEventsConsumed()
        }
    }
}