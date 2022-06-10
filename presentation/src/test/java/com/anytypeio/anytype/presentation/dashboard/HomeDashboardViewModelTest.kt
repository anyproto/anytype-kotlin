package com.anytypeio.anytype.presentation.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.getChildrenIdsList
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.DebugSettings
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.MockBlockFactory.link
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyZeroInteractions
import kotlin.test.assertContains

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

    @Mock
    lateinit var featuresConfigProvider: FeaturesConfigProvider

    @Mock
    lateinit var getDefaultEditorType: GetDefaultEditorType

    @Mock
    lateinit var cancelSearchSubscription: CancelSearchSubscription

    @Mock
    lateinit var objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer

    @Mock
    lateinit var objectStore: ObjectStore

    @Mock
    lateinit var getTemplates: GetTemplates

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

    private fun givenViewModel(): HomeDashboardViewModel {
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
            urlBuilder = builder,
            getDefaultEditorType = getDefaultEditorType,
            featuresConfigProvider = featuresConfigProvider,
            cancelSearchSubscription = cancelSearchSubscription,
            objectStore = objectStore,
            objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
            getTemplates = getTemplates
        )
    }

    @Test
    fun `key set for search subscription should contain done relation - when request`() {
        givenViewModel().onStart()

        captureObserveKeys(objectSearchSubscriptionContainer).allValues.forEach { keys ->
            assertContains(keys, Relations.DONE, "Should contain done relation for tasks!")
        }
    }

    private fun captureObserveKeys(spannable: ObjectSearchSubscriptionContainer): KArgumentCaptor<List<String>> {
        val captor = argumentCaptor<List<String>>()
        verify(spannable, atLeast(3)).observe(
            any(), any(), any(), any(), any(), captor.capture()
        )
        return captor
    }

    @Test
    fun `should only start getting config when view model is initialized`() {

        // SETUP

        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveEvents(params = InterceptEvents.Params(context = null))
        stubObserveProfile()

        // TESTING

        vm = givenViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
    }

    @Test
    fun `should start observing events after receiving config`() {

        // SETUP

        val response = Either.Right(config)

        val params = InterceptEvents.Params(context = config.home)

        stubGetConfig(response)
        stubObserveEvents(params = params)

        // TESTING

        vm = givenViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verify(interceptEvents, times(1)).build(params)
    }

    @Test
    fun `should start observing home dashboard after receiving config`() {

        // SETUP

        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubObserveProfile()

        // TESTING

        vm = givenViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
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

        vm = givenViewModel()

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

        val page = link(
            content = StubLinkContent(target = targetId)
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

        vm = givenViewModel()

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
    fun `should proceed opening dashboard when view is created`() {

        // SETUP

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()
        stubObserveProfile()

        // TESTING

        vm = givenViewModel()
        vm.onViewCreated()

        runBlockingTest {
            verify(openDashboard, times(1)).invoke(eq(null))
        }
    }

    @Test
    fun `should start creating page when requested from UI`() {

        stubObserveEvents()
        stubGetDefaultObjectType()

        vm = givenViewModel()

        vm.onAddNewDocumentClicked()

        verifyBlocking(createPage, times(1)) { invoke(any()) }
    }

    @Test
    fun `should close dashboard and navigate to page screen when page is created`() {

        val id = MockDataFactory.randomUuid()

        stubObserveEvents()
        stubGetEditorSettings()
        stubCloseDashboard()
        stubCreatePage(id)
        stubGetTemplates()
        stubGetDefaultObjectType()

        vm = givenViewModel()

        vm.onAddNewDocumentClicked()

        vm.navigation
            .test()
            .assertHasValue()
            .assertValue { value ->
                (value.peekContent() as AppNavigation.Command.OpenObject).id == id
            }
    }

    @Test
    fun `should create new object with null template and isDraft true params`() {

        val type = MockDataFactory.randomString()

        stubObserveEvents()
        stubGetEditorSettings()
        stubCloseDashboard()
        stubGetTemplates()
        stubGetDefaultObjectType(type = type)

        vm = givenViewModel()

        vm.onAddNewDocumentClicked()

        val params = CreatePage.Params(
            ctx = null,
            type = type,
            emoji = null,
            isDraft = true,
            template = null
        )

        verifyBlocking(createPage, times(1)) { invoke(params) }
    }

    @Test
    fun `should create new object with non nullable template and isDraft false params`() {

        val templateId = MockDataFactory.randomUuid()
        val type = MockDataFactory.randomString()
        val obj = ObjectWrapper.Basic(mapOf("id" to templateId))

        stubObserveEvents()
        stubGetEditorSettings()
        stubCloseDashboard()
        stubGetTemplates(objects = listOf(obj))
        stubGetDefaultObjectType(type = type)

        vm = givenViewModel()

        vm.onAddNewDocumentClicked()

        val params = CreatePage.Params(
            ctx = null,
            type = type,
            emoji = null,
            isDraft = false,
            template = templateId
        )

        verifyBlocking(createPage, times(1)) { invoke(params) }
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
            onBlocking { invoke(any()) } doReturn Either.Right(id)
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

    private fun stubGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn Either.Right(
                GetDefaultEditorType.Response(
                    type,
                    name
                )
            )
        }
    }

    private fun stubObserveProfile() {
        getProfile.stub {
            on {
                observe(
                    subscription = any(),
                    keys = any(),
                    dispatcher = any()
                )
            } doReturn emptyFlow()
        }
    }

    private fun stubGetTemplates(objects: List<ObjectWrapper.Basic> = listOf()) {
        getTemplates.stub {
            onBlocking { run(any()) } doReturn objects
        }
    }
}