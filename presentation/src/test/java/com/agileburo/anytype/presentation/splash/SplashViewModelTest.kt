package com.agileburo.anytype.presentation.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.model.AuthStatus
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.launch.LaunchAccount
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SplashViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    @Mock
    lateinit var launchAccount: LaunchAccount

    lateinit var vm: SplashViewModel


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        vm = SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount
        )
    }

    @Test
    fun `should not execute use case when view model is created`() = runBlocking {
        verify(checkAuthorizationStatus, times(0)).invoke(any(), any(), any())
        verifyNoMoreInteractions(checkAuthorizationStatus)
    }

    @Test
    fun `should start executing use case when view is created`() = runBlocking {
        vm.onViewCreated()
        verify(checkAuthorizationStatus, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should start launching account if user is authorized`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        checkAuthorizationStatus.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, AuthStatus>) -> Unit>(2)(response)
            }
        }

        vm.onViewCreated()

        verify(launchAccount, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should emit appropriate navigation command if account is launched`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        checkAuthorizationStatus.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, AuthStatus>) -> Unit>(2)(response)
            }
        }

        launchAccount.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        vm.onViewCreated()

        vm.navigation.test().assertValue { value ->
            value.peekContent() == AppNavigation.Command.StartDesktopFromSplash
        }
    }

    @Test
    fun `should emit appropriate navigation command if user is unauthorized`() {

        val status = AuthStatus.UNAUTHORIZED

        val response = Either.Right(status)

        checkAuthorizationStatus.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, AuthStatus>) -> Unit>(2)(response)
            }
        }

        vm.onViewCreated()

        vm.navigation.test().assertValue { value ->
            value.peekContent() == AppNavigation.Command.OpenStartLoginScreen
        }
    }
}