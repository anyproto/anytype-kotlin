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
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ViewEditAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        // observe() emits an empty group set, so boardGroups stays empty (no columns rendered).
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(emptyList())
        }

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        vm.onBoardCardDropped(cardId = "card-1", sourceColumnId = "src", targetColumnId = "tgt")
        advanceUntilIdle()

        // No authoritative group ids yet → the move must be refused, not written with an
        // unresolved column id.
        verifyNoInteractions(setObjectDetails)
    }

    @Test
    fun `board is driven by per-column record subscriptions once groups load (H3)`() = runTest {
        stubBoardCollection()
        // Groups load → per-column record subscriptions should be started.
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(
                listOf(DataViewGroup(id = "g1", value = DataViewGroup.Value.Status("opt1")))
            )
        }
        boardRecordsSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(emptyMap())
        }

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        // The board sources its cards from per-column subscriptions (not the flat 50-record
        // window, which is gated off for boards).
        verify(boardRecordsSubscriptionContainer, atLeastOnce()).observe(any())
    }

    @Test
    fun `load more grows the requested column's subscription`() = runTest {
        val vm = givenViewModel()

        vm.onBoardColumnLoadMore("g1")

        verify(boardRecordsSubscriptionContainer).loadMore("g1", ObjectSetConfig.DEFAULT_LIMIT)
    }

    @Test
    fun `view-type picker offers Kanban when the flag is on`() = runTest {
        val vm = givenViewModel()
        advanceUntilIdle()

        vm.onViewerEditWidgetAction(ViewEditAction.Layout(id = "view-1"))

        assertTrue(vm.viewerLayoutWidgetState.value.kanbanEnabled)
    }

    @Test
    fun `view-type picker hides Kanban when the flag is off`() = runTest {
        userSettingsRepository.stub { on { observeKanbanEnabled() } doReturn flowOf(false) }
        val vm = givenViewModel()
        advanceUntilIdle()

        vm.onViewerEditWidgetAction(ViewEditAction.Layout(id = "view-1"))

        assertFalse(vm.viewerLayoutWidgetState.value.kanbanEnabled)
    }

    @Test
    fun `board is inert when the experimental Kanban flag is off`() = runTest {
        // Override the default (enabled) stub: Kanban experimental flag is off.
        userSettingsRepository.stub { on { observeKanbanEnabled() } doReturn flowOf(false) }
        stubBoardCollection()

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        // No board group/record subscriptions are started for a BOARD view while the flag is off.
        verify(boardGroupSubscriptionContainer, never()).observe(any())
        verify(boardRecordsSubscriptionContainer, never()).observe(any())
    }

    @Test
    fun `a cross-column move persists the drop position in the target column`() = runTest {
        stubBoardCollection()
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(
                listOf(DataViewGroup(id = "g1", value = DataViewGroup.Value.Status("opt1")))
            )
        }
        boardRecordsSubscriptionContainer.stub { on { observe(any()) } doReturn flowOf(emptyMap()) }
        setObjectDetails.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Payload(context = root, events = emptyList()))
        }
        setDataViewObjectOrder.stub {
            onBlocking { async(any()) } doReturn Resultat.Success(Payload(context = root, events = emptyList()))
        }

        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()

        vm.onBoardCardDropped(
            cardId = "card-1",
            sourceColumnId = "g0",
            targetColumnId = "g1",
            targetOrderedIds = listOf("x", "card-1")
        )
        advanceUntilIdle()

        // Relation move + an order write that places the card at the drop position.
        verifyBlocking(setDataViewObjectOrder) { async(any()) }
    }
}
