package com.anytypeio.anytype.domain.subscriptions

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubObject
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

        val obj1 = StubObject()
        val obj2 = StubObject()
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