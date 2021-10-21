package com.anytypeio.anytype.presentation.dashboard

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.getChildrenIdsList
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.DebugSettings
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.*
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class HomeDashboardViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var getProfile: GetProfile

    @Mock
    lateinit var openDashboard: OpenDashboard

    @Mock
    lateinit var getConfig: GetConfig

    @Mock
    lateinit var closeDashboard: CloseDashboard

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var setObjectListIsArchived: SetObjectListIsArchived

    @Mock
    lateinit var deleteObjects: DeleteObjects

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var getDebugSettings: GetDebugSettings

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    private lateinit var vm: HomeDashboardViewModel

    private val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomUuid(),
        profile = MockDataFactory.randomUuid()
    )

    private val builder: UrlBuilder get() = UrlBuilder(gateway)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    private fun buildViewModel(): HomeDashboardViewModel {
        return HomeDashboardViewModel(
            getProfile = getProfile,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            getConfig = getConfig,
            move = move,
            interceptEvents = interceptEvents,
            eventConverter = HomeDashboardEventConverter.DefaultConverter(
                builder = builder,
                objectTypesProvider = objectTypesProvider
            ),
            getDebugSettings = getDebugSettings,
            analytics = analytics,
            searchObjects = searchObjects,
            deleteObjects = deleteObjects,
            setObjectListIsArchived = setObjectListIsArchived,
            urlBuilder = builder
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
        verifyZeroInteractions(getProfile)
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
        verifyZeroInteractions(getProfile)
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
            blocks = emptyList(),
            childrenIdsList = emptyList(),
            error = null
        )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should emit view state with dashboard when home dashboard loading started`() {

        // SETUP

        val targetId = MockDataFactory.randomUuid()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.Link(
                target = targetId,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            )
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(SmartBlockType.HOME),
            children = listOf(page.id),
            fields = Block.Fields.empty()
        )

        stubGetConfig(Either.Right(config))

        stubObserveEvents(params = InterceptEvents.Params(context = config.home))

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = listOf(
                    Event.Command.ShowObject(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard, page),
                        type = SmartBlockType.HOME
                    )
                )
            )
        )

        // TESTING

        vm = buildViewModel()

        vm.onViewCreated()

        val views = listOf<DashboardView>(
            DashboardView.Document(
                id = page.id,
                target = targetId,
                isArchived = false,
                isLoading = true
            )
        )

        vm.state.test().assertValue(
            HomeDashboardStateMachine.State(
                isLoading = false,
                isInitialzed = true,
                blocks = views,
                childrenIdsList = listOf(dashboard).getChildrenIdsList(dashboard.id),
                error = null
            )
        )
    }

    @Test
    fun `should proceed with getting profile and opening dashboard when view is created`() {

        // SETUP

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        // TESTING

        vm = buildViewModel()
        vm.onViewCreated()

        verify(getProfile, times(1)).invoke(any(), any(), any())

        runBlockingTest {
            verify(openDashboard, times(1)).invoke(eq(null))
        }
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
                (value.peekContent() as AppNavigation.Command.OpenObject).id == id
            }
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
}