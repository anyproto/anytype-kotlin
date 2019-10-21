package com.agileburo.anytype.presentation.auth

class StartLoginViewModelTest {

    /*

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

        //val testObserver = vm.observeNavigation().test()

        vm.onLoginClicked()

        testObserver.assertValue(AppNavigation.Command.EnterKeyChainScreen)
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

    */
}