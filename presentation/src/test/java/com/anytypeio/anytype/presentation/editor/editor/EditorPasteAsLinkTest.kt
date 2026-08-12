package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.VIRTUAL_TRAILING_BLOCK_ID
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking

/**
 * "Paste link" on a caret (no selection) first inserts the url into the block,
 * then asks the view model to apply the link mark over the inserted text.
 * Both steps arrive synchronously from the same action-mode callback.
 */
class EditorPasteAsLinkTest : EditorPresentationTestSetup() {

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

    private fun stubUpdateLinkMark() {
        updateLinkMark.stub {
            on { invoke(any(), any(), any()) } doAnswer { invocation ->
                val params = invocation.getArgument<UpdateLinkMarks.Params>(1)
                val onResult = invocation
                    .getArgument<(Either<Throwable, List<Block.Content.Text.Mark>>) -> Unit>(2)
                onResult(Either.Right(params.marks + params.newMark))
            }
        }
    }

    private fun stubReplaceBlock(empty: Block, forked: Block) {
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

    private fun stubCreateBlock(created: Block, siblings: List<String>) {
        createBlock.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                Pair(
                    created.id,
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(created)
                            ),
                            Event.Command.UpdateStructure(
                                context = root,
                                id = root,
                                children = listOf(header.id) + siblings + created.id
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should apply the link mark over the text pasted at the caret of a non-empty block`() =
        runTest {

            // SETUP

            val before = "Anytype "
            val url = "https://anytype.io"
            val block = StubParagraph(text = before)

            stubInterceptEvents()
            stubOpenDocument(givenDocument(block))
            stubUpdateText()
            stubUpdateLinkMark()

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)

            advanceUntilIdle()

            // TESTING

            vm.onBlockFocusChanged(id = block.id, hasFocus = true)
            vm.onSelectionChanged(id = block.id, selection = before.length..before.length)

            advanceUntilIdle()

            // The action-mode callback inserts the url into the EditText, which
            // reports the new text and then the selection covering the insert.
            vm.onTextBlockTextChanged(
                BlockView.Text.Paragraph(id = block.id, text = before + url)
            )
            vm.onSelectionChanged(
                id = block.id,
                selection = before.length..(before.length + url.length)
            )
            vm.proceedToAddUriToTextAsLink(url)

            advanceUntilIdle()

            // The last write must carry the pasted text AND the link mark over it.
            val captor = argumentCaptor<UpdateText.Params>()

            verifyBlocking(updateText, atLeastOnce()) { invoke(params = captor.capture()) }

            val last = captor.lastValue

            kotlin.test.assertEquals(before + url, last.text)
            kotlin.test.assertEquals(
                listOf(
                    Block.Content.Text.Mark(
                        range = before.length..(before.length + url.length),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = url
                    )
                ),
                last.marks
            )

            // The pasted url renders as a link straight away — nothing echoes the
            // set-text write back, so only the local re-render can show it.
            val rendered = vm.views.filterIsInstance<BlockView.Text.Paragraph>()
                .first { it.id == block.id }

            kotlin.test.assertEquals(before + url, rendered.text)
            kotlin.test.assertEquals(
                listOf(
                    Markup.Mark.Link(
                        from = before.length,
                        to = before.length + url.length,
                        param = url
                    )
                ),
                rendered.marks
            )
        }

    @Test
    fun `should apply the link mark over the text pasted into an empty block`() = runTest {

        // SETUP

        val url = "https://anytype.io"
        val empty = StubParagraph(text = "")
        val forked = StubParagraph(text = url)

        stubInterceptEvents()
        stubOpenDocument(givenDocument(empty))
        stubUpdateText()
        stubUpdateLinkMark()
        stubReplaceBlock(empty = empty, forked = forked)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onBlockFocusChanged(id = empty.id, hasFocus = true)
        vm.onSelectionChanged(id = empty.id, selection = 0..0)

        advanceUntilIdle()

        vm.onTextBlockTextChanged(BlockView.Text.Paragraph(id = empty.id, text = url))
        vm.onSelectionChanged(id = empty.id, selection = 0..url.length)
        vm.proceedToAddUriToTextAsLink(url)

        advanceUntilIdle()

        val captor = argumentCaptor<UpdateText.Params>()

        verifyBlocking(updateText, atLeastOnce()) { invoke(params = captor.capture()) }

        val last = captor.lastValue

        kotlin.test.assertEquals(forked.id, last.target)
        kotlin.test.assertEquals(url, last.text)
        kotlin.test.assertEquals(
            listOf(
                Block.Content.Text.Mark(
                    range = 0..url.length,
                    type = Block.Content.Text.Mark.Type.LINK,
                    param = url
                )
            ),
            last.marks
        )
    }

    @Test
    fun `should apply the link mark over the text pasted into the trailing placeholder`() =
        runTest {

            // SETUP

            val url = "https://anytype.io"
            val block = StubParagraph(text = "Anytype")
            val created = StubParagraph(text = url)

            stubInterceptEvents()
            stubOpenDocument(givenDocument(block))
            stubUpdateText()
            stubUpdateLinkMark()
            stubCreateBlock(created = created, siblings = listOf(block.id))

            val vm = buildViewModel()

            vm.onStart(id = root, space = defaultSpace)

            advanceUntilIdle()

            // TESTING

            // Tap on the empty space at the end shows the trailing placeholder.
            vm.onOutsideClicked()

            advanceUntilIdle()

            vm.onTextBlockTextChanged(
                BlockView.Text.Paragraph(id = VIRTUAL_TRAILING_BLOCK_ID, text = url)
            )
            vm.onSelectionChanged(id = VIRTUAL_TRAILING_BLOCK_ID, selection = 0..url.length)
            vm.proceedToAddUriToTextAsLink(url)

            advanceUntilIdle()

            val captor = argumentCaptor<UpdateText.Params>()

            verifyBlocking(updateText, atLeastOnce()) { invoke(params = captor.capture()) }

            val last = captor.lastValue

            kotlin.test.assertEquals(created.id, last.target)
            kotlin.test.assertEquals(url, last.text)
            kotlin.test.assertEquals(
                listOf(
                    Block.Content.Text.Mark(
                        range = 0..url.length,
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = url
                    )
                ),
                last.marks
            )
        }

    @Test
    fun `should apply the link mark over the selected text without inserting anything`() = runTest {

        // SETUP

        val text = "Anytype site"
        val url = "https://anytype.io"
        val block = StubParagraph(text = text)

        stubInterceptEvents()
        stubOpenDocument(givenDocument(block))
        stubUpdateText()
        stubUpdateLinkMark()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onBlockFocusChanged(id = block.id, hasFocus = true)
        vm.onSelectionChanged(id = block.id, selection = 0..7)

        advanceUntilIdle()

        // No insert: the action mode only reports the link for the selection.
        vm.proceedToAddUriToTextAsLink(url)

        advanceUntilIdle()

        val captor = argumentCaptor<UpdateText.Params>()

        verifyBlocking(updateText, atLeastOnce()) { invoke(params = captor.capture()) }

        val last = captor.lastValue

        kotlin.test.assertEquals(text, last.text)
        kotlin.test.assertEquals(
            listOf(
                Block.Content.Text.Mark(
                    range = 0..7,
                    type = Block.Content.Text.Mark.Type.LINK,
                    param = url
                )
            ),
            last.marks
        )
    }
}
