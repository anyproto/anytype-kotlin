package com.agileburo.anytype.presentation.home

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_utils.ext.shift
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.DragAndDrop
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.dashboard.interactor.toHomeDashboard
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.emoji.Emoji
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
    lateinit var loadImage: LoadImage

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
    lateinit var dnd: DragAndDrop

    @Mock
    lateinit var emojifier: Emojifier

    private lateinit var vm: HomeDashboardViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    private fun buildViewModel(): HomeDashboardViewModel {
        return HomeDashboardViewModel(
            loadImage = loadImage,
            getCurrentAccount = getCurrentAccount,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            getConfig = getConfig,
            dragAndDrop = dnd,
            interceptEvents = interceptEvents
        )
    }

    @Test
    fun `should only start getting config when view model is initialized`() {

        val config =
            Config(home = MockDataFactory.randomUuid(), gateway = MockDataFactory.randomUuid())
        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveEvents(params = InterceptEvents.Params(context = null))

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(loadImage)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should start observing events after receiving config`() {

        val config =
            Config(home = MockDataFactory.randomUuid(), gateway = MockDataFactory.randomUuid())
        val response = Either.Right(config)

        val params = InterceptEvents.Params(context = null)

        stubGetConfig(response)
        stubObserveEvents(params = params)

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verify(interceptEvents, times(1)).build(params)
    }

    @Test
    fun `should start observing home dashboard after receiving config`() {

        val config =
            Config(home = MockDataFactory.randomUuid(), gateway = MockDataFactory.randomUuid())
        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveEvents(params = InterceptEvents.Params(context = null))

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(loadImage)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should emit loading state when home dashboard loading started`() {

        val config =
            Config(home = MockDataFactory.randomUuid(), gateway = MockDataFactory.randomUuid())

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = null))

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

        val config =
            Config(home = MockDataFactory.randomUuid(), gateway = MockDataFactory.randomUuid())

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
            content = Block.Content.Dashboard(
                type = Block.Content.Dashboard.Type.MAIN_SCREEN
            ),
            children = listOf(page.id),
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString()))
        )

        val delayInMillis = 100L

        val events = flow {
            delay(delayInMillis)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        rootId = config.home,
                        context = config.home,
                        blocks = listOf(dashboard, page)
                    )
                )
            )
        }

        stubGetConfig(Either.Right(config))
        stubObserveEvents(
            params = InterceptEvents.Params(context = null),
            flow = events
        )
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        vm.state.test().assertValue(
            HomeDashboardStateMachine.State(
                isLoading = true,
                isInitialzed = true,
                dashboard = null,
                error = null
            )
        )

        coroutineTestRule.advanceTime(delayInMillis)

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
    fun `block dragging events do not alter overall state`() {

        val config = Config(
            home = MockDataFactory.randomUuid(),
            gateway = MockDataFactory.randomUuid()
        )

        val emoji = Emoji(
            unicode = MockDataFactory.randomString(),
            alias = MockDataFactory.randomString()
        )

        val pages = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    target = MockDataFactory.randomUuid(),
                    type = Block.Content.Link.Type.PAGE,
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                            "icon" to MockDataFactory.randomString()
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    target = MockDataFactory.randomUuid(),
                    type = Block.Content.Link.Type.PAGE,
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                            "icon" to emoji.name
                        )
                    )
                )
            )
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Dashboard(
                type = Block.Content.Dashboard.Type.MAIN_SCREEN
            ),
            children = pages.map { page -> page.id },

            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        val events = flow {
            delay(delayInMillis)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        rootId = config.home,
                        context = config.home,
                        blocks = listOf(dashboard) + pages
                    )
                )
            )
        }

        emojifier.stub {
            onBlocking { fromShortName(any()) } doReturn emoji
        }

        stubGetConfig(Either.Right(config))
        stubObserveEvents(
            params = InterceptEvents.Params(context = null),
            flow = events
        )
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val expected = HomeDashboardStateMachine.State(
            isLoading = false,
            isInitialzed = true,
            dashboard = listOf(
                dashboard,
                pages.first(),
                pages.last()
            ).toHomeDashboard(dashboard.id),
            error = null
        )

        val views = runBlocking {
            listOf(
                dashboard,
                pages.first(),
                pages.last()
            ).toHomeDashboard(dashboard.id).toView(
                emojifier = emojifier
            )
        }

        val from = 0
        val to = 1

        vm.state.test().assertValue(expected)

        vm.onItemMoved(
            from = from,
            to = to,
            alteredViews = views.toMutableList().shift(from, to)
        )

        verifyZeroInteractions(dnd)
        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start dispatching drag-and-drop actions when the dragged item is dropped`() {

        val config = Config(
            home = MockDataFactory.randomUuid(),
            gateway = MockDataFactory.randomUuid()
        )

        val emoji = Emoji(
            unicode = MockDataFactory.randomString(),
            alias = MockDataFactory.randomString()
        )

        val pages = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                            "icon" to emoji.name
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                            "icon" to emoji.name
                        )
                    )
                )
            )
        )

        val dashboard = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            type = Block.Content.Dashboard.Type.MAIN_SCREEN,
            blocks = pages,
            children = pages.map { it.id }
        )

        val delayInMillis = 100L

        val flow = flow {
            delay(delayInMillis)
            emit(dashboard)
        }

        emojifier.stub {
            onBlocking { fromShortName(any()) } doReturn emoji
        }

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = null))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val views = runBlocking {
            dashboard.toView(
                emojifier = emojifier
            )
        }

        val from = 0
        val to = 1

        vm.onItemMoved(
            from = from,
            to = to,
            alteredViews = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(dnd, times(1)).invoke(
            scope = any(),
            params = eq(
                DragAndDrop.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = pages.last().content.asLink().target,
                    blockIds = listOf(pages.first().content.asLink().target),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with getting account and opening dashboard when view is created`() {

        val config =
            Config(home = MockDataFactory.randomUuid(), gateway = MockDataFactory.randomUuid())
        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = null))

        vm = buildViewModel()
        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(openDashboard, times(1)).invoke(any(), eq(null), any())
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

        vm = buildViewModel()

        vm.onViewCreated()

        val expected = ProfileView(name = account.name)

        assertEquals(actual = vm.profile.value, expected = expected)
    }

    @Test
    fun `should fetch avatar image and update view state when account is ready`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomString(),
                sizes = listOf(Image.Size.SMALL)
            ),
            color = null
        )

        val blob = ByteArray(0)

        val accountResponse = Either.Right(account)
        val imageResponse = Either.Right(blob)

        stubObserveEvents()

        stubGetCurrentAccount(accountResponse)

        loadImage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, ByteArray>) -> Unit>(2)(imageResponse)
            }
        }

        vm = buildViewModel()

        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(loadImage, times(1)).invoke(any(), any(), any())

        vm.image.test()
            .assertHasValue()
            .assertValue(blob)
    }

    @Test
    fun `should not fetch the avatar image if given account does not have it`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val accountResponse = Either.Right(account)

        stubObserveEvents()
        stubGetCurrentAccount(accountResponse)

        vm = buildViewModel()

        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(loadImage, never()).invoke(any(), any(), any())
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

        closeDashboard.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        createPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Right(id))
            }
        }

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

        val config = Config(home = "HOME_ID", gateway = MockDataFactory.randomUuid())

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
            content = Block.Content.Dashboard(
                type = Block.Content.Dashboard.Type.MAIN_SCREEN
            ),
            children = listOf(page.id),
            fields = Block.Fields(map = mapOf("name" to dashboardName))
        )

        val showDashboardEvent = Event.Command.ShowBlock(
            rootId = config.home,
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
            params = InterceptEvents.Params(context = null)
        )
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(dashboardDelayInMillis)

        val firstExpectedState = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to dashboardName)),
            type = Block.Content.Dashboard.Type.MAIN_SCREEN,
            blocks = listOf(page),
            children = listOf(page.id)
        )

        val secondExpectedState = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to dashboardName)),
            type = Block.Content.Dashboard.Type.MAIN_SCREEN,
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

    private fun stubOpenDashboard() {
        openDashboard.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
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