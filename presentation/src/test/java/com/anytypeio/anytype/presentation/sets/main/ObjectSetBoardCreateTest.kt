package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

/**
 * ViewModel-level coverage for the per-column Kanban "+ New" (DROID-4529): creating an
 * object in a column prefills that column's group value, and a Viewer cannot create.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetBoardCreateTest : ObjectSetViewModelTestSetup() {

    private val groupRelationKey = "status"
    private val boardViewerId = "board-view"

    private val spaceDefaultTypeId = "space-default-type-id"
    private val spaceDefaultTypeKey = TypeKey("space-default-type-key")

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    /**
     * Opens the set as a Collection whose only viewer is a Kanban board grouped by a status
     * relation, with a resolvable space-default type so the create flow can reach
     * [CreateDataViewObject].
     */
    private suspend fun stubStatusBoardCollection() {
        stringResourceProvider.stub {
            on { getKanbanEmptyColumnTitle() } doReturn "No value"
        }
        val boardViewer = DVViewer(
            id = boardViewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )
        stubOpenObject(
            doc = listOf(StubTitle(), StubDataView(views = listOf(boardViewer), isCollection = true)),
            details = ObjectViewDetails(
                mapOf(root to mapOf(Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()))
            )
        )
        getOptions.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(emptyList<ObjectWrapper.Option>())
        }

        // The board viewer has no explicit type → the create flow falls back to the space
        // default type; make that resolvable.
        storeOfObjectTypes.set(
            spaceDefaultTypeId,
            mapOf(
                Relations.ID to spaceDefaultTypeId,
                Relations.UNIQUE_KEY to spaceDefaultTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to "Space Default Type"
            )
        )
        stubGetDefaultPageType(
            type = spaceDefaultTypeKey,
            name = "Space Default Type",
            spaceId = SpaceId(defaultSpace)
        )
    }

    @Test
    fun `creating an object in a status column prefills the group relation with the option`() = runTest {
        stubStatusBoardCollection()
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(
                listOf(DataViewGroup(id = "g1", value = DataViewGroup.Value.Status("opt1")))
            )
        }
        boardRecordsSubscriptionContainer.stub { on { observe(any()) } doReturn flowOf(emptyMap()) }
        stubAddObjectToCollection()
        stubCreateDataViewObject()

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        vm.onBoardCreateObjectInColumn("g1")
        advanceUntilIdle()

        verifyBlocking(createDataViewObject, times(1)) {
            async(
                argThat {
                    this is CreateDataViewObject.Params.Collection &&
                        prefilled[groupRelationKey] == listOf("opt1")
                }
            )
        }
    }

    @Test
    fun `a Viewer (no edit permission) cannot create in a column`() = runTest {
        stubObservePermissions(SpaceMemberPermissions.READER)
        stubStatusBoardCollection()
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(
                listOf(DataViewGroup(id = "g1", value = DataViewGroup.Value.Status("opt1")))
            )
        }
        boardRecordsSubscriptionContainer.stub { on { observe(any()) } doReturn flowOf(emptyMap()) }

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        vm.onBoardCreateObjectInColumn("g1")
        advanceUntilIdle()

        verifyNoInteractions(createDataViewObject)
    }
}
