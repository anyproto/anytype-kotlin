package com.anytypeio.anytype.presentation.home

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubLinkToObjectBlock
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubWidgetBlock
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.bin.EmptyBin
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.GetWidgetSession
import com.anytypeio.anytype.domain.widgets.SaveWidgetSession
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.ListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetConfig
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetSessionStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

class HomeScreenViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var createWidget: CreateWidget

    @Mock
    lateinit var deleteWidget: DeleteWidget

    @Mock
    lateinit var updateWidget: UpdateWidget

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var openObject: OpenObject

    @Mock
    lateinit var closeObject: CloseBlock

    @Mock
    lateinit var getObject: GetObject

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var emptyBin: EmptyBin

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var activeViewStateHolder: WidgetActiveViewStateHolder

    @Mock
    lateinit var collapsedWidgetStateHolder: CollapsedWidgetStateHolder

    @Mock
    lateinit var unsubscriber: Unsubscriber

    @Mock
    lateinit var getDefaultPageType: GetDefaultPageType

    @Mock
    lateinit var appActionManager: AppActionManager

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var saveWidgetSession: SaveWidgetSession

    @Mock
    lateinit var spaceGradientProvider: SpaceGradientProvider

    @Mock
    lateinit var getWidgetSession: GetWidgetSession

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    private val objectPayloadDispatcher = Dispatcher.Default<Payload>()
    private val widgetEventDispatcher = Dispatcher.Default<WidgetDispatchEvent>()

    private val widgetSessionStateHolder = WidgetSessionStateHolder.Impl()

    lateinit var vm: HomeScreenViewModel

    private val appCoroutineDispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher
    )

    private val config = StubConfig(
        widgets = WIDGET_OBJECT_ID
    )

    private lateinit var urlBuilder: UrlBuilder

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
    }

    @Test
    fun `should emit bin, library and actions if there is no block`() = runTest {

        // SETUP

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = emptyList(),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(smartBlock)
        )

        val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

        val events: Flow<List<Event>> = emptyFlow()

        stubConfig()
        stubInterceptEvents(events)
        stubOpenObject(givenObjectView)
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            val secondTimeState = awaitItem()
            assertEquals(
                actual = secondTimeState,
                expected = buildList {
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                }
            )
        }

        delay(1)

        verify(openObject, times(1)).stream(OpenObject.Params(WIDGET_OBJECT_ID, false))
    }

    private fun stubGetWidgetSession(
        session: WidgetSession = WidgetSession(emptyList())
    ) {
        getWidgetSession.stub {
            onBlocking {
                execute(any())
            } doReturn Resultat.Success(session)
        }
    }

    private fun stubSaveWidgetSession(
        session: WidgetSession = WidgetSession(emptyList())
    ) {
        saveWidgetSession.stub {
            onBlocking {
                execute(SaveWidgetSession.Params(session))
            } doReturn Resultat.Success(Unit)
        }
    }

    @Test
    fun `should emit only bin and actions when home screen has no associated widgets except the default ones`() =
        runTest {

            // SETUP

            val smartBlock = StubSmartBlock(
                id = WIDGET_OBJECT_ID,
                children = emptyList(),
            )

            val givenObjectView = StubObjectView(
                root = WIDGET_OBJECT_ID,
                blocks = listOf(smartBlock),
                details = emptyMap()
            )

            val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

            stubConfig()
            stubInterceptEvents(events = emptyFlow())
            stubOpenObject(givenObjectView)
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()

            val vm = buildViewModel()

            // TESTING

            vm.onStart()

            vm.views.test {
                val firstTimeState = awaitItem()
                assertEquals(
                    actual = firstTimeState,
                    expected = emptyList()
                )
                val secondTimeItem = awaitItem()
                assertEquals(
                    expected = buildList {
                        add(WidgetView.Library)
                        add(binWidget)
                        addAll(HomeScreenViewModel.actions)
                    },
                    actual = secondTimeItem
                )
                verify(openObject, times(1)).stream(OpenObject.Params(WIDGET_OBJECT_ID, false))
            }
        }

    @Test
    fun `should emit tree-widget with empty elements and bin when source has no links`() = runTest {

        // SETUP

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = emptyList()
        )

        val sourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.TREE,
            children = listOf(sourceLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLink
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubWidgetActiveView(widgetBlock)
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            val secondTimeState = awaitItem()
            assertEquals(
                expected = buildList {
                    add(
                        WidgetView.Tree(
                            id = widgetBlock.id,
                            source = Widget.Source.Default(sourceObject),
                            elements = emptyList(),
                            isExpanded = true
                        )
                    )
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                },
                actual = secondTimeState
            )
            verify(openObject, times(1)).stream(OpenObject.Params(WIDGET_OBJECT_ID, false))
        }
    }

    @Test
    fun `should emit tree-widget with 2 elements, library and bin`() = runTest {

        // SETUP

        val firstLink = StubObject(
            id = "First link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val secondLink = StubObject(
            id = "Second link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = listOf(firstLink.id, secondLink.id)
        )

        val sourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.TREE,
            children = listOf(sourceLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLink
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)

        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubWidgetActiveView(widgetBlock)
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            val secondTimeState = awaitItem()
            assertEquals(
                expected = buildList {
                    add(
                        WidgetView.Tree(
                            id = widgetBlock.id,
                            source = Widget.Source.Default(sourceObject),
                            elements = listOf(
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = firstLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(firstLink.name.orEmpty()),
                                    indent = 0,
                                    path = widgetBlock.id + "/" + sourceObject.id + "/" + firstLink.id
                                ),
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = secondLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(secondLink.name.orEmpty()),
                                    indent = 0,
                                    path = widgetBlock.id + "/" + sourceObject.id + "/" + secondLink.id
                                )
                            ),
                            isExpanded = true
                        )
                    )
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                },
                actual = secondTimeState
            )
        }
    }

    @Test
    fun `should emit three bundled widgets, each having 2 elements, library and bin`() = runTest {

        // SETUP

        val firstLink = StubObject(
            id = "First link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val secondLink = StubObject(
            id = "Second link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )

        val layout = Block.Content.Widget.Layout.TREE

        val favoriteSource = StubObject(id = BundledWidgetSourceIds.FAVORITE)
        val recentSource = StubObject(id = BundledWidgetSourceIds.RECENT)
        val setsSource = StubObject(id = BundledWidgetSourceIds.SETS)

        val favoriteLink = StubLinkToObjectBlock(
            target = favoriteSource.id
        )

        val recentLink = StubLinkToObjectBlock(
            target = recentSource.id
        )

        val setsLink = StubLinkToObjectBlock(
            target = setsSource.id
        )

        val favoriteWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(favoriteLink.id)
        )

        val recentWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(recentLink.id)
        )

        val setsWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(setsLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(favoriteWidgetBlock.id, recentWidgetBlock.id, setsWidgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                favoriteWidgetBlock,
                favoriteLink,
                recentWidgetBlock,
                recentLink,
                setsWidgetBlock,
                setsLink
            )
        )

        val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)

        stubSearchByIds(
            subscription = favoriteWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink),
            keys = TreeWidgetContainer.keys
        )

        stubSearchByIds(
            subscription = recentWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = setsWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink),
            keys = TreeWidgetContainer.keys
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.FAVORITE,
                workspace = config.workspace,
                keys = TreeWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                workspace = config.workspace,
                keys = TreeWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                workspace = config.workspace,
                keys = TreeWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            val secondTimeState = awaitItem()
            assertEquals(
                expected = buildList {
                    add(
                        WidgetView.Tree(
                            id = favoriteWidgetBlock.id,
                            source = Widget.Source.Bundled.Favorites,
                            elements = listOf(
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = firstLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(firstLink.name.orEmpty()),
                                    indent = 0,
                                    path = favoriteWidgetBlock.id + "/" + favoriteSource.id + "/" + firstLink.id
                                ),
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = secondLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(secondLink.name.orEmpty()),
                                    indent = 0,
                                    path = favoriteWidgetBlock.id + "/" + favoriteSource.id + "/" + secondLink.id
                                )
                            ),
                            isExpanded = true
                        )
                    )
                    add(
                        WidgetView.Tree(
                            id = recentWidgetBlock.id,
                            source = Widget.Source.Bundled.Recent,
                            elements = listOf(
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = firstLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(firstLink.name.orEmpty()),
                                    indent = 0,
                                    path = recentWidgetBlock.id + "/" + recentSource.id + "/" + firstLink.id
                                ),
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = secondLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(secondLink.name.orEmpty()),
                                    indent = 0,
                                    path = recentWidgetBlock.id + "/" + recentSource.id + "/" + secondLink.id
                                )
                            ),
                            isExpanded = true
                        )
                    )
                    add(
                        WidgetView.Tree(
                            id = setsWidgetBlock.id,
                            source = Widget.Source.Bundled.Sets,
                            elements = listOf(
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = firstLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(firstLink.name.orEmpty()),
                                    indent = 0,
                                    path = setsWidgetBlock.id + "/" + setsSource.id + "/" + firstLink.id
                                ),
                                WidgetView.Tree.Element(
                                    elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                    obj = secondLink,
                                    objectIcon = ObjectIcon.Basic.Avatar(secondLink.name.orEmpty()),
                                    indent = 0,
                                    path = setsWidgetBlock.id + "/" + setsSource.id + "/" + secondLink.id
                                )
                            ),
                            isExpanded = true
                        )
                    )
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                },
                actual = secondTimeState
            )
        }
    }

    @Test
    fun `should emit link-widget, library, bin and actions`() = runTest {

        // SETUP

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = emptyList()
        )

        val sourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.LINK,
            children = listOf(sourceLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLink
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            advanceUntilIdle()
            val secondTimeState = awaitItem()
            assertEquals(
                expected = buildList {
                    add(
                        WidgetView.Link(
                            id = widgetBlock.id,
                            source = Widget.Source.Default(sourceObject),
                        )
                    )
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                },
                actual = secondTimeState
            )
            verify(openObject, times(1)).stream(OpenObject.Params(WIDGET_OBJECT_ID, false))
        }
    }

    @Test
    fun `should unsubscribe when widget is deleted as result of user action`() = runTest {

        // SETUP

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = emptyList()
        )

        val sourceLinkBlock = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.LINK,
            children = listOf(sourceLinkBlock.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLinkBlock
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSaveWidgetSession()

        getDefaultPageType.stub {
            onBlocking {
                execute(any())
            } doReturn Resultat.Success(
                GetDefaultPageType.Response(
                    null, null
                )
            )
        }

        storelessSubscriptionContainer.stub {
            onBlocking {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = HomeScreenViewModel.HOME_SCREEN_SPACE_OBJECT_SUBSCRIPTION,
                        targets = listOf(config.workspace),
                        keys = listOf(Relations.ID, Relations.ICON_EMOJI, Relations.ICON_IMAGE)
                    )
                )
            } doReturn emptyFlow()
        }

        val givenPayload = Payload(
            context = WIDGET_OBJECT_ID,
            events = listOf(
                Event.Command.UpdateStructure(
                    context = WIDGET_OBJECT_ID,
                    children = emptyList(),
                    id = smartBlock.id
                ),
                Event.Command.DeleteBlock(
                    context = WIDGET_OBJECT_ID,
                    targets = listOf(widgetBlock.id, sourceLinkBlock.id)
                )
            )
        )

        deleteWidget.stub {
            onBlocking {
                stream(
                    DeleteWidget.Params(
                        ctx = WIDGET_OBJECT_ID,
                        targets = listOf(widgetBlock.id)
                    )
                )
            } doReturn flowOf(Resultat.Success(givenPayload))
        }

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        vm.onDropDownMenuAction(
            widget = widgetBlock.id,
            DropDownMenuAction.RemoveWidget
        )

        advanceUntilIdle()

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(subscriptions = listOf(widgetBlock.id))
        }
    }

    @Test
    fun `should unsubscribe when widget is deleted as result of external event`() = runTest {

        // SETUP

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = emptyList()
        )

        val sourceLinkBlock = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.LINK,
            children = listOf(sourceLinkBlock.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLinkBlock
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        val delayBeforeEvents = 300L

        val givenPayload = Payload(
            context = WIDGET_OBJECT_ID,
            events = listOf(
                Event.Command.UpdateStructure(
                    context = WIDGET_OBJECT_ID,
                    children = emptyList(),
                    id = smartBlock.id
                ),
                Event.Command.DeleteBlock(
                    context = WIDGET_OBJECT_ID,
                    targets = listOf(widgetBlock.id, sourceLinkBlock.id)
                )
            )
        )

        stubInterceptEvents(
            events = flow {
                delay(delayBeforeEvents)
                emit(givenPayload.events)
            }
        )
        stubConfig()
        stubOpenObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.onDropDownMenuAction(
            widget = widgetBlock.id,
            DropDownMenuAction.RemoveWidget
        )

        delay(delayBeforeEvents + 1)

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(subscriptions = listOf(widgetBlock.id))
        }
    }

    @Test
    fun `should close widget-object and unsubscribe on onStop lifecycle event callback`() {
        runTest {

            // SETUP

            val sourceObject = StubObject(
                id = "SOURCE OBJECT",
                links = emptyList()
            )

            val sourceLinkBlock = StubLinkToObjectBlock(
                id = "SOURCE LINK",
                target = sourceObject.id
            )

            val widgetBlock = StubWidgetBlock(
                id = "WIDGET BLOCK",
                layout = Block.Content.Widget.Layout.LINK,
                children = listOf(sourceLinkBlock.id)
            )

            val smartBlock = StubSmartBlock(
                id = WIDGET_OBJECT_ID,
                children = listOf(widgetBlock.id),
            )

            val givenObjectView = StubObjectView(
                root = WIDGET_OBJECT_ID,
                blocks = listOf(
                    smartBlock,
                    widgetBlock,
                    sourceLinkBlock
                ),
                details = mapOf(
                    sourceObject.id to sourceObject.map
                )
            )

            stubInterceptEvents(events = emptyFlow())
            stubConfig()
            stubOpenObject(givenObjectView)
            stubSearchByIds(
                subscription = widgetBlock.id,
                targets = emptyList()
            )
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()
            stubCloseObject()

            val vm = buildViewModel()

            // TESTING

            vm.onStart()

            advanceUntilIdle()

            vm.onStop()

            advanceUntilIdle()

            verifyBlocking(unsubscriber, times(1)) {
                unsubscribe(subscriptions = listOf(widgetBlock.id))
            }

            verify(closeObject, times(1)).stream(params = WIDGET_OBJECT_ID)
        }
    }

    @Test
    fun `should close object and unsubscribe three bundled widgets on onStop callback`() = runTest {

        // SETUP

        val firstLink = StubObject(
            id = "First link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val secondLink = StubObject(
            id = "Second link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )

        val favoriteSource = StubObject(id = BundledWidgetSourceIds.FAVORITE)
        val recentSource = StubObject(id = BundledWidgetSourceIds.RECENT)
        val setsSource = StubObject(id = BundledWidgetSourceIds.SETS)

        val favoriteLink = StubLinkToObjectBlock(target = favoriteSource.id)
        val recentLink = StubLinkToObjectBlock(target = recentSource.id)
        val setsLink = StubLinkToObjectBlock(target = setsSource.id)

        val favoriteWidgetBlock = StubWidgetBlock(
            layout = Block.Content.Widget.Layout.TREE,
            children = listOf(favoriteLink.id)
        )

        val recentWidgetBlock = StubWidgetBlock(
            layout = Block.Content.Widget.Layout.LINK,
            children = listOf(recentLink.id)
        )

        val setsWidgetBlock = StubWidgetBlock(
            layout = Block.Content.Widget.Layout.TREE,
            children = listOf(setsLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(favoriteWidgetBlock.id, recentWidgetBlock.id, setsWidgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                favoriteWidgetBlock,
                favoriteLink,
                recentWidgetBlock,
                recentLink,
                setsWidgetBlock,
                setsLink
            )
        )

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)

        stubSearchByIds(
            subscription = favoriteWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = recentWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = setsWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.FAVORITE,
                workspace = config.workspace,
                keys = ListWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                workspace = config.workspace,
                keys = ListWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                workspace = config.workspace,
                keys = ListWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        vm.onStop()

        advanceUntilIdle()

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(
                subscriptions = listOf(
                    favoriteSource.id, recentSource.id, setsSource.id
                )
            )
        }

        verify(closeObject, times(1)).stream(params = WIDGET_OBJECT_ID)
    }

    @Test
    fun `should resume subscriptions for three bundled widgets with tree layout`() = runTest {

        // SETUP

        val firstLink = StubObject(
            id = "First link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val secondLink = StubObject(
            id = "Second link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )

        val favoriteSource = StubObject(id = BundledWidgetSourceIds.FAVORITE)
        val recentSource = StubObject(id = BundledWidgetSourceIds.RECENT)
        val setsSource = StubObject(id = BundledWidgetSourceIds.SETS)

        val favoriteLink = StubLinkToObjectBlock(target = favoriteSource.id)
        val recentLink = StubLinkToObjectBlock(target = recentSource.id)
        val setsLink = StubLinkToObjectBlock(target = setsSource.id)

        val layout = Block.Content.Widget.Layout.TREE

        val favoriteWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(favoriteLink.id)
        )

        val recentWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(recentLink.id)
        )

        val setsWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(setsLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(favoriteWidgetBlock.id, recentWidgetBlock.id, setsWidgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                favoriteWidgetBlock,
                favoriteLink,
                recentWidgetBlock,
                recentLink,
                setsWidgetBlock,
                setsLink
            )
        )

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)

        stubSearchByIds(
            subscription = favoriteWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = recentWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = setsWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.FAVORITE,
                workspace = config.workspace,
                keys = TreeWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                workspace = config.workspace,
                keys = TreeWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                workspace = config.workspace,
                keys = TreeWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)
        stubCloseObject()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on launch

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = favoriteSource.id,
                    workspace = config.workspace,
                    keys = TreeWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    workspace = config.workspace,
                    keys = TreeWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    workspace = config.workspace,
                    keys = TreeWidgetContainer.keys
                )
            )
        }

        vm.onStop()

        advanceUntilIdle()

        // Verifying unsubscribe behavior

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(
                subscriptions = listOf(
                    favoriteSource.id, recentSource.id, setsSource.id
                )
            )
        }

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on resume

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = favoriteSource.id,
                    workspace = config.workspace,
                    keys = TreeWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    workspace = config.workspace,
                    keys = TreeWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    workspace = config.workspace,
                    keys = TreeWidgetContainer.keys
                )
            )
        }
    }

    @Test
    fun `should resume subscriptions for three bundled widgets with list layout`() = runTest {

        // SETUP

        val firstLink = StubObject(
            id = "First link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val secondLink = StubObject(
            id = "Second link",
            layout = ObjectType.Layout.BASIC.code.toDouble()
        )

        val favoriteSource = StubObject(id = BundledWidgetSourceIds.FAVORITE)
        val recentSource = StubObject(id = BundledWidgetSourceIds.RECENT)
        val setsSource = StubObject(id = BundledWidgetSourceIds.SETS)

        val favoriteLink = StubLinkToObjectBlock(target = favoriteSource.id)
        val recentLink = StubLinkToObjectBlock(target = recentSource.id)
        val setsLink = StubLinkToObjectBlock(target = setsSource.id)

        val layout = Block.Content.Widget.Layout.LIST

        val favoriteWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(favoriteLink.id)
        )

        val recentWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(recentLink.id)
        )

        val setsWidgetBlock = StubWidgetBlock(
            layout = layout,
            children = listOf(setsLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(favoriteWidgetBlock.id, recentWidgetBlock.id, setsWidgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                favoriteWidgetBlock,
                favoriteLink,
                recentWidgetBlock,
                recentLink,
                setsWidgetBlock,
                setsLink
            )
        )

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)

        stubSearchByIds(
            subscription = favoriteWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = recentWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubSearchByIds(
            subscription = setsWidgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.FAVORITE,
                workspace = config.workspace,
                keys = ListWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                workspace = config.workspace,
                keys = ListWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                workspace = config.workspace,
                keys = ListWidgetContainer.keys
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)

        stubCloseObject()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on launch

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = favoriteSource.id,
                    workspace = config.workspace,
                    keys = ListWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    workspace = config.workspace,
                    keys = ListWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    workspace = config.workspace,
                    keys = ListWidgetContainer.keys
                )
            )
        }

        vm.onStop()

        advanceUntilIdle()

        // Verifying unsubscribe behavior

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(
                subscriptions = listOf(
                    favoriteSource.id, recentSource.id, setsSource.id
                )
            )
        }

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on resume

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = favoriteSource.id,
                    workspace = config.workspace,
                    keys = ListWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    workspace = config.workspace,
                    keys = ListWidgetContainer.keys
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    workspace = config.workspace,
                    keys = ListWidgetContainer.keys
                )
            )
        }
    }

    @Test
    fun `should filter out link widgets where source has unsupported object type`() = runTest {

        // SETUP

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = emptyList(),
            objectType = WidgetConfig.excludedTypes.random()
        )

        val sourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.LINK,
            children = listOf(sourceLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLink
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        val binWidget = WidgetView.Bin(id = Subscriptions.SUBSCRIPTION_ARCHIVED)

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())

        stubObserveSpaceObject()
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            delay(1)
            val secondTimeState = awaitItem()
            assertEquals(
                actual = secondTimeState,
                expected = buildList {
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                }
            )
        }
    }

    @Test
    fun `should filter out tree widgets where source has unsupported object type`() = runTest {

        // SETUP

        val sourceObject = StubObject(
            id = "SOURCE OBJECT",
            links = emptyList(),
            objectType = WidgetConfig.excludedTypes.random()
        )

        val sourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = sourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.TREE,
            children = listOf(sourceLink.id)
        )

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartBlock,
                widgetBlock,
                sourceLink
            ),
            details = mapOf(
                sourceObject.id to sourceObject.map
            )
        )

        stubConfig()
        stubInterceptEvents(events = emptyFlow())
        stubOpenObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            delay(1)
            val secondTimeItem = awaitItem()
            assertTrue { secondTimeItem.none { it.id == widgetBlock.id } }
        }
    }

    @Test
    fun `should filter out list widgets where source has unsupported object type`() {
        runTest {
            // SETUP

            val sourceObject = StubObject(
                id = "SOURCE OBJECT",
                links = emptyList(),
                objectType = WidgetConfig.excludedTypes.random()
            )

            val sourceLink = StubLinkToObjectBlock(
                id = "SOURCE LINK",
                target = sourceObject.id
            )

            val widgetBlock = StubWidgetBlock(
                id = "WIDGET BLOCK",
                layout = Block.Content.Widget.Layout.TREE,
                children = listOf(sourceLink.id)
            )

            val smartBlock = StubSmartBlock(
                id = WIDGET_OBJECT_ID,
                children = listOf(widgetBlock.id),
            )

            val givenObjectView = StubObjectView(
                root = WIDGET_OBJECT_ID,
                blocks = listOf(
                    smartBlock,
                    widgetBlock,
                    sourceLink
                ),
                details = mapOf(
                    sourceObject.id to sourceObject.map
                )
            )

            stubConfig()
            stubInterceptEvents(events = emptyFlow())
            stubOpenObject(givenObjectView)
            stubSearchByIds(
                subscription = widgetBlock.id,
                targets = emptyList()
            )
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()

            val vm = buildViewModel()

            // TESTING

            vm.onStart()

            vm.views.test {
                val firstTimeState = awaitItem()
                assertEquals(
                    actual = firstTimeState,
                    expected = emptyList()
                )
                delay(1)
                val secondTimeItem = awaitItem()
                assertTrue { secondTimeItem.none { it.id == widgetBlock.id } }
            }
        }
    }

    private fun stubInterceptEvents(events: Flow<List<Event>>) {
        interceptEvents.stub {
            on { build(InterceptEvents.Params(WIDGET_OBJECT_ID)) } doReturn events
        }
    }

    private fun stubConfig() {
        configStorage.stub {
            on { get() } doReturn config
        }
    }

    private fun stubOpenObject(givenObjectView: ObjectView) {
        openObject.stub {
            on {
                stream(OpenObject.Params(WIDGET_OBJECT_ID, false))
            } doReturn flowOf(
                Resultat.Success(
                    value = givenObjectView
                )
            )
        }
    }

    private fun stubCloseObject() {
        closeObject.stub {
            onBlocking {
                stream(
                    params = WIDGET_OBJECT_ID
                )
            } doReturn flowOf(Resultat.Loading(), Resultat.Success(Unit))
        }
    }

    private fun stubSearchByIds(
        subscription: Id,
        targets: List<Id>,
        keys: List<Key> = TreeWidgetContainer.keys,
        results: List<ObjectWrapper.Basic> = emptyList()
    ) {
        storelessSubscriptionContainer.stub {
            onBlocking {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = subscription,
                        keys = keys,
                        targets = targets
                    )
                )
            } doReturn flowOf(results)
        }
    }

    private fun stubDefaultSearch(
        params: StoreSearchParams,
        results: List<ObjectWrapper.Basic> = emptyList(),
    ) {
        storelessSubscriptionContainer.stub {
            onBlocking {
                subscribe(params)
            } doReturn flowOf(results)
        }
    }

    private fun stubWidgetActiveView(widgetBlock: Block) {
        activeViewStateHolder.stub {
            on { observeCurrentWidgetView(widgetBlock.id) } doReturn flowOf(null)
        }
    }

    private fun stubCollapsedWidgetState(id: Id, isCollapsed: Boolean = false) {
        collapsedWidgetStateHolder.stub {
            on { isCollapsed(id) } doReturn flowOf(isCollapsed)
        }
    }

    private fun stubObserveSpaceObject() {
        storelessSubscriptionContainer.stub {
            onBlocking {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = HomeScreenViewModel.HOME_SCREEN_SPACE_OBJECT_SUBSCRIPTION,
                        targets = listOf(config.workspace),
                        keys = listOf(Relations.ID, Relations.ICON_EMOJI, Relations.ICON_IMAGE)
                    )
                )
            } doReturn emptyFlow()
        }
    }

    private fun buildViewModel() = HomeScreenViewModel(
        configStorage = configStorage,
        interceptEvents = interceptEvents,
        createWidget = createWidget,
        deleteWidget = deleteWidget,
        updateWidget = updateWidget,
        objectPayloadDispatcher = objectPayloadDispatcher,
        widgetEventDispatcher = widgetEventDispatcher,
        openObject = openObject,
        closeObject = closeObject,
        createObject = createObject,
        appCoroutineDispatchers = appCoroutineDispatchers,
        getObject = getObject,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        widgetSessionStateHolder = widgetSessionStateHolder,
        widgetActiveViewStateHolder = activeViewStateHolder,
        collapsedWidgetStateHolder = collapsedWidgetStateHolder,
        urlBuilder = urlBuilder,
        move = move,
        emptyBin = emptyBin,
        unsubscriber = unsubscriber,
        getDefaultPageType = getDefaultPageType,
        appActionManager = appActionManager,
        analytics = analytics,
        getWidgetSession = getWidgetSession,
        saveWidgetSession = saveWidgetSession,
        spaceGradientProvider = spaceGradientProvider,
        storeOfObjectTypes = storeOfObjectTypes
    )

    companion object {
        val WIDGET_OBJECT_ID: Id = MockDataFactory.randomUuid()
    }
}