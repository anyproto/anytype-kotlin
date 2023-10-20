package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.testIn
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationObject
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

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectCreateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `Should create and open Note Object when clicking on New button in Set By Type`() =
        runTest {

            mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.NOTE)

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
                objectType = TypeKey(ObjectTypeIds.NOTE)
            )
            doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
                CreateDataViewObject.Params.SetByType(
                    type = TypeKey(ObjectTypeIds.NOTE),
                    filters = mockObjectSet.filters,
                    template = null
                )
            )
            doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

            // TESTING
            viewModel.onStart(ctx = root)

            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(ObjectTypeIds.NOTE),
                        filters = mockObjectSet.filters,
                        template = null
                    )
                )
            }

            verifyBlocking(closeBlock, times(1)) { async(mockObjectSet.root) }
        }

    @Test
    fun `Should create and open Not-Note Object when clicking on New button in Set by Type`() =
        runTest {

            mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.PAGE)

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
                    type = TypeKey(ObjectTypeIds.PAGE),
                    filters = mockObjectSet.filters,
                    template = null
                )
            )
            doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

            // TESTING
            viewModel.onStart(ctx = root)
            val commandFlow = viewModel.commands.testIn(backgroundScope)

            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(ObjectTypeIds.PAGE),
                        filters = mockObjectSet.filters,
                        template = null
                    )
                )
            }
        }

    @Test
    fun `Should create and open Object when clicking on New button in Set by Relations`() =
        runTest {

            val setByRelationValue = "setByRelation-${RandomString.make()}"
            val relationKey = "relationKey-${RandomString.make()}"
            val relationUniqueKeys = "relationUniqueKeys-${RandomString.make()}"
            mockObjectSet = MockSet(context = root, setOfValue = setByRelationValue)
            val relationSetBy = StubRelationObject(
                id = setByRelationValue,
                key = relationKey,
                uniqueKey = relationUniqueKeys,
                isReadOnlyValue = false,
                format = Relation.Format.LONG_TEXT,
                spaceId = mockObjectSet.spaceId
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
                    relations = listOf(setByRelationValue),
                    filters = mockObjectSet.filters,
                    template = null,
                    type = TypeKey(pageTypeId)
                )
            )
            doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

            // TESTING
            viewModel.onStart(ctx = root)

            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByRelation(
                        relations = listOf(relationSetBy.id),
                        filters = mockObjectSet.filters,
                        template = null,
                        type = TypeKey(pageTypeId)
                    )
                )
            }

            verifyBlocking(closeBlock, times(1)) { async(mockObjectSet.root) }
        }

    @Test
    fun `Should create and open Object when clicking on New button in Collection`() = runTest {

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
                templateId = null,
                type = TypeKey(pageTypeId)
            )
        )
        doReturn(Resultat.success(Unit)).`when`(closeBlock).async(objectCollection.root)

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(
                CreateDataViewObject.Params.Collection(
                    type = TypeKey(pageTypeId),
                    templateId = null
                )
            )
        }

        verifyBlocking(closeBlock, times(1)) { async(objectCollection.root) }
    }
}