package com.anytypeio.anytype.payments.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.stub

class PaymentsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

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

    private lateinit var getMembershipPaymentUrl: GetMembershipPaymentUrl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getMembershipPaymentUrl = GetMembershipPaymentUrl(dispatchers, repo)
    }

    @Test
    fun `test 1`() = runTest{
        // Given
        val viewModel = buildViewModel()

        // When
        stubMembershipProvider(StubMembership())

        val p = ProductDetails()
        billingClientLifecycle.stub {
            onBlocking { builderSubProductWithProductDetails }.thenReturn(
                flow<List<ProductDetails>> { listOf(ProductDetails("sku", "", "")) }
            )
        }

        // Then
        viewModel.viewState.test {
            val value = awaitItem()
            assertNotNull(value)
            // assert
        }
    }

    private fun stubMembershipProvider(membershipStatus: MembershipStatus) {
        membershipProvider.stub {
            onBlocking { status() }.thenReturn(flow {
                emit(membershipStatus)
            })
        }
    }

    private fun buildViewModel() = PaymentsViewModel(
        analytics = analytics,
        billingClientLifecycle = billingClientLifecycle,
        getAccount = getAccount,
        membershipProvider = membershipProvider,
        getMembershipPaymentUrl = getMembershipPaymentUrl
    )
}