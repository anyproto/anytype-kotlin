package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

/**
 * Filling any existing EMPTY text block forks the block identity: the first
 * real input is sent as a single BlockReplace carrying the input plus
 * everything the empty block already had, so two clients filling the same
 * empty block concurrently end up with two blocks — both texts intact.
 */
class EditorIdentityForkTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    val title = StubTitle()
    val header = StubHeader(children = listOf(title.id))

    private fun givenDocument(vararg blocks: Block): List<Block> {
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id) + blocks.map { it.id }
        )
        return listOf(page, header, title) + blocks
    }

    @Test
    fun `should preserve style and formatting of the empty block in the replace prototype`() = runTest {

        // SETUP

        val checkbox = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(mapOf("key" to "value")),
            children = emptyList(),
            backgroundColor = "yellow",
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.CHECKBOX,
                isChecked = true,
                color = "red",
                align = Block.Align.AlignCenter
            )
        )

        stubInterceptEvents()
        stubOpenDocument(givenDocument(checkbox))
        stubReplaceBlock()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onTextBlockTextChanged(
            BlockView.Text.Checkbox(
                id = checkbox.id,
                text = "h",
                isChecked = true
            )
        )

        advanceUntilIdle()

        verifyBlocking(replaceBlock, times(1)) {
            invoke(
                params = eq(
                    ReplaceBlock.Params(
                        context = root,
                        target = checkbox.id,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.CHECKBOX,
                            text = "h",
                            marks = emptyList(),
                            checked = true,
                            color = "red",
                            align = Block.Align.AlignCenter,
                            backgroundColor = "yellow",
                            fields = Block.Fields(mapOf("key" to "value"))
                        )
                    )
                )
            )
        }
        verifyNoInteractions(updateText)
    }

    @Test
    fun `should not fork an empty toggle with children - regular set-text flow`() = runTest {

        // SETUP

        val child = StubParagraph()
        val toggle = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(child.id),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.TOGGLE
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, toggle.id)
        )

        stubInterceptEvents()
        stubOpenDocument(listOf(page, header, title, toggle, child))
        stubUpdateText()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onTextBlockTextChanged(
            BlockView.Text.Toggle(
                id = toggle.id,
                text = "h"
            )
        )

        advanceUntilIdle()

        // Replacing a toggle with children would orphan them — text is set
        // on the existing block instead.
        verifyNoInteractions(replaceBlock)
        verifyBlocking(updateText, times(1)) {
            invoke(any())
        }
    }

    @Test
    fun `should not fork a table cell`() = runTest {

        // SETUP

        val cell = StubParagraph(text = "")
        val row = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(cell.id),
            content = Block.Content.TableRow(isHeader = false)
        )

        val block = StubParagraph()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        // The row/cell pair exists in the document but is deliberately not
        // rendered — only the parent lookup matters for this test.
        stubInterceptEvents()
        stubOpenDocument(listOf(page, header, title, block, row, cell))
        stubUpdateText()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = cell.id,
                text = "h"
            )
        )

        advanceUntilIdle()

        // Table cell ids are structural (row-column composites) — never replaced.
        verifyNoInteractions(replaceBlock)
        verifyBlocking(updateText, times(1)) {
            invoke(any())
        }
    }

    @Test
    fun `should keep markdown shortcut flow for empty blocks - style replace without text`() = runTest {

        // SETUP

        val block = StubParagraph(text = "")

        stubInterceptEvents()
        stubOpenDocument(givenDocument(block))
        stubReplaceBlock()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = block.id,
                text = "# "
            )
        )

        advanceUntilIdle()

        // The markdown pattern already allocates a new id via BlockReplace.
        verifyBlocking(replaceBlock, times(1)) {
            invoke(
                params = eq(
                    ReplaceBlock.Params(
                        context = root,
                        target = block.id,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.H1
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should swap the empty block for the forked block preserving text and focus`() = runTest {

        // SETUP

        val empty = StubParagraph(text = "")
        val forked = StubParagraph(text = "h")

        stubInterceptEvents()
        stubOpenDocument(givenDocument(empty))

        replaceBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    forked.id,
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = root,
                                children = listOf(header.id, forked.id)
                            ),
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(forked)
                            ),
                            Event.Command.DeleteBlock(
                                context = root,
                                targets = listOf(empty.id)
                            )
                        )
                    )
                )
            )
        }

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onBlockFocusChanged(id = empty.id, hasFocus = true)

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = empty.id,
                text = "h"
            )
        )

        advanceUntilIdle()

        // The old id is gone; the forked block carries the text and the focus.
        assertTrue(vm.views.none { it.id == empty.id })

        val last = vm.views.last() as BlockView.Text.Paragraph
        assertEquals(expected = forked.id, actual = last.id)
        assertEquals(expected = "h", actual = last.text)
        assertTrue(last.isFocused, "Focus must move to the forked block")

        // The block was replaced already carrying the text — no set-text needed.
        verifyNoInteractions(updateText)
    }

    @Test
    fun `should not send any text write when text and marks are unchanged`() = runTest {

        // SETUP

        val block = StubParagraph()

        stubInterceptEvents()
        stubOpenDocument(givenDocument(block))
        stubUpdateText()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        // Cursor placement / focus-blur flushes re-emit the unchanged text.
        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = block.id,
                text = block.content.asText().text
            )
        )

        advanceUntilIdle()

        // No change — no last-writer-wins write that could stomp a peer edit.
        verifyNoInteractions(updateText)
        verifyNoInteractions(replaceBlock)
    }

    private fun stubReplaceBlockWithSwap(empty: Block, forked: Block) {
        replaceBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    forked.id,
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.UpdateStructure(
                                context = root,
                                id = root,
                                children = listOf(header.id, forked.id)
                            ),
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(forked)
                            ),
                            Event.Command.DeleteBlock(
                                context = root,
                                targets = listOf(empty.id)
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should defer link paste while the fork is in flight and apply the mark to the forked block`() = runTest {

        // SETUP

        val empty = StubParagraph(text = "")
        val url = "https://anytype.io"
        val forked = StubParagraph(text = url)

        stubInterceptEvents()
        stubOpenDocument(givenDocument(empty))
        stubUpdateText()
        stubReplaceBlockWithSwap(empty, forked)

        updateLinkMark.stub {
            on { invoke(any(), any(), any()) } doAnswer { invocation ->
                val params = invocation.getArgument<UpdateLinkMarks.Params>(1)
                val onResult = invocation
                    .getArgument<(Either<Throwable, List<Block.Content.Text.Mark>>) -> Unit>(2)
                onResult(Either.Right(params.marks + params.newMark))
            }
        }

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onBlockFocusChanged(id = empty.id, hasFocus = true)
        vm.onSelectionChanged(id = empty.id, selection = 0..url.length)

        advanceUntilIdle()

        // "Paste link" inserts the url into the block (the identity fork starts)...
        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = empty.id,
                text = url
            )
        )

        // ...and applies the link mark while the replace is still in flight.
        vm.proceedToAddUriToTextAsLink(url)

        advanceUntilIdle()

        // The mark reaches the middleware against the forked id carrying the url
        // text — never as an empty-text write racing the in-flight replace.
        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        target = forked.id,
                        text = url,
                        marks = listOf(
                            Block.Content.Text.Mark(
                                range = 0..url.length,
                                type = Block.Content.Text.Mark.Type.LINK,
                                param = url
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should defer paste while the fork is in flight and replay it against the forked block`() = runTest {

        // SETUP

        val empty = StubParagraph(text = "")
        val forked = StubParagraph(text = "h")

        stubInterceptEvents()
        stubOpenDocument(givenDocument(empty))
        stubPaste()
        stubReplaceBlockWithSwap(empty, forked)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onBlockFocusChanged(id = empty.id, hasFocus = true)

        advanceUntilIdle()

        // Typing starts the fork...
        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = empty.id,
                text = "h"
            )
        )

        // ...and the paste arrives while the replace is still in flight.
        vm.onPaste(range = 1..1)

        advanceUntilIdle()

        // The paste is replayed against the forked block — never the old id,
        // which the replace removes.
        verifyBlocking(paste, times(1)) {
            invoke(
                params = eq(
                    Paste.Params(
                        context = root,
                        focus = forked.id,
                        range = 1..1,
                        selected = emptyList(),
                        isPartOfBlock = null
                    )
                )
            )
        }
    }
}
