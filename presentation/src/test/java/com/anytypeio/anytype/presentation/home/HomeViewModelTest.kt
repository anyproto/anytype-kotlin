package com.anytypeio.anytype.presentation.home

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubLinkToObjectBlock
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubWidgetBlock
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class HomeViewModelTest {

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
    lateinit var getObject: GetObject

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var activeViewStateHolder: WidgetActiveViewStateHolder

    @Mock
    lateinit var collapsedWidgetStateHolder: CollapsedWidgetStateHolder

    @Mock
    lateinit var gateway: Gateway

    private val objectPayloadDispatcher = Dispatcher.Default<Payload>()
    private val widgetEventDispatcher = Dispatcher.Default<WidgetDispatchEvent>()

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
    fun `should emit only widget actions if there is no block`() = runTest {

        // SETUP

        val smartBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = emptyList(),
            type = SmartBlockType.WIDGET
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            type = SmartBlockType.WIDGET,
            blocks = listOf(smartBlock)
        )

        val events : Flow<List<Event>> = emptyFlow()

        stubConfig()
        stubInterceptEvents(events)
        stubOpenObject(givenObjectView)

        val vm = buildViewModel()

        // TESTING

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = HomeScreenViewModel.actions
            )
        }

        delay(1)

        verify(openObject, times(1)).invoke(WIDGET_OBJECT_ID)
    }

    @Test
    fun `should emit tree-widget with empty elements when source has no links`() = runTest {

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
            type = SmartBlockType.WIDGET
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            type = SmartBlockType.WIDGET,
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
        stubObjectSearchContainer(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(widgetBlock)
        stubWidgetActiveView(widgetBlock)

        val vm = buildViewModel()

        // TESTING

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = HomeScreenViewModel.actions
            )
            val secondTimeItem = awaitItem()
            assertEquals(
                expected = buildList {
                    add(
                        WidgetView.Tree(
                            id = widgetBlock.id,
                            obj = sourceObject,
                            elements = emptyList(),
                            isExpanded = true
                        )
                    )
                    addAll(HomeScreenViewModel.actions)
                },
                actual = secondTimeItem
            )
            verify(openObject, times(1)).invoke(WIDGET_OBJECT_ID)
        }
    }

    @Test
    fun `should emit link-widget and actions`() = runTest {

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
            type = SmartBlockType.WIDGET
        )

        val givenObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            type = SmartBlockType.WIDGET,
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
        stubObjectSearchContainer(
            subscription = widgetBlock.id,
            targets = emptyList()
        )

        val vm = buildViewModel()

        // TESTING

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = HomeScreenViewModel.actions
            )
            val secondTimeItem = awaitItem()
            assertEquals(
                expected = buildList {
                    add(
                        WidgetView.Link(
                            id = widgetBlock.id,
                            obj = sourceObject
                        )
                    )
                    addAll(HomeScreenViewModel.actions)
                },
                actual = secondTimeItem
            )
            verify(openObject, times(1)).invoke(WIDGET_OBJECT_ID)
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
                invoke(WIDGET_OBJECT_ID)
            } doReturn flowOf(
                Resultat.Success(
                    value = givenObjectView
                )
            )
        }
    }

    private fun stubObjectSearchContainer(
        subscription: Id,
        targets: List<Id>,
        keys: List<Key> = TreeWidgetContainer.keys
    ) {
        objectSearchSubscriptionContainer.stub {
            onBlocking {
                get(
                    subscription = subscription,
                    keys = keys,
                    targets = targets
                )
            } doReturn emptyList()
        }
    }

    private fun stubWidgetActiveView(widgetBlock: Block) {
        activeViewStateHolder.stub {
            on { observeCurrentWidgetView(widgetBlock.id) } doReturn flowOf(null)
        }
    }

    private fun stubCollapsedWidgetState(widgetBlock: Block) {
        collapsedWidgetStateHolder.stub {
            on { isCollapsed(widgetBlock.id) } doReturn flowOf(false)
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
        createObject = createObject,
        objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
        appCoroutineDispatchers = appCoroutineDispatchers,
        getObject = getObject,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        widgetActiveViewStateHolder = activeViewStateHolder,
        collapsedWidgetStateHolder = collapsedWidgetStateHolder,
        urlBuilder = urlBuilder
    )

    companion object {
        val WIDGET_OBJECT_ID: Id = MockDataFactory.randomUuid()
    }
}