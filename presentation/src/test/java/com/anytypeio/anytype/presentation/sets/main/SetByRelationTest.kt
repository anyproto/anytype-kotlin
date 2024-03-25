package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.primitives.TypeKey
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

    private val setOfId = "setOfId"
    private val setOfKey = "setOfKey"

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, setOfValue = setOfId, setOfKey = setOfKey, space = defaultSpace)
        stubNetworkMode()
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `should create new object with source object type if given set is aggregated by specific object type`() = runTest{
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )
        doReturn(Unit).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByType(
                type = TypeKey(setOfKey),
                filters = mockObjectSet.filters,
                template = null,
                prefilled = emptyMap()
            )
        )

        // TESTING
        proceedWithStartingViewModel()

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
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = null,
                        prefilled = mapOf(
                            mockObjectSet.filters[0].relation to mockObjectSet.filters[0].value,
                            mockObjectSet.filters[1].relation to mockObjectSet.filters[1].value
                        )
                    )
                )
            }
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}