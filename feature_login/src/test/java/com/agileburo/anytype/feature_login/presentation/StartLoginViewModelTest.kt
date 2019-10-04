package com.agileburo.anytype.feature_login.presentation

import com.agileburo.anytype.core_utils.common.Either
import com.agileburo.anytype.feature_login.common.CoroutineTestRule
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SetupWallet
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.start.StartLoginViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.notification.Failure
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class StartLoginViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var pathProvider: PathProvider

    @Mock
    lateinit var setupWallet: SetupWallet

    lateinit var vm: StartLoginViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        vm = StartLoginViewModel(
            setupWallet = setupWallet,
            pathProvider = pathProvider
        )
    }

    @Test
    fun `when login clicked, should emit navigation command to open enter-keychain screen`() {

        val testObserver = vm.observeNavigation().test()

        vm.onLoginClicked()

        testObserver.assertValue(NavigationCommand.EnterKeyChainScreen)
    }

    @Test
    fun `when wallet is ready, should open create profile screen`() {

        val path = "path"

        stubProvidePath(path)
        stubSetupWalletCall()

        val testObserver = vm.observeNavigation().test()

        vm.onSignUpClicked()

        testObserver.assertValue(NavigationCommand.OpenCreateProfile)
    }

    @Test
    fun `when sign-up button clicked, should start setting up wallet`() {

        val path = "path"

        stubProvidePath(path)
        stubSetupWalletCall()

        vm.onSignUpClicked()

        verify(setupWallet, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should use path provider to setup wallet`() {

        val path = "path"

        stubProvidePath(path)
        stubSetupWalletCall()

        vm.onSignUpClicked()

        verify(pathProvider, times(1)).providePath()
    }

    private fun stubProvidePath(path: String) {
        pathProvider.stub {
            on { providePath() } doReturn path
        }
    }

    private fun stubSetupWalletCall() {
        setupWallet.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Failure, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }
    }
}