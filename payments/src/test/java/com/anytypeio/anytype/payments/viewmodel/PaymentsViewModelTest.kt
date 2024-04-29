package com.anytypeio.anytype.payments.viewmodel

import app.cash.turbine.turbineScope
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.ResolveMembershipName
import com.anytypeio.anytype.payments.constants.TiersConstants.ACTIVE_TIERS_WITH_BANNERS
import com.anytypeio.anytype.payments.constants.TiersConstants.BUILDER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.CO_CREATOR_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import junit.framework.TestCase.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class PaymentsViewModelTest {

    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    ).also { Dispatchers.setMain(dispatcher) }

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var billingClientLifecycle: BillingClientLifecycle

    @Mock
    lateinit var getAccount: GetAccount

    @Mock
    lateinit var membershipProvider: MembershipProvider

    @Mock
    lateinit var isMembershipNameValid: IsMembershipNameValid

    @Mock
    lateinit var resolveMembershipName: ResolveMembershipName

    private lateinit var getMembershipPaymentUrl: GetMembershipPaymentUrl
    private val androidProductId = "id_android_builder"

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

    private fun membershipStatus(tiers: List<MembershipTierData>) = MembershipStatus(
        activeTier = TierId(EXPLORER_ID),
        status = Membership.Status.STATUS_ACTIVE,
        dateEnds = 1714199910,
        paymentMethod = MembershipPaymentMethod.METHOD_NONE,
        anyName = "",
        tiers = tiers,
        formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getMembershipPaymentUrl = GetMembershipPaymentUrl(dispatchers, repo)
    }

    @Test
    fun `should be in loading state before first members status`() = runTest {
        turbineScope {
            stubMembershipProvider(null)
            stubBilling()

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
            stubMembershipProvider(membershipStatus(listOf(
                StubMembershipTierData(
                    id = EXPLORER_ID,
                ),
                StubMembershipTierData(
                    id = BUILDER_ID,
                ),
                StubMembershipTierData(
                    id = CO_CREATOR_ID
                )
            )))
            stubBilling()

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

    /**
     * Tiers, different
     * 1. EXPLORER_ID, not active, valid forever
     */



    @Test
    fun `should convert tier models to preview views`() = runTest {
        turbineScope {
            val tiers = listOf(
                StubMembershipTierData(
                    id = EXPLORER_ID,
                    name = "Explorer",
                    colorStr = "#000000",
                    features = listOf("feature1", "feature2"),
                    periodType = MembershipPeriodType.PERIOD_TYPE_UNLIMITED
                )
            )
            stubMembershipProvider(
                MembershipStatus(
                    activeTier = TierId(CO_CREATOR_ID),
                    status = Membership.Status.STATUS_ACTIVE,
                    dateEnds = 1714199910,
                    paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
                    anyName = RandomString.make(),
                    tiers = tiers,
                    formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
                )
            )
            stubBilling()

            val viewModel = buildViewModel()
            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())

            val result = viewStateFlow.awaitItem()
            assertIs<MembershipMainState.Default>(result)
            val tier = result.tiers.first()

            //Asserts
            assertEquals(EXPLORER_ID, tier.id.value)
            assertEquals(false, tier.isCurrent)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun `should show tier not found error`() = runTest {
//        turbineScope {
//            stubMembershipProvider(membershipStatus(tiers))
//            val viewModel = buildViewModel()
//            val nonExistentTierId = TierId(Int.MAX_VALUE)
//
//            val errorFlow = viewModel.errorState.testIn(backgroundScope)
//            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
//
//            assertIs<PaymentsErrorState.Hidden>(errorFlow.awaitItem())
//            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
//            advanceUntilIdle()
//            assertIs<MembershipMainState.Default>(viewStateFlow.awaitItem())
//            advanceUntilIdle()
//            viewModel.onTierClicked(nonExistentTierId)
//            assertEquals(
//                expected = PaymentsErrorState.TierNotFound(nonExistentTierId),
//                actual = errorFlow.awaitItem()
//            )
//            viewStateFlow.ensureAllEventsConsumed()
//            errorFlow.ensureAllEventsConsumed()
//        }
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun `should show membership status not found error`() = runTest {
//        turbineScope {
//            stubMembershipProvider(null)
//            val viewModel = buildViewModel()
//            val nonExistentTierId = TierId(Int.MAX_VALUE)
//
//            val errorFlow = viewModel.errorState.testIn(backgroundScope)
//            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
//
//            assertIs<PaymentsErrorState.Hidden>(errorFlow.awaitItem())
//            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
//            advanceUntilIdle()
//            assertIs<MembershipMainState.Default.WithBanner>(viewStateFlow.awaitItem())
//            advanceUntilIdle()
//            viewModel.onTierClicked(nonExistentTierId)
//            assertEquals(
//                expected = PaymentsErrorState.TierNotFound(nonExistentTierId),
//                actual = errorFlow.awaitItem()
//            )
//            viewStateFlow.ensureAllEventsConsumed()
//            errorFlow.ensureAllEventsConsumed()
//        }
//    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun `should show tier without subscription and `() = runTest {
//        turbineScope {
//            val viewModel = buildViewModel()
//            val nonExistentTierId = TierId(EXPLORER_ID)
//
//            val errorFlow = viewModel.errorState.testIn(backgroundScope)
//            val viewStateFlow = viewModel.viewState.testIn(backgroundScope)
//
//            assertIs<PaymentsErrorState.Hidden>(errorFlow.awaitItem())
//            errorFlow.expectNoEvents()
//            assertIs<MembershipMainState.Loading>(viewStateFlow.awaitItem())
//            advanceUntilIdle()
//            assertIs<MembershipMainState.Default.WithBanner>(viewStateFlow.awaitItem())
//            advanceUntilIdle()
//            viewModel.onTierClicked(nonExistentTierId)
//                assertEquals(
//                expected = PaymentsErrorState.TierNotFound(nonExistentTierId),
//                actual = errorFlow.awaitItem()
//            )
//            viewStateFlow.ensureAllEventsConsumed()
//        }
//    }

    private fun stubMembershipProvider(membershipStatus: MembershipStatus?) {
        val flow = if (membershipStatus == null) {
            emptyFlow()
        } else {
            flow {
                emit(membershipStatus)
            }
        }
        membershipProvider.stub {
            onBlocking { status() }.thenReturn(flow)
        }
    }

    private fun stubBilling() {
        val p = Mockito.mock(ProductDetails::class.java)
        val billingState = BillingClientState.Connected.Ready(listOf(p))
        billingClientLifecycle.stub {
            onBlocking { builderSubProductWithProductDetails }.thenReturn(
                MutableStateFlow(billingState)
            )
        }
    }

    private fun buildViewModel() = PaymentsViewModel(
        analytics = analytics,
        billingClientLifecycle = billingClientLifecycle,
        getAccount = getAccount,
        membershipProvider = membershipProvider,
        getMembershipPaymentUrl = getMembershipPaymentUrl,
        isMembershipNameValid = isMembershipNameValid,
        resolveMembershipName = resolveMembershipName
    )
}