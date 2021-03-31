package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorTextUpdateTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
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

    @Test
    fun `should send paragraph's text update before closing page when bottom sheet is hidden`() {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateText()
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

        vm.onStart(root)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        vm.onBottomSheetHidden()

        val inOrder = inOrder(updateText, closePage)

        runBlockingTest {
            inOrder.verify(updateText, times(1)).invoke(
                UpdateText.Params(
                    context = root,
                    text = updated.text,
                    marks = emptyList(),
                    target = block.id
                )
            )
            inOrder.verify(closePage, times(1)).invoke(
                CloseBlock.Params(id = root)
            )
        }

        // RELEASING PENDING COROUTINES

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should send paragraph's text update after delay before closing page when bottom sheet is hidden`() {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateText()
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

        vm.onStart(root)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    text = updated.text,
                    marks = emptyList(),
                    target = block.id
                )
            )
        }

        vm.onBottomSheetHidden()

        verifyBlocking(closePage, times(1)) {
            invoke(CloseBlock.Params(id = root))
        }

        verifyNoMoreInteractions(updateText)
    }

    @Test
    fun `should send paragraph's text update before closing page on system-back-pressed event`() {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateText()
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

        vm.onStart(root)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        vm.onSystemBackPressed(false)

        val inOrder = inOrder(updateText, closePage)

        runBlockingTest {
            inOrder.verify(updateText, times(1)).invoke(
                UpdateText.Params(
                    context = root,
                    text = updated.text,
                    marks = emptyList(),
                    target = block.id
                )
            )
            inOrder.verify(closePage, times(1)).invoke(
                CloseBlock.Params(id = root)
            )
        }

        // RELEASING PENDING COROUTINES

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should send paragraph's text update after delay before closing page on system-back-pressed event`() {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateText()
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

        vm.onStart(root)

        vm.onBlockFocusChanged(
            id = block.id, hasFocus = true
        )

        vm.onTextBlockTextChanged(updated)

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    text = updated.text,
                    marks = emptyList(),
                    target = block.id
                )
            )
        }

        vm.onSystemBackPressed(false)

        verifyBlocking(closePage, times(1)) {
            invoke(CloseBlock.Params(id = root))
        }

        verifyNoMoreInteractions(updateText)
    }
}