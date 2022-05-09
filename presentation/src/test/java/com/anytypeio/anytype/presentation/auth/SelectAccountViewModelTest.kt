package com.anytypeio.anytype.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.presentation.auth.account.SelectAccountViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class SelectAccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var startLoadingAccounts: StartLoadingAccounts

    @Mock
    lateinit var observeAccounts: ObserveAccounts

    @Mock
    lateinit var analytics: Analytics

    lateinit var vm: SelectAccountViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    private fun buildViewModel(): SelectAccountViewModel {
        return SelectAccountViewModel(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts,
            analytics = analytics
        )
    }

    @Test
    fun `should not emit one account without image`() = runBlockingTest {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = MockDataFactory.randomString(),
            color = null
        )

        val accounts = listOf(account).asFlow()


        observeAccounts.stub {
            onBlocking { build() } doReturn accounts
        }

        vm = buildViewModel()

        vm.state.test().assertNoValue()
    }

    @Test
    fun `should not emit list with two accounts without images`() = runBlockingTest {

        val firstAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val secondAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val accounts = flow {
            emit(firstAccount)
            emit(secondAccount)
        }

        observeAccounts.stub {
            onBlocking { build() } doReturn accounts
        }

        vm = buildViewModel()

        vm.state.test().assertNoValue()
    }
}