package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.base.Either
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

/**
 * ViewModel-level coverage for the Kanban board subscription lifecycle (review H1) and the
 * drag write-gating (review M3).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetBoardSubscriptionTest : ObjectSetViewModelTestSetup() {

    private val groupRelationKey = "tag"
    private val boardViewerId = "board-view"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    /** Opens the set as a Collection whose only viewer is a Kanban board grouped by [groupRelationKey]. */
    private fun stubBoardCollection() {
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
    }

    @Test
    fun `re-subscribes to board groups after a background-foreground cycle (H1)`() = runTest {
        stubBoardCollection()
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(emptyList())
        }

        val vm = givenViewModel()

        vm.onStart(view = boardViewerId)
        advanceUntilIdle()
        vm.onStop()
        advanceUntilIdle()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        // Once per onStart: the subscription must be re-established after onStop, not frozen
        // by a surviving distinctUntilChanged cache.
        verify(boardGroupSubscriptionContainer, times(2)).observe(any())
    }

    @Test
    fun `does not write a board card move while groups are not loaded (M3)`() = runTest {
        stubBoardCollection()
        // observe() emits an empty group set, so boardGroups stays empty (client-fallback render).
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(emptyList())
        }

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        vm.onBoardCardDropped(cardId = "card-1", sourceColumnId = "src", targetColumnId = "tgt")
        advanceUntilIdle()

        // No authoritative group ids yet → the move must be refused, not written with a
        // client-fallback column id.
        verifyNoInteractions(setObjectDetails)
    }
}
