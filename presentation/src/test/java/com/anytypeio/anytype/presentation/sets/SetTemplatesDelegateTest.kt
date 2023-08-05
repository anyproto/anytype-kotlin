package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.test_utils.MockDataFactory
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
class SetTemplatesDelegateTest : ObjectSetViewModelTestSetup() {

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
    fun `should start get templates when set by pages`() = runTest {

        val setOf = ObjectTypeIds.PAGE
        val setOfName = "Pages"
        val setOfMap = mapOf(
            Relations.ID to setOf,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to setOfName
        )

        mockObjectSet = MockSet(context = root, setOfValue = setOf)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(map = setOfMap)
        stubGetTemplates()

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(setOf)
                    )
                ),
                setOf to Block.Fields(map = setOfMap)
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
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

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verify(getTemplates, times(1)).async(
            GetTemplates.Params(type = setOf)
        )

        verifyNoInteractions(createDataViewObject)
    }

    @Test
    fun `should not start get templates when set by notes`() = runTest {

        val setOf = ObjectTypeIds.NOTE
        val setOfName = "Notes"
        val setOfMap = mapOf(
            Relations.ID to setOf,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
            Relations.NAME to setOfName
        )
        mockObjectSet = MockSet(context = root, setOfValue = setOf)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(setOfMap)
        stubTemplatesDelegate()

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(setOf)
                    )
                ),
                setOf to Block.Fields(map = setOfMap)
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
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

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verifyNoInteractions(getTemplates)
        verifyNoInteractions(createDataViewObject)
    }

    @Test
    fun `should start get templates when set by custom type with recommended layout`() = runTest {

        val setOf = MockDataFactory.randomString()
        val setOfName = MockDataFactory.randomString()
        val setOfMap = mapOf(
            Relations.ID to setOf,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.PROFILE.code.toDouble(),
            Relations.NAME to setOfName
        )
        mockObjectSet = MockSet(context = root, setOfValue = setOf)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(setOfMap)
        stubTemplatesDelegate()

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(setOf)
                    )
                ),
                setOf to Block.Fields(map = setOfMap)
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
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

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verify(getTemplates, times(1)).async(
            GetTemplates.Params(type = setOf,)
        )
        verifyNoInteractions(createDataViewObject)
    }

    @Test
    fun `should not start get templates event OnStart when set by custom type with not recommended layout`() = runTest {

        val setOf = MockDataFactory.randomString()
        val setOfName = MockDataFactory.randomString()
        val setOfMap = mapOf(
            Relations.ID to setOf,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.FILE.code.toDouble(),
            Relations.NAME to setOfName
        )
        mockObjectSet = MockSet(context = root, setOfValue = setOf)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(setOfMap)
        stubTemplatesDelegate()

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(setOf)
                    )
                ),
                setOf to Block.Fields(map = setOfMap)
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
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

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verifyNoInteractions(getTemplates)
        verifyNoInteractions(createDataViewObject)
    }

    @Test
    fun `should not start get templates event OnStart when set by set`() = runTest {

        val setOf = ObjectTypeIds.SET
        val setOfName = MockDataFactory.randomString()
        val setOfMap = mapOf(
            Relations.ID to setOf,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.SET.code.toDouble(),
            Relations.NAME to setOfName
        )
        mockObjectSet = MockSet(context = root, setOfValue = setOf)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(setOfMap)
        stubTemplatesDelegate()

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(setOf)
                    )
                ),
                setOf to Block.Fields(map = setOfMap)
            )
        )

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
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

        // TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        viewModel.onNewButtonIconClicked()

        advanceUntilIdle()

        verifyNoInteractions(getTemplates)
        verifyNoInteractions(createDataViewObject)
    }

    @Test
    fun `should start get templates when set by relation and default type is valid`() = runTest {

        val relation = MockDataFactory.randomString()
        val defaultType = ObjectTypeIds.PAGE
        val defaultTypeName = "Page"
        val defaultTypeMap = mapOf(
            Relations.ID to defaultType,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to defaultTypeName
        )
        mockObjectSet = MockSet(context = root, setOfValue = relation)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(defaultTypeMap)
        stubTemplatesDelegate()
        stubGetDefaultPageType(type = defaultType, name = defaultTypeName)

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(relation)
                    )
                ),
                relation to Block.Fields(
                    map = mapOf(
                        Relations.ID to relation,
                        Relations.TYPE to ObjectTypeIds.RELATION
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
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
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
    fun `should start get templates when set by relation and default type templates are not allowed`() = runTest {

        val relation = MockDataFactory.randomString()
        val defaultType = ObjectTypeIds.NOTE
        val defaultTypeName = "Note"
        val defaultTypeMap = mapOf(
            Relations.ID to defaultType,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
            Relations.NAME to defaultTypeName
        )
        mockObjectSet = MockSet(context = root, setOfValue = relation)

        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfObjectTypes(defaultTypeMap)
        stubTemplatesDelegate()
        stubGetDefaultPageType(type = defaultType, name = defaultTypeName)

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                        Relations.SET_OF to listOf(relation)
                    )
                ),
                relation to Block.Fields(
                    map = mapOf(
                        Relations.ID to relation,
                        Relations.TYPE to ObjectTypeIds.RELATION
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
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
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