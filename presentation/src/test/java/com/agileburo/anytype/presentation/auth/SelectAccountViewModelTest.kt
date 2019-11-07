package com.agileburo.anytype.presentation.auth

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.presentation.CoroutinesTestRule
import com.agileburo.anytype.presentation.auth.account.SelectAccountViewModel
import com.agileburo.anytype.presentation.auth.model.SelectAccountView
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
    lateinit var loadImage: LoadImage

    lateinit var vm: SelectAccountViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

    }

    private fun buildViewModel(): SelectAccountViewModel {
        return SelectAccountViewModel(
            startLoadingAccounts = startLoadingAccounts,
            loadImage = loadImage,
            observeAccounts = observeAccounts
        )
    }

    @Test
    fun `should emit one account with image`() = runBlockingTest {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val accounts = listOf(account).asFlow()

        val blob = ByteArray(0)

        val response = Either.Right(blob)

        observeAccounts.stub {
            onBlocking { build() } doReturn accounts
        }

        loadImage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, ByteArray>) -> Unit>(2)(response)
            }
        }

        vm = buildViewModel()

        vm.state.test()
            .assertValue(
                listOf(
                    SelectAccountView.AccountView(
                        id = account.id,
                        name = account.name,
                        image = blob
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
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val secondAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
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

        loadImage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, ByteArray>) -> Unit>(2)(response)
            }
        }

        vm = buildViewModel()

        vm.state.test()
            .assertValue(
                listOf(
                    SelectAccountView.AccountView(
                        id = firstAccount.id,
                        name = firstAccount.name,
                        image = blob
                    ),
                    SelectAccountView.AccountView(
                        id = secondAccount.id,
                        name = secondAccount.name,
                        image = blob
                    )
                )
            )
            .assertHistorySize(1)

    }

    @Test
    fun `should emit first account while the second one is still loading`() = runBlockingTest {

        val firstAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val secondAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val accounts = flow {
            emit(firstAccount)
            delay(300)
            emit(secondAccount)
        }

        val blob = ByteArray(0)

        val response = Either.Right(blob)

        observeAccounts.stub {
            onBlocking { build() } doReturn accounts
        }

        loadImage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, ByteArray>) -> Unit>(2)(response)
            }
        }

        vm = buildViewModel()

        vm.state.test()
            .assertValue(
                listOf(
                    SelectAccountView.AccountView(
                        id = firstAccount.id,
                        name = firstAccount.name,
                        image = blob
                    )
                )
            )
            .assertHistorySize(1)

        vm.viewModelScope.cancel()
    }
}