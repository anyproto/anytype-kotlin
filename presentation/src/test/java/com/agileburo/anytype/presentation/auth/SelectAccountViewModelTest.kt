package com.agileburo.anytype.presentation.auth

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.presentation.auth.account.SelectAccountViewModel
import com.agileburo.anytype.presentation.auth.model.SelectAccountView
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.MockitoDebugger

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
    fun `should emit one account without image`() = runBlockingTest {

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

        vm.state.test()
            .assertValue(
                listOf(
                    SelectAccountView.AccountView(
                        id = account.id,
                        name = account.name,
                        image = account.avatar
                    )
                )
            )
            .assertHistorySize(1)
    }

    @Test
    fun `should emit list with two accounts without images`() = runBlockingTest {

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

        val blob = ByteArray(0)

        val response = Either.Right(blob)

        observeAccounts.stub {
            onBlocking { build() } doReturn accounts
        }

        vm = buildViewModel()

        vm.state.test()
            .assertValue(
                listOf(
                    SelectAccountView.AccountView(
                        id = firstAccount.id,
                        name = firstAccount.name
                    ),
                    SelectAccountView.AccountView(
                        id = secondAccount.id,
                        name = secondAccount.name
                    )
                )
            )
            .assertHistorySize(1)

    }
}