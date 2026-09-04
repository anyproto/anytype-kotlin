package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.EditorFocusSnapshot
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

/**
 * A rotation recreates the activity and the editor with it. The focused block and the caret must
 * come back where the user left them (DROID-4586, DROID-3404). The fragment saves
 * [EditorViewModel.focusSnapshot] in its instance state and hands it back through
 * [EditorViewModel.onRestoreSavedState] before the document opens again.
 */
class EditorFocusRestoreTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    private val title = StubTitle()
    private val header = StubHeader(children = listOf(title.id))
    private val paragraph = StubParagraph(text = "hello world")

    private val document = listOf(
        Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraph.id)
        ),
        header,
        title,
        paragraph
    )

    private fun openDocument(): EditorViewModel {
        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        return buildViewModel()
    }

    @Test
    fun `focus snapshot carries the focused block and its caret`() = runTest {
        val vm = openDocument()
        vm.onStart(id = root, space = defaultSpace)
        coroutineTestRule.advanceUntilIdle()

        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = true)
        vm.onSelectionChanged(id = paragraph.id, selection = 3..3)
        coroutineTestRule.advanceUntilIdle()

        assertEquals(
            expected = EditorFocusSnapshot(
                blockId = paragraph.id,
                selectionStart = 3,
                selectionEnd = 3
            ),
            actual = vm.focusSnapshot
        )
    }

    @Test
    fun `focus snapshot is empty while no block has focus`() = runTest {
        val vm = openDocument()
        vm.onStart(id = root, space = defaultSpace)
        coroutineTestRule.advanceUntilIdle()

        assertNull(vm.focusSnapshot)
    }

    @Test
    fun `a restored snapshot focuses the block with the caret once the document opens`() = runTest {
        val vm = openDocument()

        vm.onRestoreSavedState(
            uploadMediaDescription = null,
            focus = EditorFocusSnapshot(
                blockId = paragraph.id,
                selectionStart = 3,
                selectionEnd = 3
            )
        )
        vm.onStart(id = root, space = defaultSpace)
        coroutineTestRule.advanceUntilIdle()

        val rendered = (vm.state.value as ViewState.Success).blocks
            .first { it.id == paragraph.id } as BlockView.Text.Paragraph
        assertTrue(rendered.isFocused, "the restored block is focused")
        assertEquals(expected = 3, actual = rendered.cursor)
    }

    @Test
    fun `a restored caret is reported by the next snapshot`() = runTest {
        val vm = openDocument()
        val snapshot = EditorFocusSnapshot(
            blockId = paragraph.id,
            selectionStart = 3,
            selectionEnd = 3
        )

        vm.onRestoreSavedState(uploadMediaDescription = null, focus = snapshot)
        vm.onStart(id = root, space = defaultSpace)
        coroutineTestRule.advanceUntilIdle()

        // A second rotation right after the first must save the same caret again.
        assertEquals(expected = snapshot, actual = vm.focusSnapshot)
    }

    @Test
    fun `a snapshot for a block that is gone leaves nothing focused`() = runTest {
        val vm = openDocument()

        vm.onRestoreSavedState(
            uploadMediaDescription = null,
            focus = EditorFocusSnapshot(
                blockId = "block-deleted-on-another-device",
                selectionStart = 0,
                selectionEnd = 0
            )
        )
        vm.onStart(id = root, space = defaultSpace)
        coroutineTestRule.advanceUntilIdle()

        val rendered = (vm.state.value as ViewState.Success).blocks
            .first { it.id == paragraph.id } as BlockView.Text.Paragraph
        assertTrue(!rendered.isFocused, "an unknown block must not steal focus")
        assertNull(vm.focusSnapshot)
    }
}
