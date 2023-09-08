package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class SetByRelationTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `should create new object with source object type if given set is aggregated by specific object type`() = runTest{
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
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
        doReturn(Unit).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByType(
                type = mockObjectSet.setOf,
                filters = mockObjectSet.filters,
                template = null
            )
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()
            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = mockObjectSet.setOf,
                        filters = mockObjectSet.filters,
                        template = null
                    )
                )
            }
        }
    }

    @Test
    fun `should create new object with default object type if given set is aggregated by relations`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.detailsSetByRelation
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.relationObject3.id),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )
        doReturn(Unit).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByRelation(
                relations = listOf(mockObjectSet.relationObject3.id),
                filters = mockObjectSet.filters,
                template = null,
                type = ObjectTypeIds.PAGE
            )
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()
            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByRelation(
                        relations = listOf(mockObjectSet.relationObject3.id),
                        filters = mockObjectSet.filters,
                        template = null,
                        type = ObjectTypeIds.PAGE
                    )
                )
            }
        }
    }
}