package com.anytypeio.anytype.presentation.home

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.widgets.RecentlyEditedWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class RecentlyEditedWidgetContainerTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var getSpaceView: GetSpaceView

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        urlBuilder = UrlBuilderImpl(gateway = gateway)
    }

    @Test
    fun `should unsubscribe when session becomes inactive`() = runTest {
        // SETUP
        val isSessionActive = MutableStateFlow(true)
        val isWidgetCollapsed = MutableStateFlow(false)

        val widget = createWidget()

        stubGetSpaceView()
        stubSubscription(emptyList())

        val container = RecentlyEditedWidgetContainer(
            widget = widget,
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            isWidgetCollapsed = isWidgetCollapsed,
            getSpaceView = getSpaceView,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            isSessionActive = isSessionActive
        )

        // TESTING
        container.view.test {
            // Initial emission when active
            awaitItem()
            awaitItem()

            // Make session inactive
            isSessionActive.value = false
            advanceUntilIdle()

            // Should unsubscribe
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                unsubscribe(eq(listOf(SUBSCRIPTION_ID)))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should unsubscribe when widget is collapsed`() = runTest {
        // SETUP
        val isSessionActive = MutableStateFlow(true)
        val isWidgetCollapsed = MutableStateFlow(false)

        val widget = createWidget()

        stubGetSpaceView()
        stubSubscription(emptyList())

        val container = RecentlyEditedWidgetContainer(
            widget = widget,
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            isWidgetCollapsed = isWidgetCollapsed,
            getSpaceView = getSpaceView,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            isSessionActive = isSessionActive
        )

        // TESTING
        container.view.test {
            // Initial emission when expanded
            awaitItem()
            awaitItem()

            // Collapse the widget
            isWidgetCollapsed.value = true
            advanceUntilIdle()
            
            // Wait for collapsed view
            awaitItem()

            // Should unsubscribe when collapsed
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                unsubscribe(eq(listOf(SUBSCRIPTION_ID)))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should not subscribe when widget starts collapsed`() = runTest {
        // SETUP
        val isSessionActive = MutableStateFlow(true)
        val isWidgetCollapsed = flowOf(true)

        val widget = createWidget()

        stubGetSpaceView()

        val container = RecentlyEditedWidgetContainer(
            widget = widget,
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            isWidgetCollapsed = isWidgetCollapsed,
            getSpaceView = getSpaceView,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            isSessionActive = isSessionActive
        )

        // TESTING
        container.view.test {
            val item = awaitItem()
            
            // Should emit collapsed view without subscribing
            assert(item is WidgetView.RecentlyEdited)
            assert(!(item as WidgetView.RecentlyEdited).isExpanded)

            // Verify subscribe was never called
            verifyBlocking(storelessSubscriptionContainer, times(0)) {
                subscribe(any<StoreSearchParams>())
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should not subscribe when session starts inactive`() = runTest {
        // SETUP
        val isSessionActive = flowOf(false)
        val isWidgetCollapsed = flowOf(false)

        val widget = createWidget()

        val container = RecentlyEditedWidgetContainer(
            widget = widget,
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            isWidgetCollapsed = isWidgetCollapsed,
            getSpaceView = getSpaceView,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            isSessionActive = isSessionActive
        )

        // TESTING
        container.view.test {
            // emptyFlow() completes immediately without emitting
            awaitComplete()

            // Verify subscribe was never called
            verifyBlocking(storelessSubscriptionContainer, times(0)) {
                subscribe(any<StoreSearchParams>())
            }
        }
    }

    private fun createWidget(): Widget.RecentlyEdited {
        return Widget.RecentlyEdited(
            id = MockDataFactory.randomUuid(),
            source = Widget.Source.Bundled.Recent,
            config = config,
            icon = ObjectIcon.None,
            sectionType = SectionType.RECENTLY_EDITED
        )
    }

    private fun stubGetSpaceView() {
        getSpaceView.stub {
            onBlocking {
                async(any())
            } doReturn Resultat.success(
                ObjectWrapper.Basic(
                    mapOf(
                        Relations.ID to config.spaceView,
                        Relations.CREATED_DATE to 0.0
                    )
                )
            )
        }
    }

    private fun stubSubscription(results: List<ObjectWrapper.Basic>) {
        storelessSubscriptionContainer.stub {
            on {
                subscribe(any<StoreSearchParams>())
            } doReturn flowOf(results)
        }
    }

    companion object {
        private const val SUBSCRIPTION_ID = "subscription.widget.recently_edited"
    }
}
