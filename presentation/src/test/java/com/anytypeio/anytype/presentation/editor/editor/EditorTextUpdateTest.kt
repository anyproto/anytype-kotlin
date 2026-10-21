package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

/**
 * First content into an existing empty text block forks the block identity:
 * the text reaches the middleware through a single BlockReplace carrying the
 * input — never through BlockTextSetText on the old (shared) block id.
 */
class EditorTextUpdateTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule(UnconfinedTestDispatcher())

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    private fun givenEmptyParagraph(): Block = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = emptyList(),
        content = Block.Content.Text(
            text = "",
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )
    )

    private fun expectedForkParams(target: Block, text: String) = ReplaceBlock.Params(
        context = root,
        target = target.id,
        prototype = Block.Prototype.Text(
            style = Block.Content.Text.Style.P,
            text = text,
            marks = emptyList(),
            fields = Block.Fields.empty()
        )
    )

    @Test
    fun `should send paragraph's text update before closing page when bottom sheet is hidden`() {

        // SETUP

        val block = givenEmptyParagraph()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubReplaceBlock()
        stubClosePage()

        val vm = buildViewModel()

        // TESTING

        val view = BlockView.Text.Paragraph(
            id = block.id,
            text = block.content<TXT>().text
        )

        val updated = view.copy(
            text = "ABCD"
        )

        vm.onStart(id = root, space = defaultSpace)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        vm.onHomeButtonClicked()

        val inOrder = inOrder(replaceBlock, closePage)

        runBlockingTest {
            inOrder.verify(replaceBlock, times(1)).invoke(
                expectedForkParams(target = block, text = updated.text)
            )
            inOrder.verify(closePage, times(1)).async(CloseObject.Params(root, SpaceId(defaultSpace)))
        }

        // Filling an empty block must never set text on the old (shared) id.
        verifyNoInteractions(updateText)

        // RELEASING PENDING COROUTINES

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should send paragraph's text update after delay before closing page when bottom sheet is hidden`() {

        // SETUP

        val block = givenEmptyParagraph()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubReplaceBlock()
        stubClosePage()

        val vm = buildViewModel()

        // TESTING

        val view = BlockView.Text.Paragraph(
            id = block.id,
            text = block.content<TXT>().text
        )

        val updated = view.copy(
            text = "ABCD"
        )

        vm.onStart(id = root, space = defaultSpace)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(replaceBlock, times(1)) {
            invoke(expectedForkParams(target = block, text = updated.text))
        }

        vm.onHomeButtonClicked()

        verifyBlocking(closePage, times(1)) {
            async(CloseObject.Params(root, SpaceId(defaultSpace)))
        }

        verifyNoInteractions(updateText)
    }

    @Test
    fun `should send paragraph's text update before closing page on system-back-pressed event`() {

        // SETUP

        val block = givenEmptyParagraph()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubReplaceBlock()
        stubClosePage()

        val vm = buildViewModel()

        // TESTING

        val view = BlockView.Text.Paragraph(
            id = block.id,
            text = block.content<TXT>().text
        )

        val updated = view.copy(
            text = "ABCD"
        )

        vm.onStart(id = root, space = defaultSpace)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        vm.onSystemBackPressed(false)

        val inOrder = inOrder(replaceBlock, closePage)

        runBlockingTest {
            inOrder.verify(replaceBlock, times(1)).invoke(
                expectedForkParams(target = block, text = updated.text)
            )
            inOrder.verify(closePage, times(1)).async(
                CloseObject.Params(root, SpaceId(defaultSpace))
            )
        }

        verifyNoInteractions(updateText)

        // RELEASING PENDING COROUTINES

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should send paragraph's text update after delay before closing page on system-back-pressed event`() {

        // SETUP

        val block = givenEmptyParagraph()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubReplaceBlock()
        stubClosePage()

        val vm = buildViewModel()

        // TESTING

        val view = BlockView.Text.Paragraph(
            id = block.id,
            text = block.content<TXT>().text
        )

        val updated = view.copy(
            text = "ABCD"
        )

        vm.onStart(id = root, space = defaultSpace)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(replaceBlock, times(1)) {
            invoke(expectedForkParams(target = block, text = updated.text))
        }

        vm.onSystemBackPressed(false)

        verifyBlocking(closePage, times(1)) {
            async(CloseObject.Params(root, SpaceId(defaultSpace)))
        }

        verifyNoInteractions(updateText)
    }
}
