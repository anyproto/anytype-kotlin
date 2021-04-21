package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.model.Focusable
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorFocusTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "Relation Block UI Testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should clear focus internally and re-render on hide-keyboard event`() {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().filter { it != Block.Content.Text.Style.DESCRIPTION }.random()
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id, block.id)
            ),
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val testViewStateObserver = vm.state.test()

        val testFocusObserver = vm.focus.test()

        testViewStateObserver.assertValue { value ->
            check(value is ViewState.Success)
            val last = value.blocks.last()
            check(last is Focusable)
            !last.isFocused
        }

        vm.onBlockFocusChanged(
            id = block.id,
            hasFocus = true
        )

        testFocusObserver.assertValue(block.id)

        vm.onHideKeyboardClicked()

        testFocusObserver.assertValue(PageViewModel.EMPTY_FOCUS_ID)
    }

    @Test
    fun `should update views on hide-keyboard event`() {

        // SETUP

        val style = Block.Content.Text.Style.values()
                .filter { style ->
                    style != Block.Content.Text.Style.TITLE || style != Block.Content.Text.Style.DESCRIPTION
                }
                .random()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id, block.id)
            ),
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.state.test().apply {
            assertValue { value ->
                check(value is ViewState.Success)
                val last = value.blocks.last()
                check(last is Focusable)
                !last.isFocused
            }
        }

        vm.onBlockFocusChanged(
            id = block.id,
            hasFocus = true
        )

        vm.onHideKeyboardClicked()

        vm.state.test().apply {
            assertValue { value ->
                check(value is ViewState.Success)
                val last = value.blocks.last()
                check(last is Focusable)
                !last.isFocused
            }
        }

        vm.onOutsideClicked()

        vm.state.test().apply {
            try {
                assertValue { value ->
                    check(value is ViewState.Success)
                    val last = value.blocks.last()
                    check(last is Focusable)
                    last.isFocused
                }
            } catch (e: AssertionError) {
                throw AssertionError("Test assertion failed for style: $style")
            }
        }

        verifyZeroInteractions(createBlock)
    }
}