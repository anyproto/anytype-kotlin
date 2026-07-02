package com.anytypeio.anytype.payments

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipConstants.ACTIVE_TIERS_WITH_BANNERS
import com.anytypeio.anytype.core_models.membership.MembershipConstants.BUILDER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.CO_CREATOR_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.STARTER_ID
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.payments.viewmodel.ActivateCodeState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.payments.viewmodel.MembershipEmailCodeState
import com.anytypeio.anytype.payments.viewmodel.MembershipErrorState
import com.anytypeio.anytype.payments.viewmodel.TierAction
import com.anytypeio.anytype.payments.viewmodel.WelcomeState
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MembershipViewModelTest : MembershipTestsSetup() {

    private val mTiers = listOf(
        StubMembershipTierData(
            id = STARTER_ID,
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

            assertIs<MembershipErrorState.Hidden>(errorFlow.awaitItem())
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())
            assertIs<WelcomeState.Hidden>(welcomeStateFlow.awaitItem())
            assertIs<MembershipEmailCodeState.Hidden>(codeStateFlow.awaitItem())
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
                            id = STARTER_ID,
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
            assertIs<MembershipErrorState.Hidden>(errorFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())
            assertIs<WelcomeState.Hidden>(welcomeStateFlow.awaitItem())
            assertIs<MembershipEmailCodeState.Hidden>(codeStateFlow.awaitItem())

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
            assertIs<MembershipErrorState.Hidden>(errorFlow.awaitItem())
            assertIs<MembershipTierState.Hidden>(tierStateFlow.awaitItem())
            assertIs<WelcomeState.Hidden>(welcomeStateFlow.awaitItem())
            assertIs<MembershipEmailCodeState.Hidden>(codeStateFlow.awaitItem())

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

    @Test
    fun `redeem success confirms with the settled active tier`() = runTest {
        // The pre-redeem active tier (Starter) and the post-redeem one (Plus) differ; after a
        // forced refresh the settle routine must confirm with the NEW tier read from the status.
        val oldTiers = listOf(StubMembershipTierData(id = STARTER_ID, name = "Starter"))
        val newTiers = listOf(
            StubMembershipTierData(
                id = BUILDER_ID,
                name = "Plus",
                features = listOf("Feature A", "Feature B"),
                androidProductId = androidProductId
            )
        )
        whenever(membershipProvider.status(false)) doReturn flowOf(statusWith(STARTER_ID, oldTiers))
        whenever(membershipProvider.status(true)) doReturn flowOf(statusWith(BUILDER_ID, newTiers))
        stubBilling()
        stubPurchaseState()
        stubRedeemUseCases()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onTierAction(TierAction.OnActivateCodeClicked("CODE-123"))
        advanceUntilIdle()

        val state = viewModel.activateCodeState.value
        assertIs<ActivateCodeState.Visible.Success>(state)
        assertEquals("Plus", state.tierName)
        assertEquals(listOf("Feature A", "Feature B"), state.features)
    }

    @Test
    fun `redeem success without a settled new tier confirms generically`() = runTest {
        // The active tier never changes after redeem, so the settle routine times out and we
        // still confirm success — just without a tier name (no endless spinner).
        val tiers = listOf(StubMembershipTierData(id = STARTER_ID, name = "Starter"))
        whenever(membershipProvider.status(any())) doReturn flowOf(statusWith(STARTER_ID, tiers))
        stubBilling()
        stubPurchaseState()
        stubRedeemUseCases()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onTierAction(TierAction.OnActivateCodeClicked("CODE-123"))
        advanceUntilIdle()

        val state = viewModel.activateCodeState.value
        assertIs<ActivateCodeState.Visible.Success>(state)
        assertNull(state.tierName)
    }

    @Test
    fun `redeem failure surfaces an error and does not confirm`() = runTest {
        val tiers = listOf(StubMembershipTierData(id = STARTER_ID, name = "Starter"))
        whenever(membershipProvider.status(any())) doReturn flowOf(statusWith(STARTER_ID, tiers))
        stubBilling()
        stubPurchaseState()
        stubRedeemUseCases(redeemResult = Resultat.failure(Exception("boom")))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onTierAction(TierAction.OnActivateCodeClicked("CODE-123"))
        advanceUntilIdle()

        val state = viewModel.activateCodeState.value
        assertIs<ActivateCodeState.Visible.Error>(state)
        assertEquals("boom", state.message)
    }

    private fun statusWith(activeTier: Int, tiers: List<MembershipTierData>) = MembershipStatus(
        activeTier = TierId(activeTier),
        status = Membership.Status.STATUS_ACTIVE,
        dateEnds = 1714199910,
        paymentMethod = MembershipPaymentMethod.METHOD_STRIPE,
        anyName = "",
        tiers = tiers,
        formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
    )

    private fun stubRedeemUseCases(
        infoResult: Resultat<Int> = Resultat.success(40),
        redeemResult: Resultat<Unit> = Resultat.success(Unit)
    ) {
        getMembershipCodeInfo.stub {
            onBlocking { async(any()) } doReturn infoResult
        }
        redeemMembershipCode.stub {
            onBlocking { async(any()) } doReturn redeemResult
        }
    }
}