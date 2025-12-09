package com.anytypeio.anytype.presentation.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.domain.spaces.GetLastOpenedSpace
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import java.util.Locale
import kotlin.test.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

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

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var settings: UserSettingsRepository

    private lateinit var getLastOpenedObject: GetLastOpenedObject

    @Mock
    lateinit var globalSubscriptionManager: GlobalSubscriptionManager

    @Mock
    private lateinit var crashReporter: CrashReporter

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var getLastOpenedSpace: GetLastOpenedSpace

    @Mock
    lateinit var createObjectByTypeAndTemplate: CreateObjectByTypeAndTemplate

    @Mock
    lateinit var spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer

    @Mock
    lateinit var migrationHelperDelegate: MigrationHelperDelegate

    lateinit var vm: SplashViewModel

    private val defaultSpaceConfig = StubConfig()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getLastOpenedObject = GetLastOpenedObject(
            settings = settings,
            blockRepo = repo
        )
        stubSpaceManager()
        stubAnalyticSpaceHelperDelegate()
    }

    private fun initViewModel() {
        stubLocalProvider()
        vm = SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics,
            getLastOpenedObject = getLastOpenedObject,
            crashReporter = crashReporter,
            localeProvider = localeProvider,
            spaceManager = spaceManager,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            globalSubscriptionManager = globalSubscriptionManager,
            getLastOpenedSpace = getLastOpenedSpace,
            createObjectByTypeAndTemplate = createObjectByTypeAndTemplate,
            spaceViews = spaceViewSubscriptionContainer,
            migration = migrationHelperDelegate,
            deepLinkResolver = mock()
        )
    }

    @Test
    fun `should not execute use case when view model is created`() {

        val status = AuthStatus.AUTHORIZED
        val response = Resultat.Success(Pair(status, Account(id = "id")))

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        initViewModel()

        runBlocking {
            verify(checkAuthorizationStatus, times(1)).async(any())
            verifyNoMoreInteractions(checkAuthorizationStatus)
        }
    }

    @Test
    fun `should start launching wallet if user is authorized`() {

        val status = AuthStatus.AUTHORIZED

        val response = Resultat.Success(Pair(status, Account(id = "id")))

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        initViewModel()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
        }
    }

    @Test
    fun `should start launching account if wallet is launched`() {

        val status = AuthStatus.AUTHORIZED

        val response = Resultat.Success(Pair(status, Account(id = "id")))

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        initViewModel()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
            verify(launchAccount, times(1)).invoke(any())
        }
    }

    @Test
    fun `should navigate to vault if space view loading times out`() = runTest {
        // GIVEN

        val deeplink = "test-deeplink"

        val status = AuthStatus.AUTHORIZED
        val response = Resultat.Success(Pair(status, Account(id = "id")))

        val space = defaultSpaceConfig.space

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        getLastOpenedSpace.stub {
            onBlocking {
                async(Unit)
            } doReturn Resultat.Success(
                SpaceId(space)
            )
        }

        initViewModel()

        // WHEN
        // simulate spaceManager.observe() never emits a valid space view (simulate timeout)
        spaceManager.stub {
            on { observe() } doReturn flow { /* no emission */ }
        }
        spaceViewSubscriptionContainer.stub {
            on { observe() } doReturn flow { /* no emission */ }
        }

        vm.commands.test {
            // Act: manually trigger proceedWithVaultNavigation
            vm.onDeepLinkLaunch(deeplink)

            // THEN
            // small delay to allow the coroutine to timeout internally
            delay(SplashViewModel.SPACE_LOADING_TIMEOUT + 100)


            val first = awaitItem()
            assertEquals(
                expected = SplashViewModel.Command.NavigateToVault(deeplink),
                actual = first
            )
        }
    }

    @Test
    fun `should navigate to space view when space is restored`() = runTest {
        // GIVEN

        val deeplink = "test-deeplink"

        val status = AuthStatus.AUTHORIZED
        val response = Resultat.Success(Pair(status, Account(id = "id")))

        val space = defaultSpaceConfig.space

        val spaceView = StubSpaceView(
            targetSpaceId = space,
            spaceLocalStatus = SpaceStatus.OK,
            spaceAccountStatus = SpaceStatus.OK
        )

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        getLastOpenedSpace.stub {
            onBlocking {
                async(Unit)
            } doReturn Resultat.Success(
                SpaceId(space)
            )
        }

        initViewModel()

        // WHEN
        // simulate spaceManager.observe() never emits a valid space view (simulate timeout)
        spaceManager.stub {
            on { observe() } doReturn flowOf(defaultSpaceConfig)
        }
        spaceViewSubscriptionContainer.stub {
            on { observe(SpaceId(defaultSpaceConfig.space)) } doReturn flowOf(spaceView)
        }

        vm.commands.test {
            // Act: manually trigger proceedWithVaultNavigation
            vm.onDeepLinkLaunch(deeplink)

            val first = awaitItem()
            assertEquals(
                expected = SplashViewModel.Command.NavigateToWidgets(
                    space = defaultSpaceConfig.space,
                    deeplink
                ),
                actual = first
            )
        }
    }

    @Test
    fun `should navigate to space-level chat if chat is available`() = runTest {
        // GIVEN
        val deeplink = "test-deeplink"
        val status = AuthStatus.AUTHORIZED
        val response = Resultat.Success(Pair(status, Account(id = "id")))

        val space = defaultSpaceConfig.space
        val chatId = "chat-id"

        val spaceView = StubSpaceView(
            targetSpaceId = space,
            spaceLocalStatus = SpaceStatus.OK,
            spaceAccountStatus = SpaceStatus.OK,
            chatId = chatId,
            spaceUxType = SpaceUxType.CHAT
        )

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        getLastOpenedSpace.stub {
            onBlocking { async(Unit) } doReturn Resultat.Success(SpaceId(space))
        }

        initViewModel()

        // WHEN
        spaceManager.stub {
            on { observe() } doReturn flowOf(defaultSpaceConfig)
        }
        spaceViewSubscriptionContainer.stub {
            on { observe(SpaceId(defaultSpaceConfig.space)) } doReturn flowOf(spaceView)
        }

        vm.commands.test {
            // Act
            vm.onDeepLinkLaunch(deeplink)

            val first = awaitItem()
            assertEquals(
                expected = SplashViewModel.Command.NavigateToChat(
                    space = space,
                    chat = chatId,
                    deeplink = deeplink
                ),
                actual = first
            )
        }
    }

    @Test
    fun `should navigate to vault even if chat is available if given space has data ux type`() = runTest {
        // GIVEN
        val deeplink = "test-deeplink"
        val status = AuthStatus.AUTHORIZED
        val response = Resultat.Success(Pair(status, Account(id = "id")))

        val space = defaultSpaceConfig.space
        val chatId = "chat-id"

        val spaceView = StubSpaceView(
            targetSpaceId = space,
            spaceLocalStatus = SpaceStatus.OK,
            spaceAccountStatus = SpaceStatus.OK,
            chatId = chatId,
            spaceUxType = SpaceUxType.DATA
        )

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        getLastOpenedSpace.stub {
            onBlocking { async(Unit) } doReturn Resultat.Success(SpaceId(space))
        }

        initViewModel()

        // WHEN
        spaceManager.stub {
            on { observe() } doReturn flowOf(defaultSpaceConfig)
        }
        spaceViewSubscriptionContainer.stub {
            on { observe(SpaceId(defaultSpaceConfig.space)) } doReturn flowOf(spaceView)
        }

        vm.commands.test {
            // Act
            vm.onDeepLinkLaunch(deeplink)

            val first = awaitItem()
            assertEquals(
                expected = SplashViewModel.Command.NavigateToWidgets(
                    space = space,
                    deeplink = deeplink
                ),
                actual = first
            )
        }
    }

    private fun stubCheckAuthStatus(response: Resultat.Success<Pair<AuthStatus, Account?>>) {
        checkAuthorizationStatus.stub {
            onBlocking { async(eq(Unit)) } doReturn response
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
            onBlocking { invoke(any()) } doReturn Either.Right(Pair("accountId", ""))
        }
    }

    private fun stubGetLastOpenedObject() {
        settings.stub {
            onBlocking {
                getLastOpenedObject(
                    space = SpaceId(defaultSpaceConfig.space)
                )
            } doReturn null
        }
    }

    private fun stubSpaceManager() {
        spaceManager.stub {
            onBlocking {
                get()
            } doReturn defaultSpaceConfig.space
        }
    }

    private fun stubAnalyticSpaceHelperDelegate() {
        Mockito.`when`(analyticSpaceHelperDelegate.provideParams(""))
            .thenReturn(AnalyticSpaceHelperDelegate.Params.EMPTY)
    }

    fun stubLocalProvider() {
        localeProvider.stub {
            on { locale() } doReturn Locale.getDefault()
            on { language() } doReturn "en"
        }
    }
}