package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.collections.MockCollection
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
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
class ObjectSetDataViewObjectCreateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet
    private lateinit var mockObjectCollection: MockCollection

    private val setOfId = "setOfId"
    private val setOfKey = "setOfKey"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, setOfValue = setOfId, setOfKey = setOfKey)
        mockObjectCollection = MockCollection(context = root, space = defaultSpace)
        stubNetworkMode()
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `create pre-populated record`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
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
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )
        doReturn(Unit).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByType(
                type = TypeKey(setOfKey),
                filters = mockObjectSet.filters,
                template = null,
                prefilled = mapOf(),
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
                        prefilled = mapOf(),
                    )
                )
            }
        }
    }

    @Test
    fun `shouldn't be object create allowed when restriction is present`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details,
            dataViewRestrictions = listOf(
                DataViewRestrictions(
                    block = mockObjectSet.dataView.id,
                    restrictions = listOf(DataViewRestriction.CREATE_OBJECT)
                )
            )
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertFalse(second.isCreateObjectAllowed)
        }
    }

    @Test
    fun `shouldn't be object create allowed when type recommended layout`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val skipLayouts = SupportedLayouts.fileLayouts + SupportedLayouts.systemLayouts
        val recommendedLayout = skipLayouts.random()
        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(mockObjectSet.setOf)
                    )
                ),
                mockObjectSet.setOf to Block.Fields(
                    map = mapOf(
                        Relations.ID to mockObjectSet.setOf,
                        Relations.UNIQUE_KEY to setOfKey,
                        Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                        Relations.RECOMMENDED_LAYOUT to recommendedLayout.code.toDouble(),
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    )
                )
            )
        )
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertFalse(second.isCreateObjectAllowed)
        }
    }

    @Test
    fun `should be object create allowed when type recommended layout`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()

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
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertTrue(second.isCreateObjectAllowed)
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}