package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.viewer.ViewerEvent
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
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ViewModel-level coverage for the Kanban (Board) layout menu:
 *  - transient menu-flag transitions of the nested "Group by" picker (open/close, dismiss
 *    precedence), which assert state only and need no middleware round-trip;
 *  - the actual viewer write path (`groupBackgroundColors`, `groupRelationKey`), seeded with a
 *    real BOARD viewer in the reducer state so [ObjectSetViewModel.onViewerLayoutWidgetAction]
 *    reaches `viewerDelegate.onEvent(ViewerEvent.UpdateView(...))`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetBoardLayoutMenuTest : ObjectSetViewModelTestSetup() {

    private val boardViewerId = "board-view"
    private val seededGroupRelationKey = "tag"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    /** Seeds the layout widget open on a Board viewer without a middleware round-trip. */
    private fun ObjectSetViewModel.openLayoutWidgetOnBoard() {
        viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.copy(
            showWidget = true,
            viewer = boardViewerId,
            layoutType = Block.Content.DataView.Viewer.Type.BOARD
        )
    }

    /**
     * Opens the set as a Collection whose only viewer is a Kanban board, so the reducer holds a
     * real BOARD viewer that the write path can resolve. Mirrors [ObjectSetBoardSubscriptionTest].
     */
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
            groupRelationKey = seededGroupRelationKey
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
        boardGroupSubscriptionContainer.stub {
            on { observe(any()) } doReturn flowOf(emptyList())
        }
    }

    @Test
    fun `GroupByMenu action toggles showGroupByMenu`() = runTest {
        val vm = givenViewModel()
        vm.openLayoutWidgetOnBoard()

        assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)

        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
        assertTrue(vm.viewerLayoutWidgetState.value.showGroupByMenu)

        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
        assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)
    }

    @Test
    fun `Dismiss closes group-by picker first, then widget`() = runTest {
        val vm = givenViewModel()
        vm.openLayoutWidgetOnBoard()
        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
        assertTrue(vm.viewerLayoutWidgetState.value.showGroupByMenu)

        // First dismiss only closes the nested group-by picker; the widget stays open.
        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.Dismiss)
        assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)
        assertTrue(vm.viewerLayoutWidgetState.value.showWidget)

        // Second dismiss closes the whole widget.
        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.Dismiss)
        assertFalse(vm.viewerLayoutWidgetState.value.showWidget)
    }

    @Test
    fun `GroupByUpdate closes the group-by picker`() = runTest {
        val vm = givenViewModel()
        vm.openLayoutWidgetOnBoard()
        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
        assertTrue(vm.viewerLayoutWidgetState.value.showGroupByMenu)

        vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.GroupByUpdate(
                item = ViewerLayoutWidgetUi.State.GroupBy(
                    relationKey = RelationKey("k_tag"),
                    name = "Tag",
                    format = RelationFormat.TAG,
                    isChecked = false
                )
            )
        )
        advanceUntilIdle()

        // Selecting a relation collapses the picker back to the layout widget.
        assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)
    }

    @Test
    fun `ColorColumns action writes groupBackgroundColors to the viewer`() = runTest {
        stubBoardCollection()
        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()
        vm.openLayoutWidgetOnBoard()

        vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.ColorColumns(toggled = true))
        advanceUntilIdle()

        // The toggle is persisted as a viewer update carrying groupBackgroundColors == true.
        verifyBlocking(viewerDelegate) {
            onEvent(
                argThat {
                    this is ViewerEvent.UpdateView &&
                            viewer.id == boardViewerId &&
                            viewer.groupBackgroundColors
                }
            )
        }
    }

    @Test
    fun `GroupByUpdate action writes groupRelationKey to the viewer`() = runTest {
        stubBoardCollection()
        val vm = givenViewModel()
        vm.onStart(view = boardViewerId)
        advanceUntilIdle()
        vm.openLayoutWidgetOnBoard()

        vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.GroupByUpdate(
                item = ViewerLayoutWidgetUi.State.GroupBy(
                    relationKey = RelationKey("k_tag"),
                    name = "Tag",
                    format = RelationFormat.TAG,
                    isChecked = false
                )
            )
        )
        advanceUntilIdle()

        // Picking a not-yet-selected relation persists it as the viewer's groupRelationKey.
        verifyBlocking(viewerDelegate) {
            onEvent(
                argThat {
                    this is ViewerEvent.UpdateView &&
                            viewer.id == boardViewerId &&
                            viewer.groupRelationKey == "k_tag"
                }
            )
        }
    }
}
