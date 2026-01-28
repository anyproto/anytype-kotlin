package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.DataView
import com.anytypeio.anytype.core_models.Block.Content.DataView.Viewer
import com.anytypeio.anytype.core_models.Block.Content.DataView.Viewer.ViewerRelation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.ViewerRelationListView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class ObjectSetSettingsViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private lateinit var objectState: MutableStateFlow<ObjectState>
    private lateinit var dispatcher: Dispatcher<com.anytypeio.anytype.core_models.Payload>
    private lateinit var updateDataViewViewer: UpdateDataViewViewer
    private lateinit var storeOfRelations: StoreOfRelations
    private lateinit var analytics: com.anytypeio.anytype.analytics.base.Analytics
    private lateinit var deleteRelationFromDataView: DeleteRelationFromDataView
    private lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    private lateinit var spaceManager: SpaceManager
    private lateinit var viewModel: ObjectSetSettingsViewModel

    @Before
    fun setup() {
        dispatcher = mock()
        updateDataViewViewer = mock()
        storeOfRelations = DefaultStoreOfRelations()
        analytics = mock()
        deleteRelationFromDataView = mock()
        analyticSpaceHelperDelegate = mock()
        spaceManager = mock()
    }

    @Test
    fun `in case of Gallery property Name should be in the list`() = runTest {
        // Arrange


        val relationKey = Relations.NAME
        val viewerId = "viewer-id"
        val blockId = "block-id"
        val relationName = "Name"
        val relationFormat = RelationFormat.LONG_TEXT
        val relationObjects = listOf(
            StubRelationObject(
                key = relationKey,
                name = relationName,
                format = relationFormat,
                isHidden = true
            )
        )

        storeOfRelations.merge(relationObjects)
        advanceUntilIdle()

        val viewerRelation = ViewerRelation(
            key = relationKey,
            isVisible = true
        )

        val viewer = Viewer(
            id = viewerId,
            name = "Test Viewer",
            type = Viewer.Type.GALLERY,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(viewerRelation)
        )
        val relationLink = RelationLink(key = relationKey, format = relationFormat)

        val dataView = DataView(
            viewers = listOf(viewer),
            relationLinks = listOf(relationLink)
        )
        val block = Block(
            id = blockId,
            children = emptyList(),
            content = dataView,
            fields = Block.Fields.empty()
        )
        val state = ObjectState.DataView.Set(
            root = "root-id",
            blocks = listOf(block)
        )
        objectState = MutableStateFlow(state)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Act
        viewModel.onStart(viewerId)
        coroutineTestRule.advanceUntilIdle()

        // Assert
        val expected = listOf(
            ViewerRelationListView.Relation(
                SimpleRelationView(
                    key = relationKey,
                    title = relationName,
                    format = RelationFormat.LONG_TEXT,
                    isVisible = true,
                    isHidden = true,
                    isReadonly = false,
                    isDefault = Relations.systemRelationKeys.contains(relationKey),
                    canToggleVisibility = false
                )
            )
        )
        assertEquals(expected, viewModel.views.value)
    }

    @Test
    fun `in case of Grid hidden Name should be in the list with toggle hidden`() = runTest {
        val relationKey = Relations.NAME
        val viewerId = "viewer-id"
        val blockId = "block-id"
        val relationName = "Name"
        val relationFormat = RelationFormat.LONG_TEXT
        val relationObjects = listOf(
            StubRelationObject(
                key = relationKey,
                name = relationName,
                format = relationFormat,
                isHidden = true
            )
        )
        storeOfRelations.merge(relationObjects)
        advanceUntilIdle()

        val viewerRelation = ViewerRelation(
            key = relationKey,
            isVisible = true
        )
        val viewer = Viewer(
            id = viewerId,
            name = "Test Viewer",
            type = Viewer.Type.GRID,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(viewerRelation)
        )
        val relationLink = RelationLink(key = relationKey, format = relationFormat)
        val dataView = DataView(
            viewers = listOf(viewer),
            relationLinks = listOf(relationLink)
        )
        val block = Block(
            id = blockId,
            children = emptyList(),
            content = dataView,
            fields = Block.Fields.empty()
        )
        val state = ObjectState.DataView.Set(
            root = "root-id",
            blocks = listOf(block)
        )
        objectState = MutableStateFlow(state)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Act
        viewModel.onStart(viewerId)
        coroutineTestRule.advanceUntilIdle()

        // Assert - Name should be in the list but with toggle hidden (canToggleVisibility = false)
        val expected = listOf(
            ViewerRelationListView.Relation(
                SimpleRelationView(
                    key = relationKey,
                    title = relationName,
                    format = RelationFormat.LONG_TEXT,
                    isVisible = true,
                    isHidden = true,
                    isReadonly = false,
                    isDefault = Relations.systemRelationKeys.contains(relationKey),
                    canToggleVisibility = false
                )
            )
        )
        assertEquals(expected, viewModel.views.value)
    }

    @Test
    fun `non-hidden custom relation should be in the list`() = runTest {
        val relationKey = "custom-relation"
        val viewerId = "viewer-id"
        val blockId = "block-id"
        val relationName = "Custom"
        val relationFormat = RelationFormat.NUMBER
        val relationObjects = listOf(
            StubRelationObject(
                key = relationKey,
                name = relationName,
                format = relationFormat,
                isHidden = false
            )
        )
        storeOfRelations.merge(relationObjects)
        advanceUntilIdle()

        val viewerRelation = ViewerRelation(
            key = relationKey,
            isVisible = true
        )
        val viewer = Viewer(
            id = viewerId,
            name = "Test Viewer",
            type = Viewer.Type.GRID,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(viewerRelation)
        )
        val relationLink = RelationLink(key = relationKey, format = relationFormat)
        val dataView = DataView(
            viewers = listOf(viewer),
            relationLinks = listOf(relationLink)
        )
        val block = Block(
            id = blockId,
            children = emptyList(),
            content = dataView,
            fields = Block.Fields.empty()
        )
        val state = ObjectState.DataView.Set(
            root = "root-id",
            blocks = listOf(block)
        )
        objectState = MutableStateFlow(state)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Act
        viewModel.onStart(viewerId)
        coroutineTestRule.advanceUntilIdle()

        // Assert
        val expected = listOf(
            ViewerRelationListView.Relation(
                SimpleRelationView(
                    key = relationKey,
                    title = relationName,
                    format = RelationFormat.NUMBER,
                    isVisible = true,
                    isHidden = false,
                    isReadonly = false,
                    isDefault = Relations.systemRelationKeys.contains(relationKey)
                )
            )
        )
        assertEquals(expected, viewModel.views.value)
    }

    @Test
    fun `no viewer found should result in empty views`() = runTest {
        val viewerId = "non-existent-viewer"
        val blockId = "block-id"
        val dataView = DataView(
            viewers = emptyList(),
            relationLinks = emptyList()
        )
        val block = Block(
            id = blockId,
            children = emptyList(),
            content = dataView,
            fields = Block.Fields.empty()
        )
        val state = ObjectState.DataView.Set(
            root = "root-id",
            blocks = listOf(block)
        )
        objectState = MutableStateFlow(state)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Act
        viewModel.onStart(viewerId)
        coroutineTestRule.advanceUntilIdle()

        // Assert
        assertEquals(emptyList(), viewModel.views.value)
    }

    @Test
    fun `non DataView state should result in empty views`() = runTest {
        objectState = MutableStateFlow(ObjectState.Init)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Act
        viewModel.onStart("any-viewer-id")
        coroutineTestRule.advanceUntilIdle()

        // Assert
        assertEquals(emptyList(), viewModel.views.value)
    }

    //region buildCompleteOrderPreservingHidden tests

    @Test
    fun `should return visible order when no hidden properties exist`() = runTest {
        // Arrange
        objectState = MutableStateFlow(ObjectState.Init)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        val completeRelations = listOf(
            createSimpleRelationView(key = "name", isHidden = false),
            createSimpleRelationView(key = "status", isHidden = false),
            createSimpleRelationView(key = "tags", isHidden = false)
        )
        val newVisibleOrder = listOf("tags", "name", "status") // User reordered

        // Act
        val result = viewModel.buildCompleteOrderPreservingHidden(
            completeRelations = completeRelations,
            newVisibleOrder = newVisibleOrder
        )

        // Assert - should match new visible order exactly
        assertEquals(listOf("tags", "name", "status"), result)
    }

    @Test
    fun `should preserve hidden property at original position when visible properties are reordered`() =
        runTest {
            // Arrange
            objectState = MutableStateFlow(ObjectState.Init)
            viewModel = ObjectSetSettingsViewModel(
                objectState = objectState,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                storeOfRelations = storeOfRelations,
                analytics = analytics,
                deleteRelationFromDataView = deleteRelationFromDataView,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                spaceManager = spaceManager
            )

            // Original order: name, hiddenProp, status, tags
            val completeRelations = listOf(
                createSimpleRelationView(key = "name", isHidden = false),
                createSimpleRelationView(key = "hiddenProp", isHidden = true),
                createSimpleRelationView(key = "status", isHidden = false),
                createSimpleRelationView(key = "tags", isHidden = false)
            )
            // User sees: name, status, tags (hiddenProp is filtered out)
            // User drags tags to second position: name, tags, status
            val newVisibleOrder = listOf("name", "tags", "status")

            // Act
            val result = viewModel.buildCompleteOrderPreservingHidden(
                completeRelations = completeRelations,
                newVisibleOrder = newVisibleOrder
            )

            // Assert - hiddenProp should stay after name (its original relative position)
            assertEquals(listOf("name", "hiddenProp", "tags", "status"), result)
        }

    @Test
    fun `should preserve multiple hidden properties at their relative positions`() = runTest {
        // Arrange
        objectState = MutableStateFlow(ObjectState.Init)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Original order: name(0), hidden1(1), status(2), hidden2(3), tags(4)
        val completeRelations = listOf(
            createSimpleRelationView(key = "name", isHidden = false),
            createSimpleRelationView(key = "hidden1", isHidden = true),
            createSimpleRelationView(key = "status", isHidden = false),
            createSimpleRelationView(key = "hidden2", isHidden = true),
            createSimpleRelationView(key = "tags", isHidden = false)
        )
        // User sees: name, status, tags
        // User reverses order: tags, status, name
        val newVisibleOrder = listOf("tags", "status", "name")

        // Act
        val result = viewModel.buildCompleteOrderPreservingHidden(
            completeRelations = completeRelations,
            newVisibleOrder = newVisibleOrder
        )

        // Assert - hidden properties are inserted after the last visible item that was originally before them:
        // - hidden1 (orig idx 1): originally after name (idx 0), so inserted after name
        // - hidden2 (orig idx 3): originally after status (idx 2) and name (idx 0), inserted after name (comes later in new order)
        // Both hidden props end up after "name" because name is the last visible item that was before them both
        assertEquals(listOf("tags", "status", "name", "hidden1", "hidden2"), result)
    }

    @Test
    fun `should place hidden property at start if it was originally first`() = runTest {
        // Arrange
        objectState = MutableStateFlow(ObjectState.Init)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        // Original order: hiddenFirst, name, status
        val completeRelations = listOf(
            createSimpleRelationView(key = "hiddenFirst", isHidden = true),
            createSimpleRelationView(key = "name", isHidden = false),
            createSimpleRelationView(key = "status", isHidden = false)
        )
        // User sees: name, status
        // User reorders: status, name
        val newVisibleOrder = listOf("status", "name")

        // Act
        val result = viewModel.buildCompleteOrderPreservingHidden(
            completeRelations = completeRelations,
            newVisibleOrder = newVisibleOrder
        )

        // Assert - hiddenFirst should remain at position 0
        assertEquals(listOf("hiddenFirst", "status", "name"), result)
    }

    @Test
    fun `should handle empty visible order with only hidden properties`() = runTest {
        // Arrange
        objectState = MutableStateFlow(ObjectState.Init)
        viewModel = ObjectSetSettingsViewModel(
            objectState = objectState,
            dispatcher = dispatcher,
            updateDataViewViewer = updateDataViewViewer,
            storeOfRelations = storeOfRelations,
            analytics = analytics,
            deleteRelationFromDataView = deleteRelationFromDataView,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        val completeRelations = listOf(
            createSimpleRelationView(key = "hidden1", isHidden = true),
            createSimpleRelationView(key = "hidden2", isHidden = true)
        )
        val newVisibleOrder = emptyList<String>()

        // Act
        val result = viewModel.buildCompleteOrderPreservingHidden(
            completeRelations = completeRelations,
            newVisibleOrder = newVisibleOrder
        )

        // Assert - hidden properties should be preserved in original order
        assertEquals(listOf("hidden1", "hidden2"), result)
    }

    private fun createSimpleRelationView(
        key: String,
        isHidden: Boolean,
        isVisible: Boolean = !isHidden
    ): SimpleRelationView = SimpleRelationView(
        key = key,
        title = key,
        format = RelationFormat.LONG_TEXT,
        isVisible = isVisible,
        isHidden = isHidden,
        isReadonly = false,
        isDefault = false
    )

    //endregion
} 