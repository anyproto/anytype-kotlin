package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.test
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectStateCollectionViewTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectCollection: MockCollection

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectCollection = MockCollection(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should display init state when opening object without DataView block`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectCollection.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectCollection.header, mockObjectCollection.title),
            details = mockObjectCollection.details
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT COLLECTION OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Collection>(second)

        }

        // ASSERT HEADER STATE
        viewModel.header.test {
            val first = awaitItem()
            assertNull(first)

            val second = awaitItem()
            assertEquals(
                expected = mockObjectCollection.title.content.asText().text,
                actual = second?.text
            )
            expectNoEvents()
        }

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)
            expectNoEvents()
        }

        // ASSERT NO SUBSCRIPTION TO COLLECTION RECORDS
        advanceUntilIdle()
        verifyNoInteractions(repo)
    }

    @Test
    fun `should display collection no view state when opening object without view`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectCollection.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                mockObjectCollection.header,
                mockObjectCollection.title,
                mockObjectCollection.dataViewEmpty
            ),
            details = mockObjectCollection.details
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT COLLECTION OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Collection>(second)
            expectNoEvents()
        }

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Collection.NoView>(second)
            expectNoEvents()
        }

        // ASSERT SUBSCRIPTION TO COLLECTION RECORDS
        advanceUntilIdle()
        verifyNoInteractions(repo)
    }


    @Test
    fun `should display collection no items state when opening object without records`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectCollection.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                mockObjectCollection.header,
                mockObjectCollection.title,
                mockObjectCollection.dataView
            ),
            details = mockObjectCollection.details
        )
        stubStoreOfRelations(mockObjectCollection)
        stubSubscriptionResults(
            subscription = mockObjectCollection.subscriptionId,
            workspace = mockObjectCollection.workspaceId,
            collection = root,
            storeOfRelations = storeOfRelations,
            keys = mockObjectCollection.dvKeys
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT COLLECTION OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Collection>(second)
            expectNoEvents()
        }

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Collection.NoItems>(second)
            expectNoEvents()
        }
    }

    @Test
    fun `should display collection default state when opening object with records`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectCollection.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                mockObjectCollection.header,
                mockObjectCollection.title,
                mockObjectCollection.dataView
            ),
            details = mockObjectCollection.details
        )
        stubStoreOfRelations(mockObjectCollection)
        stubSubscriptionResults(
            subscription = mockObjectCollection.subscriptionId,
            workspace = mockObjectCollection.workspaceId,
            collection = root,
            storeOfRelations = storeOfRelations,
            keys = mockObjectCollection.dvKeys,
            objects = listOf(mockObjectCollection.obj1, mockObjectCollection.obj2),
            dvSorts = mockObjectCollection.sorts
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT COLLECTION OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Collection>(second)
            expectNoEvents()
        }

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Collection.NoItems>(second)

            val third = awaitItem()
            assertIs<DataViewViewState.Collection.Default>(third)
            expectNoEvents()
        }
    }
}