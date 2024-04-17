package com.anytypeio.anytype.presentation.home

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubFilter
import com.anytypeio.anytype.core_models.StubLinkToObjectBlock
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubWidgetBlock
import com.anytypeio.anytype.core_models.UNKNOWN_SPACE_TYPE
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.bin.EmptyBin
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.domain.types.GetPinnedObjectTypes
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.GetWidgetSession
import com.anytypeio.anytype.domain.widgets.SaveWidgetSession
import com.anytypeio.anytype.domain.widgets.SetWidgetActiveView
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.ListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceWidgetContainer
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions

class HomeScreenViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
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
    lateinit var setObjectDetails: SetObjectDetails

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
    lateinit var getDefaultObjectType: GetDefaultObjectType

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
    lateinit var setWidgetActiveView: SetWidgetActiveView

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var objectWatcher: ObjectWatcher

    @Mock
    lateinit var spaceWidgetContainer: SpaceWidgetContainer

    @Mock
    lateinit var getSpaceView: GetSpaceView

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var getPinnedObjectTypes: GetPinnedObjectTypes

    @Mock
    lateinit var deepLinkToObjectDelegate: DeepLinkToObjectDelegate

    lateinit var userPermissionProvider: UserPermissionProvider

    private val objectPayloadDispatcher = Dispatcher.Default<Payload>()
    private val widgetEventDispatcher = Dispatcher.Default<WidgetDispatchEvent>()

    private val widgetSessionStateHolder = WidgetSessionStateHolder.Impl()

    lateinit var vm: HomeScreenViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    private val appCoroutineDispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher
    )

    private val defaultSpaceConfig = StubConfig(
        widgets = WIDGET_OBJECT_ID
    )

    private val defaultSpaceWidgetView = WidgetView.SpaceWidget.View(
        space = StubObject(),
        icon = SpaceIconView.Placeholder,
        type = UNKNOWN_SPACE_TYPE
    )

    private lateinit var urlBuilder: UrlBuilder

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
        stubSpaceManager()
        userPermissionProvider = UserPermissionProviderStub()
        stubGetPinnedObjectTypes()
    }

    @Test
    fun `should emit bin, library and actions and space view if there is no block`() = runTest {

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
        stubOpenWidgetObject(givenObjectView)
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

        val vm = buildViewModel()

        // TESTING

        vm.views.test {
            val firstTimeState = awaitItem()
            assertEquals(
                actual = firstTimeState,
                expected = emptyList()
            )
            vm.onStart()
            val secondTimeState = awaitItem()
            verify(openObject, times(1)).stream(
                OpenObject.Params(
                    obj = WIDGET_OBJECT_ID,
                    saveAsLastOpened = false,
                    spaceId = SpaceId(defaultSpaceConfig.space)
                )
            )
            assertEquals(
                actual = secondTimeState,
                expected = buildList {
                    add(defaultSpaceWidgetView)
                    add(WidgetView.Library)
                    add(binWidget)
                    addAll(HomeScreenViewModel.actions)
                }
            )
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
            stubOpenWidgetObject(givenObjectView)
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()
            stubSpaceManager()
            stubSpaceWidgetContainer(defaultSpaceWidgetView)
            stubUserPermission()

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
                        add(defaultSpaceWidgetView)
                        add(WidgetView.Library)
                        add(binWidget)
                        addAll(HomeScreenViewModel.actions)
                    },
                    actual = secondTimeItem
                )
                verify(openObject, times(1)).stream(
                    OpenObject.Params(
                        obj = WIDGET_OBJECT_ID,
                        saveAsLastOpened = false,
                        spaceId = SpaceId(defaultSpaceConfig.space)
                    )
                )
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
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubWidgetActiveView(widgetBlock)
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

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
                    add(defaultSpaceWidgetView)
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
            verify(openObject, times(1)).stream(
                OpenObject.Params(
                    obj = WIDGET_OBJECT_ID,
                    saveAsLastOpened = false,
                    spaceId = SpaceId(defaultSpaceConfig.space)
                )
            )
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
        stubOpenWidgetObject(givenObjectView)

        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubWidgetActiveView(widgetBlock)
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

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
                    add(defaultSpaceWidgetView)
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
    fun `should emit list without elements, library and bin`() = runTest {

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
            layout = Block.Content.Widget.Layout.LIST,
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
        stubOpenWidgetObject(givenObjectView)

        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubWidgetActiveView(widgetBlock)
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

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
                    add(defaultSpaceWidgetView)
                    add(
                        WidgetView.SetOfObjects(
                            id = widgetBlock.id,
                            source = Widget.Source.Default(sourceObject),
                            elements = emptyList(),
                            isExpanded = true,
                            isCompact = false,
                            tabs = emptyList()
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
    fun `should emit compact list without elements, library and bin`() = runTest {

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
            layout = Block.Content.Widget.Layout.COMPACT_LIST,
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
        stubOpenWidgetObject(givenObjectView)

        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubWidgetActiveView(widgetBlock)
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

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
                    add(defaultSpaceWidgetView)
                    add(
                        WidgetView.SetOfObjects(
                            id = widgetBlock.id,
                            source = Widget.Source.Default(sourceObject),
                            elements = emptyList(),
                            isExpanded = true,
                            isCompact = true,
                            tabs = emptyList()
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
    fun `should emit three bundled widgets with tree layout, each having 2 elements, library and bin`() =
        runTest {

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
            stubOpenWidgetObject(givenObjectView)

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
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
                ),
                results = listOf(firstLink, secondLink)
            )

            val firstLinkToObjectFavoriteObjectBlock = StubLinkToObjectBlock(
                target = firstLink.id
            )

            val secondLinkToObjectFavoriteObjectBlock = StubLinkToObjectBlock(
                target = secondLink.id
            )

            stubFavoritesObjectWatcher(
                objectView = StubObjectView(
                    root = defaultSpaceConfig.home,
                    blocks = listOf(
                        StubSmartBlock(
                            id = defaultSpaceConfig.home,
                            children = listOf(
                                firstLinkToObjectFavoriteObjectBlock.id,
                                secondLinkToObjectFavoriteObjectBlock.id
                            )
                        ),
                        firstLinkToObjectFavoriteObjectBlock,
                        secondLinkToObjectFavoriteObjectBlock
                    )
                )
            )

            stubSearchByIds(
                subscription = BundledWidgetSourceIds.FAVORITE,
                targets = listOf(firstLink.id, secondLink.id),
                results = listOf(firstLink, secondLink),
                keys = TreeWidgetContainer.keys,
            )

            stubSearchByIds(
                subscription = favoriteWidgetBlock.id,
                targets = listOf(firstLink.id, secondLink.id),
                results = listOf(firstLink, secondLink),
                keys = TreeWidgetContainer.keys
            )

            stubDefaultSearch(
                params = ListWidgetContainer.params(
                    subscription = BundledWidgetSourceIds.RECENT,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
                ),
                results = listOf(firstLink, secondLink)
            )

            stubGetSpaceView(defaultSpaceConfig.spaceView)

            stubDefaultSearch(
                params = ListWidgetContainer.params(
                    subscription = BundledWidgetSourceIds.SETS,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
                ),
                results = listOf(firstLink, secondLink)
            )

            stubCollapsedWidgetState(any())
            stubGetWidgetSession()
            stubWidgetActiveView(favoriteWidgetBlock)

            stubSpaceManager()
            stubSpaceWidgetContainer(defaultSpaceWidgetView)
            stubUserPermission()

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
                        add(defaultSpaceWidgetView)
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
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

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
                    add(defaultSpaceWidgetView)
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
            verify(openObject, times(1)).stream(
                OpenObject.Params(
                    obj = WIDGET_OBJECT_ID,
                    saveAsLastOpened = false,
                    spaceId = SpaceId(defaultSpaceConfig.space)
                )
            )
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
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSaveWidgetSession()
        stubGetDefaultPageType()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)

        storelessSubscriptionContainer.stub {
            onBlocking {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = HomeScreenViewModel.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                        targets = listOf(defaultSpaceConfig.spaceView),
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

        stubDeleteWidget(widgetBlock, givenPayload)

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
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubDeleteWidget(widgetBlock, givenPayload)

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
            stubOpenWidgetObject(givenObjectView)
            stubSearchByIds(
                subscription = widgetBlock.id,
                targets = emptyList()
            )
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()
            stubCloseObject()
            stubSpaceManager()
            stubSpaceWidgetContainer(defaultSpaceWidgetView)

            val vm = buildViewModel()

            // TESTING

            vm.onStart()

            advanceUntilIdle()

            vm.onStop()

            advanceUntilIdle()

            verifyBlocking(unsubscriber, times(1)) {
                unsubscribe(
                    subscriptions = listOf(
                        widgetBlock.id,
                        SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION
                    )
                )
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
        stubOpenWidgetObject(givenObjectView)

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
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = ListWidgetContainer.keys,
                limit = WidgetConfig.DEFAULT_LIST_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = ListWidgetContainer.keys,
                limit = WidgetConfig.DEFAULT_LIST_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = ListWidgetContainer.keys,
                limit = WidgetConfig.DEFAULT_LIST_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        given(objectWatcher.watch(any())).willReturn(flowOf())
        given(storelessSubscriptionContainer.subscribe(any<StoreSearchParams>())).willReturn(flowOf())
        given(storelessSubscriptionContainer.subscribe(any<StoreSearchByIdsParams>())).willReturn(
            flowOf()
        )
        stubCloseObject()

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        vm.onStop()

        advanceUntilIdle()

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(
                subscriptions = listOf(
                    favoriteSource.id,
                    recentSource.id,
                    setsSource.id,
                    SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION
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
        stubOpenWidgetObject(givenObjectView)

        val firstLinkToObjectFavoriteObjectBlock = StubLinkToObjectBlock(
            target = firstLink.id
        )

        val secondLinkToObjectFavoriteObjectBlock = StubLinkToObjectBlock(
            target = secondLink.id
        )

        stubFavoritesObjectWatcher(
            objectView = StubObjectView(
                root = defaultSpaceConfig.home,
                blocks = listOf(
                    StubSmartBlock(
                        id = defaultSpaceConfig.home,
                        children = listOf(
                            firstLinkToObjectFavoriteObjectBlock.id,
                            secondLinkToObjectFavoriteObjectBlock.id
                        )
                    ),
                    firstLinkToObjectFavoriteObjectBlock,
                    secondLinkToObjectFavoriteObjectBlock
                )
            )
        )

        stubSearchByIds(
            subscription = BundledWidgetSourceIds.FAVORITE,
            targets = listOf(firstLink.id, secondLink.id),
            results = listOf(firstLink, secondLink),
            keys = TreeWidgetContainer.keys,
        )

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
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.FAVORITE,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = TreeWidgetContainer.keys,
                limit = WidgetConfig.NO_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = TreeWidgetContainer.keys,
                limit = WidgetConfig.NO_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubGetSpaceView(defaultSpaceConfig.spaceView)

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = TreeWidgetContainer.keys,
                limit = WidgetConfig.NO_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)
        stubCloseObject()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on launch

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                StoreSearchByIdsParams(
                    subscription = favoriteSource.id,
                    targets = listOf(firstLink.id, secondLink.id),
                    keys = TreeWidgetContainer.keys,
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
                )
            )
        }

        vm.onStop()

        advanceUntilIdle()

        // Verifying unsubscribe behavior

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(
                subscriptions = listOf(
                    favoriteSource.id,
                    recentSource.id,
                    setsSource.id,
                    SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION
                )
            )
        }

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on resume

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                StoreSearchByIdsParams(
                    subscription = favoriteSource.id,
                    targets = listOf(firstLink.id, secondLink.id),
                    keys = TreeWidgetContainer.keys,
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = TreeWidgetContainer.keys,
                    limit = WidgetConfig.NO_LIMIT
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
        stubOpenWidgetObject(givenObjectView)

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

        stubSearchByIds(
            subscription = favoriteSource.id,
            keys = ListWidgetContainer.keys,
            targets = emptyList()
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.FAVORITE,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = ListWidgetContainer.keys,
                limit = WidgetConfig.DEFAULT_LIST_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.RECENT,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = ListWidgetContainer.keys,
                limit = WidgetConfig.DEFAULT_LIST_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubGetSpaceView(defaultSpaceConfig.spaceView)

        stubDefaultSearch(
            params = ListWidgetContainer.params(
                subscription = BundledWidgetSourceIds.SETS,
                spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                keys = ListWidgetContainer.keys,
                limit = WidgetConfig.DEFAULT_LIST_LIMIT
            ),
            results = listOf(firstLink, secondLink)
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubWidgetActiveView(favoriteWidgetBlock)
        stubFavoritesObjectWatcher()
        stubCloseObject()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)

        val vm = buildViewModel()

        // TESTING

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on launch

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                StoreSearchByIdsParams(
                    subscription = favoriteSource.id,
                    keys = ListWidgetContainer.keys,
                    targets = emptyList()
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = ListWidgetContainer.keys,
                    limit = WidgetConfig.DEFAULT_LIST_LIMIT
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(1)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = ListWidgetContainer.keys,
                    limit = WidgetConfig.DEFAULT_LIST_LIMIT
                )
            )
        }

        vm.onStop()

        advanceUntilIdle()

        // Verifying unsubscribe behavior

        verifyBlocking(unsubscriber, times(1)) {
            unsubscribe(
                subscriptions = listOf(
                    favoriteSource.id,
                    recentSource.id,
                    setsSource.id,
                    SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION
                )
            )
        }

        vm.onStart()

        advanceUntilIdle()

        // Verifying subscription on resume

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                StoreSearchByIdsParams(
                    subscription = favoriteSource.id,
                    keys = ListWidgetContainer.keys,
                    targets = emptyList()
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = setsSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = ListWidgetContainer.keys,
                    limit = WidgetConfig.DEFAULT_LIST_LIMIT
                )
            )
        }

        verifyBlocking(storelessSubscriptionContainer, times(2)) {
            subscribe(
                ListWidgetContainer.params(
                    subscription = recentSource.id,
                    spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace),
                    keys = ListWidgetContainer.keys,
                    limit = WidgetConfig.DEFAULT_LIST_LIMIT
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
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())

        stubObserveSpaceObject()
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)
        stubUserPermission()

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
                    add(defaultSpaceWidgetView)
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
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)

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
            stubOpenWidgetObject(givenObjectView)
            stubSearchByIds(
                subscription = widgetBlock.id,
                targets = emptyList()
            )
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()
            stubSpaceManager()
            stubSpaceWidgetContainer(defaultSpaceWidgetView)

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

    @Test
    fun `should react to change-widget-source event when source type is page for old and new source`() =
        runTest {

            val currentSourceObject = StubObject(
                id = "SOURCE OBJECT 1",
                links = emptyList(),
                objectType = ObjectTypeIds.PAGE
            )

            val newSourceObject = StubObject(
                id = "SOURCE OBJECT 2",
                links = emptyList(),
                objectType = ObjectTypeIds.PAGE
            )

            val sourceLink = StubLinkToObjectBlock(
                id = "SOURCE LINK",
                target = currentSourceObject.id
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
                    currentSourceObject.id to currentSourceObject.map
                )
            )

            stubConfig()
            stubInterceptEvents(
                events = flow {
                    delay(300)
                    emit(
                        listOf(
                            Event.Command.LinkGranularChange(
                                context = WIDGET_OBJECT_ID,
                                id = sourceLink.id,
                                target = newSourceObject.id
                            )
                        )
                    )
                }
            )
            stubOpenWidgetObject(givenObjectView)
            stubSearchByIds(
                subscription = widgetBlock.id,
                targets = emptyList()
            )
            stubCollapsedWidgetState(any())
            stubGetWidgetSession()
            stubSpaceManager()
            stubSpaceWidgetContainer(defaultSpaceWidgetView)

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
                assertTrue {
                    val secondWidget = secondTimeItem[1]
                    secondWidget is WidgetView.Tree && secondWidget.source.id == currentSourceObject.id
                }
                val thirdTimeItem = awaitItem()
                advanceUntilIdle()
                assertTrue {
                    val secondWidget = thirdTimeItem[1]
                    secondWidget is WidgetView.Tree && secondWidget.source.id == newSourceObject.id
                }
            }
        }

    @Test
    fun `should react to change-widget-layout event when tree changed to link`() = runTest {

        val currentSourceObject = StubObject(
            id = "SOURCE OBJECT 1",
            links = emptyList(),
            objectType = ObjectTypeIds.PAGE
        )

        val sourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = currentSourceObject.id
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
                currentSourceObject.id to currentSourceObject.map
            )
        )

        stubConfig()
        stubInterceptEvents(
            events = flow {
                delay(300)
                emit(
                    listOf(
                        Event.Command.Widgets.SetWidget(
                            context = WIDGET_OBJECT_ID,
                            widget = widgetBlock.id,
                            layout = Block.Content.Widget.Layout.LINK,
                        )
                    )
                )
            }
        )
        stubOpenWidgetObject(givenObjectView)
        stubSearchByIds(
            subscription = widgetBlock.id,
            targets = emptyList()
        )
        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)

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
            assertTrue {
                val firstWidget = secondTimeItem[1]
                firstWidget is WidgetView.Tree
            }
            val thirdTimeItem = awaitItem()
            advanceUntilIdle()
            assertTrue {
                val firstWidget = thirdTimeItem[1]
                firstWidget is WidgetView.Link
            }
        }
    }

    @Test
    fun `should not re-fetch data after updating active view locally and then on mw`() = runTest {

        val currentWidgetSourceObject = StubObject(
            id = "SOURCE OBJECT 1",
            links = emptyList(),
            objectType = ObjectTypeIds.SET
        )

        val widgetSourceLink = StubLinkToObjectBlock(
            id = "SOURCE LINK",
            target = currentWidgetSourceObject.id
        )

        val widgetBlock = StubWidgetBlock(
            id = "WIDGET BLOCK",
            layout = Block.Content.Widget.Layout.LIST,
            children = listOf(widgetSourceLink.id)
        )

        val smartWidgetBlock = StubSmartBlock(
            id = WIDGET_OBJECT_ID,
            children = listOf(widgetBlock.id),
        )

        val givenWidgetObjectView = StubObjectView(
            root = WIDGET_OBJECT_ID,
            blocks = listOf(
                smartWidgetBlock,
                widgetBlock,
                widgetSourceLink
            ),
            details = mapOf(
                currentWidgetSourceObject.id to currentWidgetSourceObject.map
            )
        )

        val dataViewFirstView = StubDataViewView()
        val dataViewSecondView = StubDataViewView(
            filters = listOf(StubFilter())
        )

        val dataViewBlock = StubDataView(
            views = listOf(dataViewFirstView, dataViewSecondView)
        )

        val dataViewSmartBlock = StubSmartBlock(
            id = currentWidgetSourceObject.id,
            children = listOf(dataViewBlock.id)
        )

        val givenDataViewObjectView = StubObjectView(
            root = dataViewSmartBlock.id,
            blocks = listOf(
                dataViewSmartBlock,
                dataViewBlock
            ),
            details = mapOf(
                currentWidgetSourceObject.id to mapOf(
                    Relations.ID to currentWidgetSourceObject.map
                )
            )
        )

        stubConfig()
        stubInterceptEvents(events = emptyFlow())

        stubOpenWidgetObject(givenWidgetObjectView)
        stubGetObject(givenDataViewObjectView)

        stubWidgetActiveView(widgetBlock)

        stubSetWidgetActiveView(
            widget = widgetBlock.id,
            view = dataViewSecondView.id
        )

        val firstTimeParams = StoreSearchParams(
            subscription = widgetBlock.id,
            filters = buildList {
                addAll(
                    ObjectSearchConstants.defaultDataViewFilters(
                        spaces = listOf(defaultSpaceConfig.space, defaultSpaceConfig.techSpace)
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.TYPE_UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = listOf(
                            ObjectTypeIds.OBJECT_TYPE,
                            ObjectTypeIds.RELATION,
                            ObjectTypeIds.TEMPLATE,
                            ObjectTypeIds.DASHBOARD,
                            ObjectTypeIds.RELATION_OPTION,
                            ObjectTypeIds.DASHBOARD,
                            ObjectTypeIds.DATE
                        )
                    ),
                )
            },
            sorts = emptyList(),
            limit = WidgetConfig.DEFAULT_LIST_LIMIT,
            keys = buildList {
                addAll(ObjectSearchConstants.defaultDataViewKeys)
                add(Relations.DESCRIPTION)
            }.distinct(),
            source = currentWidgetSourceObject.setOf
        )

        // Params expected after switching active view
        val secondTimeParams = firstTimeParams.copy(
            filters = buildList {
                addAll(dataViewSecondView.filters)
                addAll(firstTimeParams.filters)
            }
        )

        stubDefaultSearch(
            params = firstTimeParams,
            results = emptyList()
        )

        stubDefaultSearch(
            params = secondTimeParams,
            results = emptyList()
        )

        stubCollapsedWidgetState(any())
        stubGetWidgetSession()
        stubGetDefaultPageType()
        stubObserveSpaceObject()

        stubSpaceManager()
        stubSpaceWidgetContainer(defaultSpaceWidgetView)

        // Using real implementation here
        activeViewStateHolder = WidgetActiveViewStateHolder.Impl()

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
            assertTrue {
                val secondWidget = secondTimeItem[1]
                secondWidget is WidgetView.SetOfObjects && secondWidget.tabs.first().isSelected
            }
            verifyBlocking(getObject, times(1)) {
                run(params = currentWidgetSourceObject.id)
            }
            verify(storelessSubscriptionContainer, times(1)).subscribe(
                StoreSearchByIdsParams(
                    subscription = HomeScreenViewModel.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                    targets = listOf(defaultSpaceConfig.profile),
                    keys = listOf(
                        Relations.ID,
                        Relations.NAME,
                        Relations.ICON_EMOJI,
                        Relations.ICON_IMAGE,
                        Relations.ICON_OPTION
                    )
                )
            )
            verify(storelessSubscriptionContainer, times(1)).subscribe(
                firstTimeParams
            )
            advanceUntilIdle()

            // Changing active view
            vm.onChangeCurrentWidgetView(
                widget = widgetBlock.id,
                view = dataViewSecondView.id
            )

            advanceUntilIdle()
            val thirdTimeItem = awaitItem()
            advanceUntilIdle()
            assertTrue {
                val secondWidget = thirdTimeItem[1]
                secondWidget is WidgetView.SetOfObjects && secondWidget.tabs.last().isSelected
            }
            verify(storelessSubscriptionContainer, times(1)).subscribe(
                secondTimeParams
            )
            verifyNoMoreInteractions(storelessSubscriptionContainer)
        }
    }

    private fun stubSetWidgetActiveView(
        widget: Id,
        view: Id,
    ) {
        setWidgetActiveView.stub {
            on {
                stream(
                    params = SetWidgetActiveView.Params(
                        ctx = defaultSpaceConfig.widgets,
                        widget = widget,
                        view = view
                    )
                )
            } doReturn flowOf(
                Resultat.Success(
                    Payload(
                        context = WIDGET_OBJECT_ID,
                        events = listOf(
                            Event.Command.Widgets.SetWidget(
                                context = WIDGET_OBJECT_ID,
                                widget = widget,
                                activeView = view
                            )
                        )
                    )
                )
            )
        }
    }

    private fun stubInterceptEvents(events: Flow<List<Event>>) {
        interceptEvents.stub {
            on { build(InterceptEvents.Params(WIDGET_OBJECT_ID)) } doReturn events
        }
    }

    private fun stubConfig() {
        configStorage.stub {
            on { get() } doReturn defaultSpaceConfig
        }
        configStorage.stub {
            on { getOrNull() } doReturn defaultSpaceConfig
        }
    }

    private fun stubOpenWidgetObject(givenObjectView: ObjectView) {
        openObject.stub {
            on {
                stream(
                    OpenObject.Params(
                        WIDGET_OBJECT_ID,
                        false,
                        SpaceId(defaultSpaceConfig.space)
                    )
                )
            } doReturn flowOf(
                Resultat.Success(
                    value = givenObjectView
                )
            )
        }
    }

    private fun stubGetObject(
        givenObjectView: ObjectView
    ) {
        getObject.stub {
            onBlocking {
                run(givenObjectView.root)
            } doReturn givenObjectView
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
                        subscription = HomeScreenViewModel.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                        targets = listOf(defaultSpaceConfig.spaceView),
                        keys = listOf(Relations.ID, Relations.ICON_EMOJI, Relations.ICON_IMAGE)
                    )
                )
            } doReturn emptyFlow()
        }
    }

    private fun stubFavoritesObjectWatcher(
        objectView: ObjectView = StubObjectView(root = defaultSpaceConfig.home)
    ) {
        objectWatcher.stub {
            on {
                watch(defaultSpaceConfig.home)
            } doReturn flowOf(objectView)
        }
    }

    private fun stubGetDefaultPageType(
        id: TypeId = TypeId(MockDataFactory.randomUuid()),
        type: TypeKey = TypeKey(MockDataFactory.randomUuid()),
        name: String = MockDataFactory.randomString(),
        template: String? = null
    ) {
        getDefaultObjectType.stub {
            onBlocking {
                execute(any())
            } doReturn Resultat.Success(
                GetDefaultObjectType.Response(
                    id = id,
                    type = type,
                    name = name,
                    defaultTemplate = template
                )
            )
        }
    }

    fun stubSpaceManager() {
        spaceManager.stub {
            on { observe() } doReturn flowOf(defaultSpaceConfig)
        }
        spaceManager.stub {
            onBlocking { get() } doReturn defaultSpaceConfig.space
        }
        spaceManager.stub {
            on { getConfig() } doReturn defaultSpaceConfig
        }
    }

    private fun stubGetWidgetSession(
        session: WidgetSession = WidgetSession(emptyList())
    ) {
        getWidgetSession.stub {
            onBlocking {
                async(any())
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

    private fun stubSpaceWidgetContainer(defaultSpaceWidgetView: WidgetView.SpaceWidget.View) {
        spaceWidgetContainer.stub {
            on {
                view
            } doReturn flowOf(
                defaultSpaceWidgetView
            )
        }
    }

    private fun stubGetSpaceView(
        spaceView: Id,
        objectWrapper: ObjectWrapper.Basic? = null
    ) {
        getSpaceView.stub {
            onBlocking {
                async(GetSpaceView.Params.BySpaceViewId(spaceView))
            } doReturn Resultat.success(objectWrapper)
        }
    }

    private fun stubGetPinnedObjectTypes() {
        getPinnedObjectTypes.stub {
            onBlocking {
                flow(any())
            } doReturn flowOf()
        }
    }

    private fun stubDeleteWidget(
        widgetBlock: Block,
        givenPayload: Payload
    ) {
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
    }

    private fun stubUserPermission(
        permission: SpaceMemberPermissions = SpaceMemberPermissions.OWNER
    ) {
        (userPermissionProvider as UserPermissionProviderStub).stubObserve(
            SpaceId(
                defaultSpaceConfig.space
            ), permission
        )
    }

    private fun buildViewModel() = HomeScreenViewModel(
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
        getDefaultObjectType = getDefaultObjectType,
        appActionManager = appActionManager,
        analytics = analytics,
        getWidgetSession = getWidgetSession,
        saveWidgetSession = saveWidgetSession,
        spaceGradientProvider = spaceGradientProvider,
        storeOfObjectTypes = storeOfObjectTypes,
        objectWatcher = objectWatcher,
        setWidgetActiveView = setWidgetActiveView,
        spaceWidgetContainer = spaceWidgetContainer,
        spaceManager = spaceManager,
        setObjectDetails = setObjectDetails,
        getSpaceView = getSpaceView,
        searchObjects = searchObjects,
        getPinnedObjectTypes = getPinnedObjectTypes,
        userPermissionProvider = userPermissionProvider,
        deepLinkToObjectDelegate = deepLinkToObjectDelegate
    )

    companion object {
        val WIDGET_OBJECT_ID: Id = MockDataFactory.randomUuid()
    }
}