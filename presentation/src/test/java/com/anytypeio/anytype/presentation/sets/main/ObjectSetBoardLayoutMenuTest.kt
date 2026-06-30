package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Deterministic ViewModel-level coverage for the Kanban (Board) layout menu's nested
 * "Group by" picker state transitions (open/close, dismiss precedence). These assert
 * transient menu-flag transitions only — no middleware round-trip is required.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetBoardLayoutMenuTest : ObjectSetViewModelTestSetup() {

    private val boardViewerId = "board-view"

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
}
