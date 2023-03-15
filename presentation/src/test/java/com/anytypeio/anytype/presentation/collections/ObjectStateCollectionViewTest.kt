package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.testIn
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

        val headerFlow = viewModel.header.testIn(backgroundScope)
        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        assertNull(headerFlow.awaitItem())
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())

        assertEquals(
            expected = mockObjectCollection.title.content.asText().text,
            actual = headerFlow.awaitItem()?.text
        )
        viewerFlow.expectNoEvents()

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

        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Collection.NoView>(viewerFlow.awaitItem())

        // ASSERT NO SUBSCRIPTION TO COLLECTION RECORDS
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

        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Collection.NoItems>(viewerFlow.awaitItem())
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

        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Collection.NoItems>(viewerFlow.awaitItem())
        assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem())
    }
}