package com.agileburo.anytype.presentation.home

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.*
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.dashboard.interactor.toHomeDashboard
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.HomeDashboardEventConverter
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class HomeDashboardViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var getCurrentAccount: GetCurrentAccount

    @Mock
    lateinit var openDashboard: OpenDashboard

    @Mock
    lateinit var getConfig: GetConfig

    @Mock
    lateinit var closeDashboard: CloseDashboard

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var getDebugSettings: GetDebugSettings

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var gateway: Gateway

    private lateinit var vm: HomeDashboardViewModel

    private val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomUuid(),
        profile = MockDataFactory.randomUuid()
    )

    private val builder: UrlBuilder get() = UrlBuilder(gateway)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    private fun buildViewModel(): HomeDashboardViewModel {
        return HomeDashboardViewModel(
            getCurrentAccount = getCurrentAccount,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            getConfig = getConfig,
            move = move,
            interceptEvents = interceptEvents,
            eventConverter = HomeDashboardEventConverter.DefaultConverter(),
            getDebugSettings = getDebugSettings
        )
    }

    @Test
    fun `should only start getting config when view model is initialized`() {

        // SETUP

        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveEvents(params = InterceptEvents.Params(context = null))

        // TESTING

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should start observing events after receiving config`() {

        // SETUP

        val response = Either.Right(config)

        val params = InterceptEvents.Params(context = config.home)

        stubGetConfig(response)
        stubObserveEvents(params = params)

        // TESTING

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verify(interceptEvents, times(1)).build(params)
    }

    @Test
    fun `should start observing home dashboard after receiving config`() {

        // SETUP

        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))

        // TESTING

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should emit loading state when home dashboard loading started`() {

        // SETUP

        val config = Config(
            home = MockDataFactory.randomUuid(),
            gateway = MockDataFactory.randomUuid(),
            profile = MockDataFactory.randomUuid()
        )

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        // TESTING

        vm = buildViewModel()

        vm.onViewCreated()

        val expected = HomeDashboardStateMachine.State(
            isLoading = true,
            isInitialzed = true,
            dashboard = null,
            error = null
        )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should emit view state with dashboard when home dashboard loading started`() {

        // SETUP

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.HOME
            ),
            children = listOf(page.id),
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString()))
        )

        stubGetConfig(Either.Right(config))

        stubObserveEvents(params = InterceptEvents.Params(context = config.home))

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = listOf(
                    Event.Command.ShowBlock(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard, page)
                    )
                )
            )
        )

        // TESTING

        vm = buildViewModel()

        vm.onViewCreated()

        vm.state.test().assertValue(
            HomeDashboardStateMachine.State(
                isLoading = false,
                isInitialzed = true,
                dashboard = listOf(dashboard, page).toHomeDashboard(dashboard.id),
                error = null
            )
        )
    }

    @Test
    fun `should proceed with getting account and opening dashboard when view is created`() {

        // SETUP

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        // TESTING

        vm = buildViewModel()
        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())

        runBlockingTest {
            verify(openDashboard, times(1)).invoke(eq(null))
        }
    }

    @Test
    fun `should update view state as soon as current account is received`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val response = Either.Right(account)

        stubGetCurrentAccount(response)
        stubObserveEvents()
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        val expected = ProfileView(name = account.name)

        assertEquals(actual = vm.profile.value, expected = expected)
    }

    @Test
    fun `should start creating page when requested from UI`() {

        stubObserveEvents()

        vm = buildViewModel()

        vm.onAddNewDocumentClicked()

        verify(createPage, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should close dashboard and navigate to page screen when page is created`() {

        val id = MockDataFactory.randomUuid()

        stubObserveEvents()
        stubGetEditorSettings()
        stubCloseDashboard()
        stubCreatePage(id)

        vm = buildViewModel()

        vm.onAddNewDocumentClicked()

        vm.navigation
            .test()
            .assertHasValue()
            .assertValue { value ->
                (value.peekContent() as AppNavigation.Command.OpenPage).id == id
            }
    }

    @Test
    fun `should update state when a new block is added without updating dashboard children structure`() {

        val config = Config(
            home = "HOME_ID",
            gateway = MockDataFactory.randomString(),
            profile = MockDataFactory.randomUuid()
        )

        val page = Block(
            id = "FIRST_PAGE_ID",
            children = emptyList(),
            fields = Block.Fields(map = mapOf("name" to "FIRST_PAGE")),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val new = Block(
            id = "NEW_BLOCK_ID",
            children = emptyList(),
            fields = Block.Fields(map = mapOf("name" to "SECOND_PAGE")),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val dashboardName = "HOME"

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.HOME
            ),
            children = listOf(page.id),
            fields = Block.Fields(map = mapOf("name" to dashboardName))
        )

        val showDashboardEvent = Event.Command.ShowBlock(
            root = config.home,
            context = config.home,
            blocks = listOf(dashboard, page)
        )

        val addBlockEvent = Event.Command.AddBlock(
            blocks = listOf(new),
            context = config.home
        )

        val dashboardDelayInMillis = 100L
        val eventDelayInMillis = 200L

        val events: Flow<List<Event>> = flow {
            delay(dashboardDelayInMillis)
            emit(listOf(showDashboardEvent))
            delay(eventDelayInMillis)
            emit(listOf(addBlockEvent))
        }

        stubGetConfig(Either.Right(config))

        stubObserveEvents(
            flow = events,
            params = InterceptEvents.Params(context = config.home)
        )

        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(dashboardDelayInMillis)

        val firstExpectedState = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to dashboardName)),
            type = Block.Content.Smart.Type.HOME,
            blocks = listOf(page),
            children = listOf(page.id)
        )

        val secondExpectedState = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to dashboardName)),
            type = Block.Content.Smart.Type.HOME,
            blocks = listOf(page, new),
            children = listOf(page.id)
        )

        vm.state.test().assertValue(
            HomeDashboardStateMachine.State(
                isLoading = false,
                isInitialzed = true,
                dashboard = firstExpectedState,
                error = null
            )
        )

        coroutineTestRule.advanceTime(eventDelayInMillis)

        vm.state.test().assertValue(
            HomeDashboardStateMachine.State(
                isLoading = false,
                isInitialzed = true,
                dashboard = secondExpectedState,
                error = null
            )
        )
    }

    private fun stubGetConfig(response: Either.Right<Config>) {
        getConfig.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Config>) -> Unit>(2)(response)
            }
        }
    }

    private fun stubObserveEvents(
        flow: Flow<List<Event>> = flowOf(),
        params: InterceptEvents.Params? = null
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
    }

    private fun stubOpenDashboard(
        payload: Payload = Payload(
            context = MockDataFactory.randomString(),
            events = emptyList()
        )
    ) {
        openDashboard.stub {
            onBlocking { invoke(params = null) } doReturn Either.Right(payload)
        }
    }

    private fun stubCreatePage(id: String) {
        createPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Right(id))
            }
        }
    }

    private fun stubCloseDashboard() {
        closeDashboard.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }
    }

    private fun stubGetEditorSettings() {
        getDebugSettings.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(DebugSettings(true))
        }
    }

    private fun stubGetCurrentAccount(accountResponse: Either.Right<Account>) {
        getCurrentAccount.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Account>) -> Unit>(2)(accountResponse)
            }
        }
    }
}