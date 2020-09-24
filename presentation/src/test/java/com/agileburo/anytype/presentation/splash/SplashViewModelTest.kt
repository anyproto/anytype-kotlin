package com.agileburo.anytype.presentation.splash

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.interactor.LaunchAccount
import com.agileburo.anytype.domain.auth.interactor.LaunchWallet
import com.agileburo.anytype.domain.auth.model.AuthStatus
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
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

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    @Mock
    lateinit var launchAccount: LaunchAccount

    @Mock
    lateinit var launchWallet: LaunchWallet

    @Mock
    lateinit var analytics: Analytics

    lateinit var vm: SplashViewModel


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        vm = SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics
        )
    }

    @Test
    fun `should not execute use case when view model is created`() {

        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()

        runBlocking {
            verify(checkAuthorizationStatus, times(0)).invoke(any(), any(), any())
            verify(checkAuthorizationStatus, times(0)).invoke(any())
            verifyNoMoreInteractions(checkAuthorizationStatus)
        }
    }

    @Test
    fun `should start executing use case when view is created`() {

        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()

        vm.onResume()

        runBlocking {
            verify(checkAuthorizationStatus, times(1)).invoke(any())
        }
    }

    @Test
    fun `should start launching wallet if user is authorized`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()

        vm.onResume()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
        }
    }

    @Test
    fun `should start launching account if wallet is launched`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()

        vm.onResume()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
            verify(launchAccount, times(1)).invoke(any())
        }
    }

    //Todo can't mock Amplitude
//    @Test
//    fun `should emit appropriate navigation command if account is launched`() {
//
//        val status = AuthStatus.AUTHORIZED
//
//        val response = Either.Right(status)
//
//
//        //Mockito.`when`(amplitude.setUserId("accountId", true)).
//        stubCheckAuthStatus(response)
//        stubLaunchAccount()
//        stubLaunchWallet()
//
//        vm.onResume()
//
//        verify(amplitude, times(1)).setUserId("accountId", true)
//
//        vm.navigation.test().assertValue { value ->
//            value.peekContent() == AppNavigation.Command.StartDesktopFromSplash
//        }
//    }

    @Test
    fun `should emit appropriate navigation command if user is unauthorized`() {

        val status = AuthStatus.UNAUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)

        vm.onResume()

        vm.navigation.test().assertValue { value ->
            value.peekContent() == AppNavigation.Command.OpenStartLoginScreen
        }
    }

    @Test
    fun `should retry launching wallet after failed launch and emit error`() {

        // SETUP

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        val exception = Exception(MockDataFactory.randomString())

        stubCheckAuthStatus(response)

        stubLaunchWallet(response = Either.Left(exception))

        // TESTING

        val state = vm.state.test()

        state.assertNoValue()

        vm.onResume()

        state.assertValue { value -> value is ViewState.Error }

        runBlocking {
            verify(launchWallet, times(2)).invoke(any())
        }
    }

    private fun stubCheckAuthStatus(response: Either.Right<AuthStatus>) {
        checkAuthorizationStatus.stub {
            onBlocking { invoke(eq(Unit)) } doReturn response
        }
    }

    private fun stubLaunchWallet(
        response: Either<Throwable, Unit> = Either.Right(Unit)
    ) {
        launchWallet.stub {
            onBlocking { invoke(any()) } doReturn response
        }
    }

    private fun stubLaunchAccount() {
        launchAccount.stub {
            onBlocking { invoke(any()) } doReturn Either.Right("accountId")
        }
    }
}