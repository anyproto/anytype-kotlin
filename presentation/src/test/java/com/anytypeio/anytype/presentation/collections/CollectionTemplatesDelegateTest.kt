package com.anytypeio.anytype.presentation.collections

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionTemplatesDelegateTest: ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockCollection: MockCollection

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
    fun `should start get templates when collection with default type allowed templates`() = runTest {

        val defaultType = ObjectTypeIds.PAGE
        val defaultTypeName = "Page"
        val defaultTypeMap = mapOf(
            Relations.ID to defaultType,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to defaultTypeName
        )
        mockCollection = MockCollection(context = root)

        // SETUP
        stubWorkspaceManager(mockCollection.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(defaultTypeMap)
        stubGetDefaultPageType(type = defaultType, name = defaultTypeName)

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble(),
                        Relations.TYPE to ObjectTypeIds.COLLECTION
                    )
                )
            )
        )

        stubOpenObject(
            doc = listOf(mockCollection.header, mockCollection.title, mockCollection.dataView),
            details = details
        )
        stubSubscriptionResults(
            subscription = mockCollection.subscriptionId,
            collection = root,
            workspace = mockCollection.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockCollection.dvKeys,
            objects = listOf(mockCollection.obj1, mockCollection.obj2),
            dvSorts = mockCollection.sorts
        )

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verify(getTemplates, times(1)).async(
            GetTemplates.Params(type = defaultType)
        )

        verifyNoInteractions(createDataViewObject)
    }

    @Test
    fun `should not start get templates when collection with default type not allowed templates`() = runTest {

        val defaultType = ObjectTypeIds.NOTE
        val defaultTypeName = "Note"
        val defaultTypeMap = mapOf(
            Relations.ID to defaultType,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
            Relations.NAME to defaultTypeName
        )
        mockCollection = MockCollection(context = root)

        // SETUP
        stubWorkspaceManager(mockCollection.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(defaultTypeMap)
        stubGetDefaultPageType(type = defaultType, name = defaultTypeName)

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble(),
                        Relations.TYPE to ObjectTypeIds.COLLECTION
                    )
                )
            )
        )

        stubOpenObject(
            doc = listOf(mockCollection.header, mockCollection.title, mockCollection.dataView),
            details = details
        )
        stubSubscriptionResults(
            subscription = mockCollection.subscriptionId,
            collection = root,
            workspace = mockCollection.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockCollection.dvKeys,
            objects = listOf(mockCollection.obj1, mockCollection.obj2),
            dvSorts = mockCollection.sorts
        )

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verifyNoInteractions(getTemplates)
        verifyNoInteractions(createDataViewObject)
    }
}