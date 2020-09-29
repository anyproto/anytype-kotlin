package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorEmptySpaceInteractionTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should ignore outside click if document isn't started yet`() {
        val vm = buildViewModel()
        vm.onOutsideClicked()
        verifyZeroInteractions(createBlock)
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if page contains only title and icon`() {

        // SETUP

        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        stubInterceptEvents()
        stubOpenDocument(page)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onOutsideClicked()

        verifyBlocking(createBlock, times(1)) {
            invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = "",
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if the last block is a link block`() {

        // SETUP

        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTitleAndOnePageLinkBlock(
            rootId = root,
            titleBlockId = firstChild,
            pageBlockId = secondChild
        )

        stubInterceptEvents()
        stubOpenDocument(page)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onOutsideClicked()

        verifyBlocking(createBlock, times(1)) {
            invoke(
                params = eq(
                    CreateBlock.Params(
                        target = "",
                        context = root,
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should not create a new paragraph but focus the last empty block`() {

        // SETUP

        val style =
            Block.Content.Text.Style.values().filter { it != Block.Content.Text.Style.TITLE }
                .random()

        val pic = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.DONE
            ),
            fields = Block.Fields.empty(),
            children = emptyList()
        )

        val txt = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val doc = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(pic.id, txt.id)
            ),
            pic,
            txt
        )

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Checking that no text block is focused

        vm.state.test().assertValue { value ->
            check(value is ViewState.Success)
            value.blocks.none { it is BlockView.Text && it.isFocused }
        }

        vm.onOutsideClicked()

        verifyZeroInteractions(createBlock)

        // Checking that the last text block is focused and has empty text

        vm.state.test().assertValue { value ->
            check(value is ViewState.Success)
            val last = value.blocks.last()
            check(last is BlockView.Text || last is BlockView.Code)
            when (last) {
                is BlockView.Code -> last.text.isEmpty() && last.isFocused
                is BlockView.Text -> last.text.isEmpty() && last.isFocused
                else -> throw IllegalStateException()
            }
        }
    }
}