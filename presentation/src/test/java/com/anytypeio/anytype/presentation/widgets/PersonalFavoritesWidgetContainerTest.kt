package com.anytypeio.anytype.presentation.widgets

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.presentation.widgets.ObservePersonalFavoriteTargets
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
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
    lateinit var observePersonalFavoriteTargets: ObservePersonalFavoriteTargets

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
    fun `emits compact SetOfObjects with elements in target order`() = runTest {
        val widget = createWidget()
        stubTargets(listOf("obj-a", "obj-b"))
        stubSubscription(listOf(stubBasic("obj-a"), stubBasic("obj-b")))

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
    fun `emits empty SetOfObjects when no targets`() = runTest {
        val widget = createWidget()
        stubTargets(emptyList())

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

    @Test
    fun `subscribes to objects only by target IDs`() = runTest {
        val widget = createWidget()
        stubTargets(listOf("obj-a"))
        stubSubscription(listOf(stubBasic("obj-a")))

        val container = newContainer(widget = widget)

        container.view.test {
            val view = awaitItem() as WidgetView.SetOfObjects
            assertEquals(1, view.elements.size)
            cancelAndIgnoreRemainingEvents()
        }
        // The delegate-driven subscription is detailed elsewhere; here we just
        // confirm we got the expected element count for the single target.
    }

    /**
     * DROID-4397 Task 23: when a favorited object is deleted or moved to bin,
     * the object store's subscription stops emitting it. The container must
     * skip it in the final element list rather than rendering a stale row.
     */
    @Test
    fun `drops targets whose objects are missing from the store subscription`() = runTest {
        val widget = createWidget()
        stubTargets(listOf("obj-a", "deleted-obj", "obj-c"))
        // Store returns only obj-a and obj-c — deleted-obj is gone (archived or binned)
        stubSubscription(listOf(stubBasic("obj-a"), stubBasic("obj-c")))

        val container = newContainer(widget = widget)

        container.view.test {
            val view = awaitItem() as WidgetView.SetOfObjects
            assertEquals(listOf("obj-a", "obj-c"), view.elements.map { it.obj.id })
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
        observePersonalFavoriteTargets = observePersonalFavoriteTargets,
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

    private fun stubTargets(targets: List<String>) {
        observePersonalFavoriteTargets.stub {
            on { invoke(space) } doReturn flowOf(targets)
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
}
