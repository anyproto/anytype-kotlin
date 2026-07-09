package com.anytypeio.anytype.domain.subscriptions

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectMinim
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyNoInteractions

class StorelessSubscriptionContainerTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var eventChannel: SubscriptionEventChannel

    private lateinit var container: StorelessSubscriptionContainer.Impl

    private val appCoroutineTestDispatchers = AppCoroutineDispatchers(
        io = UnconfinedTestDispatcher(),
        main = UnconfinedTestDispatcher(),
        computation = UnconfinedTestDispatcher()
    )

    private val defaultSpaceId = SpaceId(
        MockDataFactory.randomUuid()
    )

    private val defaultSearchParams = StoreSearchParams(
        space = defaultSpaceId,
        filters = emptyList(),
        sorts = emptyList(),
        subscription = MockDataFactory.randomUuid(),
        keys = listOf(Relations.ID, Relations.NAME)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        container = StorelessSubscriptionContainer.Impl(
            repo = repo,
            channel = eventChannel,
            logger = TestLogger,
            dispatchers = appCoroutineTestDispatchers
        )
    }

    @Test
    fun `should emit two objects from initial results`() = runTest {

        val obj1 = StubObjectMinim()
        val obj2 = StubObjectMinim()
        val givenResults = listOf(obj1, obj2)

        stubSearchWithSubscription(
            results = givenResults,
            params = defaultSearchParams
        )
        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = emptyFlow()
        )

        container.subscribe(defaultSearchParams).test {
            val first = awaitItem()
            assertEquals(
                expected = givenResults,
                actual = first
            )
            awaitComplete()
        }
    }

    @Test
    fun `should emit empty list without hitting repo when targets are empty`() = runTest {

        val params = StoreSearchByIdsParams(
            space = defaultSpaceId,
            subscription = MockDataFactory.randomUuid(),
            keys = listOf(Relations.ID, Relations.NAME),
            targets = emptyList()
        )

        container.subscribe(params).test {
            assertEquals(
                expected = emptyList<ObjectWrapper.Basic>(),
                actual = awaitItem()
            )
            awaitComplete()
        }

        verifyNoInteractions(repo)
    }

    @Test
    fun `should add one object to objects from initial results after consuming add-event and set-event`() = runTest {

        val obj1 = StubObjectMinim(id = "1", name = "Walter Benjamin")
        val obj2 = StubObjectMinim(id = "2", name = "Aby Warburg")
        val addedObject = StubObjectMinim(id = "3", name = "Aloïs Riegl")

        val initialResults = listOf(obj1, obj2)
        val resultAfterEvents = listOf(obj1, obj2, addedObject)

        val events = buildList {
            add(
                SubscriptionEvent.Add(
                    subscription = defaultSearchParams.subscription,
                    afterId = obj2.id,
                    target = addedObject.id
                )
            )
            add(
                SubscriptionEvent.Set(
                    subscriptions = listOf(defaultSearchParams.subscription),
                    target = addedObject.id,
                    data = addedObject.map
                )
            )
        }

        stubSearchWithSubscription(
            results = initialResults,
            params = defaultSearchParams
        )
        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = flowOf(events)
        )

        container.subscribe(defaultSearchParams).test {
            val first = awaitItem()
            assertEquals(
                expected = initialResults,
                actual = first
            )
            val second = awaitItem()
            assertEquals(
                expected = resultAfterEvents,
                actual = second
            )
            awaitComplete()
        }
    }

    @Test
    fun `should add one object to objects from initial results after consuming add-event, amend-event and then set-event`() = runTest {

        val obj1 = StubObjectMinim(id = "1", name = "Walter Benjamin")
        val obj2 = StubObjectMinim(id = "2", name = "Aby Warburg")
        val addedObject = StubObjectMinim(id = "3", name = "")
        val addedObjectAfterUpdate = StubObjectMinim(id = "3", name = "Erwin Panofsky")

        val initialResults = listOf(obj1, obj2)
        val resultAfterEvents = listOf(obj1, obj2, addedObjectAfterUpdate)

        val events = buildList {
            add(
                SubscriptionEvent.Add(
                    subscription = defaultSearchParams.subscription,
                    afterId = obj2.id,
                    target = addedObject.id
                )
            )
            add(
                SubscriptionEvent.Set(
                    subscriptions = listOf(defaultSearchParams.subscription),
                    target = addedObject.id,
                    data = addedObject.map
                )
            )
            add(
                SubscriptionEvent.Amend(
                    subscriptions = listOf(defaultSearchParams.subscription),
                    target = addedObject.id,
                    diff = mapOf(
                        Relations.NAME to "Erwin Panofsky"
                    )
                )
            )
        }

        stubSearchWithSubscription(
            results = initialResults,
            params = defaultSearchParams
        )
        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = flowOf(events)
        )

        container.subscribe(defaultSearchParams).test {
            val first = awaitItem()
            assertEquals(
                expected = initialResults,
                actual = first
            )
            val second = awaitItem()
            assertEquals(
                expected = resultAfterEvents,
                actual = second
            )
            awaitComplete()
        }
    }

    @Test
    fun `should update one object from initial results after consuming amend event`() = runTest {

        val obj1 = StubObjectMinim(id = "1", name = "Heinrich")
        val obj2 = StubObjectMinim(id = "2", name = "Aby Warburg")
        val updatedName = "Heinrich Wölfflin"
        val obj2Updated = ObjectWrapper.Basic(
            obj2.map.toMutableMap().apply {
                set(Relations.NAME, updatedName)
            }
        )

        val initialResults = listOf(obj1, obj2)
        val resultsAfterEvents = listOf(obj1, obj2Updated)

        val events = buildList {
            add(
                SubscriptionEvent.Amend(
                    subscriptions = listOf(defaultSearchParams.subscription),
                    target = obj2.id,
                    diff = mapOf(
                        Relations.NAME to updatedName
                    )
                )
            )
        }

        stubSearchWithSubscription(
            results = initialResults,
            params = defaultSearchParams
        )
        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = flowOf(events)
        )

        container.subscribe(defaultSearchParams).test {
            val first = awaitItem()
            assertEquals(
                expected = initialResults,
                actual = first
            )
            val second = awaitItem()
            assertEquals(
                expected = resultsAfterEvents,
                actual = second
            )
            awaitComplete()
        }
    }

    @Test
    fun `should resolve into consistent state even if event order is unexpected`() = runTest {

        // SET, ADD, COUNTERS + SET

        val subscription = defaultSearchParams.subscription

        val obj = StubObject()

        val firstTimeEvents = buildList {
            add(
                SubscriptionEvent.Set(
                    subscriptions = listOf(subscription),
                    target = obj.id,
                    data = obj.map
                )
            )
            add(
                SubscriptionEvent.Counter(
                    counter = SearchResult.Counter(
                        total = 1,
                        prev = 0,
                        next = 0
                    )
                )
            )
            add(
                SubscriptionEvent.Add(
                    subscription = subscription,
                    afterId = null,
                    target = obj.id
                )
            )
        }

        val secondTimeEvent = buildList {
            add(
                SubscriptionEvent.Set(
                    subscriptions = listOf(subscription),
                    target = obj.id,
                    data = obj.map
                )
            )
        }

        stubSearchWithSubscription(
            results = emptyList(),
            params = defaultSearchParams
        )

        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = flow {
                emit(firstTimeEvents)
                emit(secondTimeEvent)
            }
        )

        container.subscribe(defaultSearchParams).test {
            val first = awaitItem()
            assertEquals(
                expected = emptyList(),
                actual = first
            )
            val second = awaitItem()
            assertEquals(
                expected = listOf(obj),
                actual = second
            )
            val third = awaitItem()
            assertEquals(
                expected = listOf(obj),
                actual = third
            )
            awaitComplete()
        }
    }

    @Test
    fun `should apply events emitted while initial search request is in flight`() = runTest {

        val obj1 = StubObjectMinim(id = "1", name = "Walter Benjamin")
        val addedObject = StubObjectMinim(id = "3", name = "Aloïs Riegl")

        // No replay: an event emitted before the container attaches is lost forever,
        // exactly like the shared subscription event stream in production.
        val events = MutableSharedFlow<List<SubscriptionEvent>>(replay = 0)

        eventChannel.stub {
            on {
                subscribe(listOf(defaultSearchParams.subscription))
            } doReturn events
        }

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = defaultSpaceId,
                    subscription = defaultSearchParams.subscription,
                    sorts = defaultSearchParams.sorts,
                    filters = defaultSearchParams.filters,
                    limit = defaultSearchParams.limit,
                    offset = defaultSearchParams.offset,
                    keys = defaultSearchParams.keys,
                    afterId = null,
                    beforeId = null,
                    noDepSubscription = true,
                    ignoreWorkspace = null,
                    collection = null,
                    source = emptyList()
                )
            } doSuspendableAnswer {
                // The event arrives while the initial search request is still in flight.
                // Requires the container to have attached its collector before this request:
                // otherwise this test hangs here and the event would be dropped in production.
                events.subscriptionCount.first { count -> count > 0 }
                events.emit(
                    listOf(
                        SubscriptionEvent.Add(
                            subscription = defaultSearchParams.subscription,
                            afterId = null,
                            target = addedObject.id
                        ),
                        SubscriptionEvent.Set(
                            subscriptions = listOf(defaultSearchParams.subscription),
                            target = addedObject.id,
                            data = addedObject.map
                        )
                    )
                )
                SearchResult(
                    results = listOf(obj1),
                    dependencies = emptyList()
                )
            }
        }

        container.subscribe(defaultSearchParams).test {
            assertEquals(
                expected = listOf(obj1),
                actual = awaitItem()
            )
            assertEquals(
                expected = listOf(addedObject, obj1),
                actual = awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should not duplicate object when replayed add-event targets object from initial results`() = runTest {

        val obj1 = StubObjectMinim(id = "1", name = "Walter Benjamin")

        stubSearchWithSubscription(
            results = listOf(obj1),
            params = defaultSearchParams
        )
        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = flowOf(
                listOf(
                    SubscriptionEvent.Add(
                        subscription = defaultSearchParams.subscription,
                        afterId = null,
                        target = obj1.id
                    )
                )
            )
        )

        container.subscribe(defaultSearchParams).test {
            assertEquals(
                expected = listOf(obj1),
                actual = awaitItem()
            )
            assertEquals(
                expected = listOf(obj1),
                actual = awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `should not re-emit results when payload contains only counter events`() = runTest {

        val obj1 = StubObjectMinim(id = "1", name = "Walter Benjamin")
        val obj2 = StubObjectMinim(id = "2", name = "Aby Warburg")

        stubSearchWithSubscription(
            results = listOf(obj1, obj2),
            params = defaultSearchParams
        )
        stubSubscriptionEventChannel(
            subscription = defaultSearchParams.subscription,
            events = flowOf(
                listOf(
                    SubscriptionEvent.Counter(
                        counter = SearchResult.Counter(
                            total = 2,
                            prev = 0,
                            next = 0
                        )
                    )
                )
            )
        )

        container.subscribe(defaultSearchParams).test {
            assertEquals(
                expected = listOf(obj1, obj2),
                actual = awaitItem()
            )
            // The counter-only payload does not change the data set — no second emission.
            awaitComplete()
        }
    }

    private fun stubSearchWithSubscription(
        results: List<ObjectWrapper.Basic>,
        params: StoreSearchParams
    ) {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = defaultSpaceId,
                    subscription = params.subscription,
                    sorts = params.sorts,
                    filters = params.filters,
                    limit = params.limit,
                    offset = params.offset,
                    keys = params.keys,
                    afterId = null,
                    beforeId = null,
                    noDepSubscription = true,
                    ignoreWorkspace = null,
                    collection = null,
                    source = emptyList()
                )
            } doReturn SearchResult(
                results = results,
                dependencies = emptyList()
            )
        }
    }

    private fun stubSubscriptionEventChannel(
        subscription: Id = defaultSearchParams.subscription,
        events: Flow<List<SubscriptionEvent>> = emptyFlow()
    ) {
        eventChannel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn events
        }
    }
}

object TestLogger : Logger {
    override fun logWarning(msg: String) {
        println("Warning: $msg")
    }

    override fun logException(e: Throwable) {
        println("Error: ${e.message}")
    }

    override fun logInfo(msg: String) {
        println("Info: $msg")
    }

    override fun logException(e: Throwable, msg: String) {
        println("Error: $msg, ${e.message}")
    }
}