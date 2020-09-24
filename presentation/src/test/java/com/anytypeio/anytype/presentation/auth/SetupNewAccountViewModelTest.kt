package com.anytypeio.anytype.presentation.auth

class SetupNewAccountViewModelTest {

    /*

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

        session.name = MockDataFactory.randomString()

        vm = SetupNewAccountViewModel(
            session = session,
            createAccount = createAccount
        )

        verify(createAccount, times(1)).invoke(any(), any(), any())
        verifyNoMoreInteractions(createAccount)
    }

    @Test
    fun `should navigate to next screen if account has been successfully created`() {

        session.name = MockDataFactory.randomString()

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

    */
}