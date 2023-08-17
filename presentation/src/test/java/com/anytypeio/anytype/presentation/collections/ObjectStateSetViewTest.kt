package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.test
import app.cash.turbine.testIn
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
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

        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Set.NoQuery>(viewerFlow.awaitItem())

        // ASSERT SUBSCRIPTION TO SET RECORDS
        advanceUntilIdle()
        verifyNoInteractions(repo)
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

        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Set.NoView>(viewerFlow.awaitItem())
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

        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Set.Default>(viewerFlow.awaitItem())
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
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT STATES
        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())
        assertIs<DataViewViewState.Set.NoItems>(viewerFlow.awaitItem())
    }

    @Test
    fun `Displaying Object Sets with Non-Deleted Types Only`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val typeDeleted1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to RandomString.make(),
                Relations.IS_DELETED to true
            )
        )
        val type2 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to RandomString.make(),
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE
            )
        )
        val typeDeleted3 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to RandomString.make(),
            )
        )

        val detailsDeletedSetOf = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(typeDeleted1.id, type2.id, typeDeleted3.id)
                    )
                ),
                typeDeleted1.id to Block.Fields(
                    mapOf(
                        Relations.ID to typeDeleted1.id,
                        Relations.IS_DELETED to true
                    )
                ),
                type2.id to Block.Fields(
                    mapOf(
                        Relations.ID to type2.id,
                        Relations.TYPE to ObjectTypeIds.OBJECT_TYPE
                    )
                )
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = detailsDeletedSetOf
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(type2.id),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT STATES
        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Set.Default>(viewerFlow.awaitItem())

        stateFlow.ensureAllEventsConsumed()
        viewerFlow.ensureAllEventsConsumed()

        advanceUntilIdle()
        verifyBlocking(repo, times(1)) {
            searchObjectsWithSubscription(
                eq(mockObjectSet.subscriptionId),
                eq(listOf()),
                eq(
                    mockObjectSet.filters + ObjectSearchConstants.defaultDataViewFilters(
                        mockObjectSet.workspaceId
                    )
                ),
                eq(ObjectSearchConstants.defaultDataViewKeys + mockObjectSet.dvKeys),
                eq(listOf(type2.id)),
                eq(0L),
                eq(ObjectSetConfig.DEFAULT_LIMIT),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null)
            )
        }
    }

    @Test
    fun `Displaying Object Set with No Query State When All Types are Deleted`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val typeDeleted1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to RandomString.make(),
                Relations.IS_DELETED to true
            )
        )
        val typeDeleted3 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to RandomString.make(),
            )
        )

        val detailsDeletedSetOf = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(typeDeleted1.id, typeDeleted3.id)
                    )
                ),
                typeDeleted1.id to Block.Fields(
                    mapOf(
                        Relations.ID to typeDeleted1.id,
                        Relations.IS_DELETED to true
                    )
                )
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = detailsDeletedSetOf
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT STATES
        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Set.NoQuery>(viewerFlow.awaitItem())

        stateFlow.ensureAllEventsConsumed()
        viewerFlow.ensureAllEventsConsumed()
    }

    @Test
    fun `displaying set with templates present when opening object set of pages with templates`() = runTest {
        // SETUP

        mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.PAGE)
        val pageTypeMap = mapOf(
            Relations.ID to ObjectTypeIds.PAGE,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to MockDataFactory.randomString()
        )
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
            sources = listOf(ObjectTypeIds.PAGE),
            dvFilters = mockObjectSet.filters
        )
        stubStoreOfObjectTypes(pageTypeMap)
        stubTemplatesContainer(
            type = ObjectTypeIds.PAGE,
            templates = listOf(StubObject(objectType = ObjectTypeIds.PAGE))
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT STATES
        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        val item = viewerFlow.awaitItem()
        assertIs<DataViewViewState.Set.NoItems>(item)
        assertTrue(item.hasTemplates)
    }

    @Test
    fun `displaying set without templates allowed when opening object set of notes`() = runTest {
        // SETUP

        mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.NOTE)
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
            sources = listOf(ObjectTypeIds.NOTE),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT STATES
        val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // ASSERT STATES
        assertIs<ObjectState.Init>(stateFlow.awaitItem())
        assertIs<ObjectState.DataView.Set>(stateFlow.awaitItem())
        assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

        val item = viewerFlow.awaitItem()
        assertIs<DataViewViewState.Set.NoItems>(item)
        assertFalse(item.hasTemplates)
    }
}