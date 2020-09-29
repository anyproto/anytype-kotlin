package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_ui.common.Focusable
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.presentation.page.PageViewModel
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
                style = Block.Content.Text.Style.values().random()
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(block.id)
            ),
            block
        )

        stubInterceptEvents()
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

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(block.id)
            ),
            block
        )

        stubInterceptEvents()
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
            assertValue { value ->
                check(value is ViewState.Success)
                val last = value.blocks.last()
                check(last is Focusable)
                last.isFocused
            }
        }

        verifyZeroInteractions(createBlock)
    }
}