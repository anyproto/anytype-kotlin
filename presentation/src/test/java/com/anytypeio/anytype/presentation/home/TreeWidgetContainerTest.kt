package com.anytypeio.anytype.presentation.home

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
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
import org.mockito.kotlin.verifyBlocking

class TreeWidgetContainerTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var objectWatcher: ObjectWatcher

    val dispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher
    )

    private lateinit var urlBuilder: UrlBuilder


    private val config = StubConfig()
    private val workspace = config.workspace

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway = gateway)
    }

    @Test
    fun `should search for data for source object links when no data is available about expanded branches`() =
        runTest {

            // SETUP

            val link1 = StubObject()
            val link2 = StubObject()
            val link3 = StubObject()
            val links = listOf(link1, link2, link3)

            val source = StubObject(
                links = links.map { it.id }
            )

            val widget = Widget.Tree(
                id = MockDataFactory.randomUuid(),
                source = Widget.Source.Default(source)
            )

            val expanded = flowOf(emptyList<TreePath>())

            val container = TreeWidgetContainer(
                container = storelessSubscriptionContainer,
                widget = widget,
                expandedBranches = expanded,
                isWidgetCollapsed = flowOf(false),
                urlBuilder = urlBuilder,
                workspace = workspace,
                isSessionActive = flowOf(true),
                config = config,
                objectWatcher = objectWatcher
            )

            stubObjectSearch(
                widget = widget,
                targets = links.map { it.id },
                results = links
            )

            // TESTING

            container.view.test {
                awaitItem()
                awaitComplete()
                verifyBlocking(storelessSubscriptionContainer, times(1)) {
                    subscribe(
                        StoreSearchByIdsParams(
                            subscription = widget.id,
                            targets = links.map { it.id },
                            keys = TreeWidgetContainer.keys
                        )
                    )
                }
            }
        }

    @Test
    fun `should search for data for source object links when it is expanded`() = runTest {

        // SETUP

        val link1 = StubObject(
            id = "A",
            links = listOf(
                "A1",
                "A2",
                "A3"
            )
        )
        val link2 = StubObject(id = "B")
        val link3 = StubObject(id = "C")

        val links = listOf(link1, link2, link3)

        val source = StubObject(
            id = "root",
            links = links.map { it.id }
        )

        val widget = Widget.Tree(
            id = "widget",
            source = Widget.Source.Default(source)
        )

        val expanded = flowOf(
            emptyList(),
            listOf(
                widget.id + "/" + widget.source.id + "/" + link1.id
            )
        )

        val container = TreeWidgetContainer(
            container = storelessSubscriptionContainer,
            widget = widget,
            expandedBranches = expanded,
            isWidgetCollapsed = flowOf(false),
            urlBuilder = urlBuilder,
            workspace = workspace,
            isSessionActive = flowOf(true),
            config = config,
            objectWatcher = objectWatcher
        )

        stubObjectSearch(
            widget = widget,
            targets = links.map { it.id },
            results = links
        )

        stubObjectSearch(
            widget = widget,
            targets = links.map { it.id } + link1.links,
            results = emptyList()
        )

        // TESTING

        container.view.test {
            awaitItem()
            awaitItem()
            awaitComplete()
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = widget.id,
                        targets = links.map { it.id },
                        keys = TreeWidgetContainer.keys
                    )
                )
            }
        }
    }

    @Test
    fun `should define correct indent level for children of children of source objects`() =
        runTest {

            // SETUP

            val layout = ObjectType.Layout.BASIC.code.toDouble()

            val linkA = StubObject(
                id = "A",
                links = listOf(
                    "A1",
                    "A2",
                    "A3"
                ),
                layout = layout
            )
            val linkA1 = StubObject(id = "A1", layout = layout)
            val linkA2 = StubObject(id = "A2", layout = layout)
            val linkA3 = StubObject(id = "A3", layout = layout)
            val linkB = StubObject(id = "B", layout = layout)
            val linkC = StubObject(id = "C", layout = layout)

            val sourceLinks = listOf(linkA, linkB, linkC)

            val source = StubObject(
                id = "root",
                links = sourceLinks.map { it.id }
            )

            val widget = Widget.Tree(
                id = "widget",
                source = Widget.Source.Default(source)
            )

            val delayBeforeExpanded = 100L

            val expanded = flow {
                emit(emptyList())
                delay(delayBeforeExpanded)
                emit(listOf(widget.id + "/" + widget.source.id + "/" + linkA.id))
            }

            val container = TreeWidgetContainer(
                container = storelessSubscriptionContainer,
                widget = widget,
                expandedBranches = expanded,
                isWidgetCollapsed = flowOf(false),
                urlBuilder = urlBuilder,
                workspace = workspace,
                isSessionActive = flowOf(true),
                config = config,
                objectWatcher = objectWatcher
            )

            stubObjectSearch(
                widget = widget,
                targets = sourceLinks.map { it.id },
                results = sourceLinks
            )

            stubObjectSearch(
                widget = widget,
                targets = sourceLinks.map { it.id } + linkA.links,
                results = sourceLinks + listOf(linkA1, linkA2, linkA3)
            )

            // TESTING

            container.view.test {
                val firstTimeState = awaitItem()
                assertEquals(
                    expected = WidgetView.Tree(
                        id = widget.id,
                        source = widget.source,
                        elements = listOf(
                            WidgetView.Tree.Element(
                                indent = 0,
                                obj = sourceLinks[0],
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[0].id,
                                elementIcon = WidgetView.Tree.ElementIcon.Branch(isExpanded = false),
                                objectIcon = ObjectIcon.Basic.Avatar(sourceLinks[0].name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 0,
                                obj = sourceLinks[1],
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[1].id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(sourceLinks[1].name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 0,
                                obj = sourceLinks[2],
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[2].id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(sourceLinks[2].name.orEmpty())
                            )
                        ),
                        isExpanded = true
                    ),
                    actual = firstTimeState
                )
                // Should be using coroutine delay instead, but it does not work currently.
                Thread.sleep(delayBeforeExpanded * 2)
                val secondTimeState = awaitItem()
                assertEquals(
                    expected = WidgetView.Tree(
                        id = widget.id,
                        source = widget.source,
                        elements = listOf(
                            WidgetView.Tree.Element(
                                indent = 0,
                                obj = sourceLinks[0],
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[0].id,
                                elementIcon = WidgetView.Tree.ElementIcon.Branch(isExpanded = true),
                                objectIcon = ObjectIcon.Basic.Avatar(sourceLinks[0].name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 1,
                                obj = linkA1,
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[0].id + "/" + linkA1.id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(linkA1.name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 1,
                                obj = linkA2,
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[0].id + "/" + linkA2.id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(linkA2.name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 1,
                                obj = linkA3,
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[0].id + "/" + linkA3.id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(linkA3.name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 0,
                                obj = sourceLinks[1],
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[1].id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(sourceLinks[1].name.orEmpty())
                            ),
                            WidgetView.Tree.Element(
                                indent = 0,
                                obj = sourceLinks[2],
                                path = widget.id + "/" + widget.source.id + "/" + sourceLinks[2].id,
                                elementIcon = WidgetView.Tree.ElementIcon.Leaf,
                                objectIcon = ObjectIcon.Basic.Avatar(sourceLinks[2].name.orEmpty())
                            )
                        ),
                        isExpanded = true
                    ),
                    actual = secondTimeState
                )
                awaitComplete()
            }
        }

    @Test
    fun `should start subscription with empty keys if source is archived`() = runTest {

        // SETUP

        val link1 = StubObject()
        val link2 = StubObject()
        val link3 = StubObject()
        val links = listOf(link1, link2, link3)

        val source = StubObject(
            links = links.map { it.id },
            isArchived = true
        )

        val widget = Widget.Tree(
            id = MockDataFactory.randomUuid(),
            source = Widget.Source.Default(source)
        )

        val expanded = flowOf(emptyList<TreePath>())

        val container = TreeWidgetContainer(
            container = storelessSubscriptionContainer,
            widget = widget,
            expandedBranches = expanded,
            isWidgetCollapsed = flowOf(false),
            urlBuilder = urlBuilder,
            workspace = workspace,
            isSessionActive = flowOf(true),
            config = config,
            objectWatcher = objectWatcher
        )

        stubObjectSearch(
            widget = widget,
            targets = emptyList(),
            results = links
        )

        // TESTING

        container.view.test {
            awaitItem()
            awaitComplete()
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = widget.id,
                        targets = emptyList(),
                        keys = TreeWidgetContainer.keys
                    )
                )
            }
        }
    }

    @Test
    fun `should start subscription with empty keys if source is deleted`() = runTest {

        // SETUP

        val link1 = StubObject()
        val link2 = StubObject()
        val link3 = StubObject()
        val links = listOf(link1, link2, link3)

        val source = StubObject(
            links = links.map { it.id },
            isDeleted = true
        )

        val widget = Widget.Tree(
            id = MockDataFactory.randomUuid(),
            source = Widget.Source.Default(source)
        )

        val expanded = flowOf(emptyList<TreePath>())

        val container = TreeWidgetContainer(
            container = storelessSubscriptionContainer,
            widget = widget,
            expandedBranches = expanded,
            isWidgetCollapsed = flowOf(false),
            urlBuilder = urlBuilder,
            workspace = workspace,
            isSessionActive = flowOf(true),
            config = config,
            objectWatcher = objectWatcher
        )

        stubObjectSearch(
            widget = widget,
            targets = emptyList(),
            results = links
        )

        // TESTING

        container.view.test {
            awaitItem()
            awaitComplete()
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = widget.id,
                        targets = emptyList(),
                        keys = TreeWidgetContainer.keys
                    )
                )
            }
        }
    }

    private fun stubObjectSearch(
        widget: Widget.Tree,
        targets: List<Id>,
        results: List<ObjectWrapper.Basic>,
    ) {
        storelessSubscriptionContainer.stub {
            on {
                subscribe(
                    StoreSearchByIdsParams(
                        subscription = widget.id,
                        targets = targets,
                        keys = TreeWidgetContainer.keys
                    )
                )
            } doReturn flowOf(results)
        }
    }
}