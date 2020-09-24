package com.anytypeio.anytype.presentation.auth

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.presentation.auth.account.SelectAccountViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SelectAccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var startLoadingAccounts: StartLoadingAccounts

    @Mock
    lateinit var observeAccounts: ObserveAccounts

    lateinit var vm: SelectAccountViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    private fun buildViewModel(): SelectAccountViewModel {
        return SelectAccountViewModel(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts
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