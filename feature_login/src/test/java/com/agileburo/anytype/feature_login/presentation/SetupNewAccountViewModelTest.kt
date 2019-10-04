package com.agileburo.anytype.feature_login.presentation

import com.agileburo.anytype.core_utils.common.Either
import com.agileburo.anytype.feature_login.common.CoroutineTestRule
import com.agileburo.anytype.feature_login.common.DataFactory
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.CreateAccount
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupNewAccountViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.notification.Failure
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SetupNewAccountViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    lateinit var vm: SetupNewAccountViewModel

    private val session = Session()

    @Mock
    lateinit var createAccount: CreateAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw exception when account name is not set`() {
        vm = SetupNewAccountViewModel(
            session = session,
            createAccount = createAccount
        )

        verifyNoMoreInteractions(createAccount)
    }

    @Test
    fun `should start creating account when view model is initialized`() {

        session.name = DataFactory.randomString()

        vm = SetupNewAccountViewModel(
            session = session,
            createAccount = createAccount
        )

        verify(createAccount, times(1)).invoke(any(), any(), any())
        verifyNoMoreInteractions(createAccount)
    }

    @Test
    fun `should navigate to next screen if account has been successfully created`() {

        session.name = DataFactory.randomString()

        createAccount.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Failure, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        vm = SetupNewAccountViewModel(
            session = session,
            createAccount = createAccount
        )

        val navigationObserver = vm.observeNavigation().test()

        vm.proceedWithCreatingAccount()

        verify(createAccount, times(2)).invoke(any(), any(), any())
        verifyNoMoreInteractions(createAccount)

        navigationObserver
            .assertValue(NavigationCommand.CongratulationScreen)
    }
}