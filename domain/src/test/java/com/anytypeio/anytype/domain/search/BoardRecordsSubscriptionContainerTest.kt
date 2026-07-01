package com.anytypeio.anytype.domain.search

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BoardRecordsSubscriptionContainerTest {

    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var channel: SubscriptionEventChannel

    @Mock
    lateinit var logger: Logger

    private lateinit var store: ObjectStore
    private lateinit var container: BoardRecordsSubscriptionContainer

    private val space = SpaceId(MockDataFactory.randomUuid())
    private val subA = "ctx-board-records-A"
    private val subB = "ctx-board-records-B"
    private val filterA = DVFilter(relation = "tag", condition = DVFilterCondition.IN, value = listOf("optA"))
    private val filterB = DVFilter(relation = "tag", condition = DVFilterCondition.IN, value = listOf("optB"))

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        store = DefaultObjectStore()
        container = BoardRecordsSubscriptionContainer(
            repo = repo,
            channel = channel,
            store = store,
            dispatchers = AppCoroutineDispatchers(
                io = rule.testDispatcher,
                main = rule.testDispatcher,
                computation = rule.testDispatcher
            ),
            logger = logger
        )
    }

    @Test
    fun `each column loads its own page and exposes the backend total`() = runTest {
        stubColumn(subA, filterA, SearchResult(objects("a1", "a2"), emptyList(), counter(120)))
        stubColumn(subB, filterB, SearchResult(objects("b1"), emptyList(), counter(1)))
        stubNoChannelEvents()

        container.observe(params()).test {
            val page = awaitItem()
            assertEquals(listOf("a1", "a2"), page.getValue("A").ids)
            assertEquals(120, page.getValue("A").total)
            assertEquals(listOf("b1"), page.getValue("B").ids)
            assertEquals(1, page.getValue("B").total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a live add updates only the affected column's ids and total`() = runTest {
        stubColumn(subA, filterA, SearchResult(objects("a1"), emptyList(), counter(1)))
        stubColumn(subB, filterB, SearchResult(objects("b1"), emptyList(), counter(1)))
        channel.stub {
            on { subscribe(listOf(subA)) } doReturn flow {
                emit(listOf(SubscriptionEvent.Add(subscription = subA, afterId = "a1", target = "a2")))
            }
            on { subscribe(listOf(subB)) } doReturn emptyFlow()
        }

        container.observe(params()).test {
            assertEquals(listOf("a1"), awaitItem().getValue("A").ids)
            val updated = awaitItem()
            assertEquals(listOf("a1", "a2"), updated.getValue("A").ids)
            assertEquals(2, updated.getValue("A").total)
            assertEquals(listOf("b1"), updated.getValue("B").ids)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMore grows only the requested column`() = runTest {
        // Column A: 1 of 2 loaded at the default limit; B: 1 of 1.
        stubColumn(subA, filterA, SearchResult(objects("a1"), emptyList(), counter(2)), limit = 50)
        // After loadMore, A re-subscribes at the larger limit and gets both records.
        stubColumn(subA, filterA, SearchResult(objects("a1", "a2"), emptyList(), counter(2)), limit = 100)
        stubColumn(subB, filterB, SearchResult(objects("b1"), emptyList(), counter(1)), limit = 50)
        stubNoChannelEvents()

        container.observe(params()).test {
            assertEquals(listOf("a1"), awaitItem().getValue("A").ids)

            container.loadMore("A", additional = 50)

            val grown = awaitItem()
            assertEquals(listOf("a1", "a2"), grown.getValue("A").ids)
            // B was not re-fetched.
            assertEquals(listOf("b1"), grown.getValue("B").ids)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a content amend on a loaded card bumps the revision to force a board re-render`() = runTest {
        // Single column so combine() can't conflate the seed + amended emissions.
        stubColumn(subA, filterA, SearchResult(objects("a1"), emptyList(), counter(1)))
        channel.stub {
            on { subscribe(listOf(subA)) } doReturn flow {
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            target = "a1",
                            diff = mapOf(Relations.NAME to "renamed"),
                            subscriptions = listOf(subA)
                        )
                    )
                )
            }
        }

        container.observe(singleColumnParams()).test {
            val initial = awaitItem().getValue("A")
            assertEquals(listOf("a1"), initial.ids)
            val amended = awaitItem().getValue("A")
            // ids/total are unchanged, but the revision must bump so the downstream StateFlow
            // (which dedups equal values) re-emits and the board re-renders the new name.
            assertEquals(listOf("a1"), amended.ids)
            assertEquals(initial.total, amended.total)
            assertTrue(amended.revision > initial.revision)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a metadata-only amend does not bump the revision`() = runTest {
        stubColumn(subA, filterA, SearchResult(objects("a1"), emptyList(), counter(1)))
        channel.stub {
            on { subscribe(listOf(subA)) } doReturn flow {
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            target = "a1",
                            // Only bookkeeping the card never renders → must not re-render the board.
                            diff = mapOf(Relations.LAST_MODIFIED_DATE to 123.0),
                            subscriptions = listOf(subA)
                        )
                    )
                )
            }
        }

        container.observe(singleColumnParams()).test {
            val initial = awaitItem().getValue("A")
            val amended = awaitItem().getValue("A")
            assertEquals(initial.revision, amended.revision)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun singleColumnParams() = BoardRecordsSubscriptionContainer.Params(
        space = space,
        columns = listOf(
            BoardRecordsSubscriptionContainer.Column(subscription = subA, columnId = "A", filter = filterA)
        ),
        sorts = emptyList(),
        baseFilters = emptyList(),
        keys = emptyList(),
        source = emptyList(),
        collection = null,
        limit = 50
    )

    // Exact-arg stubbing (matchers can't be used: SpaceId is an inline value class and any()
    // would unbox null). Args mirror what observeColumn passes.
    private fun stubColumn(subId: String, filter: DVFilter, result: SearchResult, limit: Int = 50) {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = space,
                    subscription = subId,
                    sorts = emptyList(),
                    filters = listOf(filter),
                    keys = emptyList(),
                    source = emptyList(),
                    offset = 0,
                    limit = limit,
                    beforeId = null,
                    afterId = null,
                    ignoreWorkspace = null,
                    noDepSubscription = null,
                    collection = null
                )
            } doReturn result
        }
    }

    private fun stubNoChannelEvents() {
        channel.stub {
            on { subscribe(listOf(subA)) } doReturn emptyFlow()
            on { subscribe(listOf(subB)) } doReturn emptyFlow()
        }
    }

    private fun params() = BoardRecordsSubscriptionContainer.Params(
        space = space,
        columns = listOf(
            BoardRecordsSubscriptionContainer.Column(subscription = subA, columnId = "A", filter = filterA),
            BoardRecordsSubscriptionContainer.Column(subscription = subB, columnId = "B", filter = filterB)
        ),
        sorts = emptyList(),
        baseFilters = emptyList(),
        keys = emptyList(),
        source = emptyList(),
        collection = null,
        limit = 50
    )

    private fun objects(vararg ids: String): List<ObjectWrapper.Basic> =
        ids.map { ObjectWrapper.Basic(mapOf(Relations.ID to it)) }

    private fun counter(total: Int) = SearchResult.Counter(total = total, prev = 0, next = total - 1)
}
