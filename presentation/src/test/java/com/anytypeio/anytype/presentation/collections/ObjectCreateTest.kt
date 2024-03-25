package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.testIn
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectCreateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        stubNetworkMode()
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `Should create and open Object with NOTE Layout when clicking on New button in Set By Type`() =
        runTest {

            val setOfId = "setOf-id-${RandomString.make()}"
            val setOfKey = "setOf-key-${RandomString.make()}"

            mockObjectSet = MockSet(
                context = root,
                setOfValue = setOfId,
                setOfKey = setOfKey
            )

            // SETUP
            stubSpaceManager(mockObjectSet.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubStoreOfObjectTypes()
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
            val newObjectId = "objNewNote-${RandomString.make()}"
            val result = CreateDataViewObject.Result(
                objectId = newObjectId,
                objectType = TypeKey(setOfKey),
                struct = mapOf(
                    Relations.ID to newObjectId,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.UNIQUE_KEY to setOfKey,
                    Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
                )
            )
            doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
                CreateDataViewObject.Params.SetByType(
                    type = TypeKey(setOfKey),
                    filters = mockObjectSet.filters,
                    template = null,
                    prefilled = emptyMap()
                )
            )
            doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

            // TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = null,
                        prefilled = emptyMap()
                    )
                )
            }

            verifyBlocking(closeBlock, times(1)) { async(mockObjectSet.root) }
        }

    @Test
    fun `Should create and open Not-Note Object when clicking on New button in Set by Type`() =
        runTest {

            val setOfId = "setOf-id-${RandomString.make()}"
            val setOfKey = ObjectTypeIds.PAGE

            mockObjectSet = MockSet(
                context = root,
                setOfValue = setOfId,
                setOfKey = setOfKey
            )

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
            val newObjectId = "objNewPage-${RandomString.make()}"
            val result = CreateDataViewObject.Result(
                objectId = newObjectId,
                objectType = TypeKey(ObjectTypeIds.PAGE)
            )
            doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
                CreateDataViewObject.Params.SetByType(
                    type = TypeKey(setOfKey),
                    filters = mockObjectSet.filters,
                    template = null,
                    prefilled = emptyMap()
                )
            )
            doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

            // TESTING
            proceedWithStartingViewModel()
            val commandFlow = viewModel.commands.testIn(backgroundScope)

            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = null,
                        prefilled = emptyMap()
                    )
                )
            }
        }

    @Test
    fun `Should create new Object and not close Set when clicking on New button in Set by Relations`() =
        runTest {

            val setByRelationValue = "setByRelation-${RandomString.make()}"

            val relationKey = "relationKey-${RandomString.make()}"
            val relationUniqueKeys = "relationUniqueKeys-${RandomString.make()}"
            mockObjectSet = MockSet(context = root, setOfValue = setByRelationValue)
            val setByRelationMap = mapOf(
                Relations.ID to setByRelationValue,
                Relations.LAYOUT to ObjectType.Layout.RELATION.code.toDouble(),
                Relations.RELATION_KEY to relationKey,
                Relations.UNIQUE_KEY to relationUniqueKeys,
            )
            val relationSetBy = ObjectWrapper.Relation(map = setByRelationMap)
            val pageTypeId = ObjectState.VIEW_DEFAULT_OBJECT_TYPE
            val pageTypeMap = mapOf(
                Relations.ID to MockDataFactory.randomString(),
                Relations.UNIQUE_KEY to pageTypeId,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to null
            )
            with(storeOfObjectTypes) {
                set(pageTypeId, pageTypeMap)
            }

            // SETUP
            stubSpaceManager(mockObjectSet.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
                details = mockObjectSet.detailsSetByRelation(relationSetBy)
            )
            storeOfRelations.merge(listOf(relationSetBy))

            stubSubscriptionResults(
                subscription = mockObjectSet.subscriptionId,
                spaceId = mockObjectSet.spaceId,
                storeOfRelations = storeOfRelations,
                keys = mockObjectSet.dvKeys,
                sources = listOf(setByRelationValue),
                dvFilters = mockObjectSet.filters,
                objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
            )
            val newObjectId = "objNew-${RandomString.make()}"
            val result = CreateDataViewObject.Result(
                objectId = newObjectId,
                objectType = TypeKey(pageTypeId)
            )
            doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
                CreateDataViewObject.Params.SetByRelation(
                    filters = mockObjectSet.filters,
                    template = null,
                    type = TypeKey(pageTypeId),
                    prefilled = emptyMap()
                )
            )
            doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

            // TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByRelation(
                        filters = mockObjectSet.filters,
                        template = null,
                        type = TypeKey(pageTypeId),
                        prefilled = mapOf(relationKey to null)
                    )
                )
            }

            verifyNoInteractions(closeBlock)
        }

    @Test
    fun `Should create new Object and not close Set when clicking on New button in Collection`() = runTest {

        val objectCollection = MockCollection(context = root)

        // SETUP
        stubSpaceManager(objectCollection.spaceId)
        stubStoreOfRelations(objectCollection)
        stubSubscriptionResults(
            subscription = objectCollection.subscriptionId,
            collection = root,
            spaceId = objectCollection.spaceId,
            storeOfRelations = storeOfRelations,
            keys = objectCollection.dvKeys,
            objects = listOf(objectCollection.obj1, objectCollection.obj2),
            dvSorts = objectCollection.sorts
        )
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                objectCollection.header,
                objectCollection.title,
                objectCollection.dataView
            ),
            details = objectCollection.details
        )

        val pageTypeId = ObjectState.VIEW_DEFAULT_OBJECT_TYPE
        val pageTypeMap = mapOf(
            Relations.ID to MockDataFactory.randomString(),
            Relations.UNIQUE_KEY to pageTypeId,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
            Relations.NAME to MockDataFactory.randomString(),
            Relations.DEFAULT_TEMPLATE_ID to null
        )
        with(storeOfObjectTypes) {
            set(pageTypeId, pageTypeMap)
        }

        val newObjectId = "objNew-${RandomString.make()}"
        val result = CreateDataViewObject.Result(
            objectId = newObjectId,
            objectType = TypeKey(pageTypeId),
            struct = null
        )
        doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.Collection(
                template = null,
                type = TypeKey(pageTypeId),
                filters = emptyList(),
                prefilled = emptyMap()
            )
        )
        doReturn(Resultat.success(Unit)).`when`(closeBlock).async(objectCollection.root)

        // TESTING
        proceedWithStartingViewModel()

        advanceUntilIdle()

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(
                CreateDataViewObject.Params.Collection(
                    type = TypeKey(pageTypeId),
                    template = null,
                    filters = emptyList(),
                    prefilled = emptyMap()
                )
            )
        }

        verifyNoInteractions(closeBlock)
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}