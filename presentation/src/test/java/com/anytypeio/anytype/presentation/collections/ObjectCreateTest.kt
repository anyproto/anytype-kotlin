package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.testIn
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import kotlin.test.assertIs
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
        stubGetDefaultPageType()
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `Should create and open Note Object when clicking on New button in Set By Type`() = runTest {

        mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.NOTE)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes()
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
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )
        val newObjectId = "objNewNote-${RandomString.make()}"
        val result = CreateDataViewObject.Result(
            objectId = newObjectId,
            objectType = ObjectTypeIds.NOTE
        )
        doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByType(
                type = ObjectTypeIds.NOTE,
                filters = mockObjectSet.filters,
                template = null
            )
        )
        doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onCreateNewDataViewObject()

        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(
                CreateDataViewObject.Params.SetByType(
                    type = ObjectTypeIds.NOTE,
                    filters = mockObjectSet.filters,
                    template = null
                )
            )
        }

        verifyBlocking(closeBlock, times(1)) { async(mockObjectSet.root)}
    }

    @Test
    fun `Should create and Set Name for Not-Note Object when clicking on New button in Set by Type`() = runTest {

        mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.PAGE)

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
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )
        val newObjectId = "objNewPage-${RandomString.make()}"
        val result = CreateDataViewObject.Result(
            objectId = newObjectId,
            objectType = ObjectTypeIds.PAGE
        )
        doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByType(
                type = ObjectTypeIds.PAGE,
                filters = mockObjectSet.filters,
                template = null
            )
        )
        doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

        // TESTING
        viewModel.onStart(ctx = root)
        val commandFlow = viewModel.commands.testIn(backgroundScope)

        advanceUntilIdle()

        viewModel.onCreateNewDataViewObject()

        assertIs<ObjectSetCommand.Modal.SetNameForCreatedObject>(commandFlow.awaitItem())

        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(
                CreateDataViewObject.Params.SetByType(
                    type = ObjectTypeIds.PAGE,
                    filters = mockObjectSet.filters,
                    template = null
                )
            )
        }

        verifyNoInteractions(closeBlock)
    }

    @Test
    fun `Should create and open Object when clicking on New button in Set by Relations`() = runTest {

        mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.NOTE)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.detailsSetByRelation
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )
        val newObjectId = "objNew-${RandomString.make()}"
        val result = CreateDataViewObject.Result(
            objectId = newObjectId,
            objectType = ObjectTypeIds.NOTE
        )
        doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.SetByRelation(
                relations = listOf(mockObjectSet.relationObject3.id),
                filters = mockObjectSet.filters,
                template = null
            )
        )
        doReturn(Resultat.success(Unit)).`when`(closeBlock).async(mockObjectSet.root)

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onCreateNewDataViewObject()

        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(
                CreateDataViewObject.Params.SetByRelation(
                    relations = listOf(mockObjectSet.relationObject3.id),
                    filters = mockObjectSet.filters,
                    template = null
                )
            )
        }

        verifyBlocking(closeBlock, times(1)) { async(mockObjectSet.root)}
    }

    @Test
    fun `Should create and open Object when clicking on New button in Collection`() = runTest {

        val objectCollection = MockCollection(context = root)

        // SETUP
        stubWorkspaceManager(objectCollection.workspaceId)
        stubStoreOfRelations(objectCollection)
        stubSubscriptionResults(
            subscription = objectCollection.subscriptionId,
            collection = root,
            workspace = objectCollection.workspaceId,
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

        val newObjectId = "objNew-${RandomString.make()}"
        val result = CreateDataViewObject.Result(
            objectId = newObjectId,
            objectType = ObjectTypeIds.NOTE
        )
        doReturn(Resultat.success(result)).`when`(createDataViewObject).async(
            CreateDataViewObject.Params.Collection(
                templateId = null
            )
        )
        doReturn(Resultat.success(Unit)).`when`(closeBlock).async(objectCollection.root)

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onCreateNewDataViewObject()

        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(CreateDataViewObject.Params.Collection(null))
        }

        verifyBlocking(closeBlock, times(1)) { async(objectCollection.root)}
    }
}