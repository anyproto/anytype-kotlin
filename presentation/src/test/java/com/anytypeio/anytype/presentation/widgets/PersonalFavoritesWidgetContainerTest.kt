package com.anytypeio.anytype.presentation.widgets

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalFavoritesWidgetContainerTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var openObject: OpenObject

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var storage: StorelessSubscriptionContainer

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    private lateinit var urlBuilder: UrlBuilder
    private lateinit var fieldParser: FieldParser

    private val config = StubConfig()
    private val space = SpaceId("space-42.context")

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        urlBuilder = UrlBuilderImpl(gateway = gateway)
    }

    @Test
    fun `emits compact SetOfObjects with elements in wrapper-child order`() = runTest {
        val widget = createWidget()
        val personalId = personalWidgetsId(space)
        stubOpenObject(
            docId = personalId,
            tree = objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink("w-1", "l-1", "obj-a"),
                    WrapperLink("w-2", "l-2", "obj-b")
                )
            )
        )
        stubInterceptEvents(personalId, emptyFlow())
        stubSubscription(
            listOf(stubBasic("obj-a"), stubBasic("obj-b"))
        )

        val container = newContainer(widget = widget)

        container.view.test {
            val view = awaitItem()
            assertTrue(view is WidgetView.SetOfObjects)
            assertTrue(view.isCompact)
            assertEquals(widget.id, view.id)
            assertEquals(2, view.elements.size)
            assertEquals("obj-a", view.elements[0].obj.id)
            assertEquals("obj-b", view.elements[1].obj.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filters out built-in link targets`() = runTest {
        val widget = createWidget()
        val personalId = personalWidgetsId(space)
        stubOpenObject(
            docId = personalId,
            tree = objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink("w-1", "l-1", "favorite"),       // built-in
                    WrapperLink("w-2", "l-2", "obj-real"),       // real
                    WrapperLink("w-3", "l-3", "allObjects")      // built-in
                )
            )
        )
        stubInterceptEvents(personalId, emptyFlow())
        stubSubscription(listOf(stubBasic("obj-real")))

        val container = newContainer(widget = widget)

        container.view.test {
            val view = awaitItem() as WidgetView.SetOfObjects
            assertEquals(listOf("obj-real"), view.elements.map { it.obj.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits empty SetOfObjects when there are no wrappers`() = runTest {
        val widget = createWidget()
        val personalId = personalWidgetsId(space)
        stubOpenObject(
            docId = personalId,
            tree = objectViewWith(wrapperLinks = emptyList())
        )
        stubInterceptEvents(personalId, emptyFlow())

        val container = newContainer(widget = widget)

        container.view.test {
            val view = awaitItem() as WidgetView.SetOfObjects
            assertTrue(view.elements.isEmpty())
            assertTrue(view.isCompact)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits empty SetOfObjects when session is inactive`() = runTest {
        val widget = createWidget()
        val container = newContainer(
            widget = widget,
            isSessionActiveFlow = flowOf(false)
        )

        container.view.test {
            val view = awaitItem() as WidgetView.SetOfObjects
            assertTrue(view.elements.isEmpty())
            assertTrue(view.isCompact)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- helpers ---

    private fun newContainer(
        widget: Widget.PersonalFavorites,
        isSessionActiveFlow: kotlinx.coroutines.flow.Flow<Boolean> = MutableStateFlow(true)
    ) = PersonalFavoritesWidgetContainer(
        space = space,
        widget = widget,
        openObject = openObject,
        interceptEvents = interceptEvents,
        storage = storage,
        urlBuilder = urlBuilder,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes,
        isSessionActiveFlow = isSessionActiveFlow
    )

    private fun createWidget() = Widget.PersonalFavorites(
        id = MockDataFactory.randomUuid(),
        source = Widget.Source.Bundled.PersonalFavorites,
        config = config,
        icon = ObjectIcon.None,
        sectionType = SectionType.MY_FAVORITES
    )

    private fun stubOpenObject(docId: String, tree: ObjectView) {
        openObject.stub {
            onBlocking { run(any()) } doReturn tree
        }
    }

    private fun stubInterceptEvents(
        docId: String,
        events: kotlinx.coroutines.flow.Flow<List<com.anytypeio.anytype.core_models.Event>>
    ) {
        interceptEvents.stub {
            on { build(any()) } doReturn events
        }
    }

    private fun stubSubscription(results: List<ObjectWrapper.Basic>) {
        storage.stub {
            on { subscribe(any<StoreSearchParams>()) } doReturn flowOf(results)
        }
    }

    private fun stubBasic(id: String): ObjectWrapper.Basic = ObjectWrapper.Basic(
        mapOf(
            Relations.ID to id,
            Relations.NAME to id
        )
    )

    private data class WrapperLink(val wrapperId: String, val linkId: String, val target: String)

    private fun objectViewWith(
        root: String = "root",
        wrapperLinks: List<WrapperLink>
    ): ObjectView {
        val rootBlock = Block(
            id = root,
            children = wrapperLinks.map { it.wrapperId },
            content = Block.Content.Smart,
            fields = Block.Fields(emptyMap())
        )
        val wrappers = wrapperLinks.map { wl ->
            Block(
                id = wl.wrapperId,
                children = listOf(wl.linkId),
                content = Block.Content.Widget(
                    layout = Block.Content.Widget.Layout.LINK
                ),
                fields = Block.Fields(emptyMap())
            )
        }
        val links = wrapperLinks.map { wl ->
            Block(
                id = wl.linkId,
                children = emptyList(),
                content = Block.Content.Link(
                    target = wl.target,
                    type = Block.Content.Link.Type.PAGE,
                    iconSize = Block.Content.Link.IconSize.NONE,
                    cardStyle = Block.Content.Link.CardStyle.TEXT,
                    description = Block.Content.Link.Description.NONE
                ),
                fields = Block.Fields(emptyMap())
            )
        }
        return ObjectView(
            root = root,
            blocks = listOf(rootBlock) + wrappers + links,
            details = emptyMap(),
            objectRestrictions = emptyList(),
            dataViewRestrictions = emptyList()
        )
    }
}
