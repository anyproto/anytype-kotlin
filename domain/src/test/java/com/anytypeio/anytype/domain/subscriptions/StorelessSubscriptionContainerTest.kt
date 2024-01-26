package com.anytypeio.anytype.domain.subscriptions

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectMinim
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import org.mockito.kotlin.stub

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

    private val defaultSearchParams = StoreSearchParams(
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

    private fun stubSearchWithSubscription(
        results: List<ObjectWrapper.Basic>,
        params: StoreSearchParams
    ) {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
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
}