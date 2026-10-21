package com.anytypeio.anytype.presentation.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSearchViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `extending a query after an empty result shows loading instead of a false no-results`() =
        runTest(coroutineTestRule.dispatcher.scheduler) {
            // GIVEN — the initial (empty) query legitimately returns no objects,
            // while the follow-up query's search stays in flight.
            storeOfObjectTypes.stub {
                on { trackChanges() } doReturn flowOf(StoreOfObjectTypes.TrackedEvent.Init)
            }
            searchObjects.stub {
                onBlocking {
                    invoke(argThat { fulltext == ObjectSearchViewModel.EMPTY_QUERY })
                } doReturn Either.Right(emptyList())
                onBlocking {
                    invoke(argThat { fulltext == "quick" })
                } doSuspendableAnswer { awaitCancellation() }
            }
            val vm = ObjectSearchViewModel(
                vmParams = ObjectSearchViewModel.VmParams(space = SpaceId("test-space")),
                urlBuilder = urlBuilder,
                searchObjects = searchObjects,
                analytics = analytics,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes,
                spaceViews = spaceViews
            )
            vm.onStart(route = "test")
            val initial = awaitState(vm) { it is ObjectSearchView.NoResults }
            assertTrue(
                actual = initial is ObjectSearchView.NoResults,
                message = "Expected NoResults for the initial empty result but was $initial"
            )

            // WHEN — the user extends the query
            vm.onSearchTextChanged("quick")
            coroutineTestRule.advanceTime(ObjectSearchViewModel.DEBOUNCE_DURATION + 100)

            // THEN — the screen shows loading for the in-flight search, not a false
            // "no results for quick" derived from the previous query's empty result.
            val state = awaitState(vm) { it is ObjectSearchView.Loading }
            assertTrue(
                actual = state is ObjectSearchView.Loading,
                message = "Expected Loading while the new search is in flight but was $state"
            )
        }

    /**
     * The view-state pipeline hops through the hardcoded Dispatchers.Default
     * (flowOn in ObjectSearchViewModel), which the test scheduler cannot advance,
     * so bridging it needs a real-time poll interleaved with scheduler drains.
     */
    private fun awaitState(
        vm: ObjectSearchViewModel,
        timeoutMs: Long = 2_000,
        predicate: (ObjectSearchView?) -> Boolean
    ): ObjectSearchView? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            coroutineTestRule.advanceUntilIdle()
            val current = vm.state.value
            if (predicate(current)) return current
            Thread.sleep(10)
        }
        return vm.state.value
    }
}
