package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectStateSetViewTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `displaying error state when object with layout other than set or collection`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val objectDetails = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                    )
                )
            )
        )
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title),
            details = objectDetails
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT COLLECTION OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)
            expectNoEvents()
        }

        // ASSERT SUBSCRIPTION TO SET RECORDS
        verifyNoInteractions(repo)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Error>(second)

            assertEquals(expected = "Wrong layout, couldn't open object", actual = second.msg)
            expectNoEvents()
        }
    }

    @Test
    fun `displaying set init state when object with SET layout and no DataView`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title),
            details = mockObjectSet.details
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT SET OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)
            expectNoEvents()
        }

        // ASSERT SUBSCRIPTION TO SET RECORDS
        verifyNoInteractions(repo)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)
            expectNoEvents()
        }
    }

    @Test
    fun `displaying set no query state when object with DataView and empty setOf`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.detailsEmptySetOf
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT SET OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Set>(second)
        }

        // ASSERT SUBSCRIPTION TO SET RECORDS
        verifyNoInteractions(repo)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.NoQuery>(second)
        }
    }

    @Test
    fun `displaying set no view state when object with DataView and nullable view`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataViewNoViews),
            details = mockObjectSet.details
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT SET OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Set>(second)
        }

        // ASSERT SUBSCRIPTION TO SET RECORDS
        verifyNoInteractions(repo)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.NoView>(second)

            expectNoEvents()
        }
    }

    @Test
    fun `displaying set with items state when object set with two records`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT SET OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Set>(second)
        }

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.NoItems>(second)

            val third = awaitItem()
            assertIs<DataViewViewState.Set.Default>(third)
        }
    }

    @Test
    fun `displaying set with no items when opening object set with no records`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf)
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT SET OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Set>(second)
        }

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.NoItems>(second)
        }
    }
}