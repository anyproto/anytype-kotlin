package com.anytypeio.anytype.presentation.editor

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class BlockReadModeTest : EditorViewModelTest() {

    val blocks = listOf(
        Block(
            id = MockDataFactory.randomString(),
            content = Block.Content.Text(
                marks = emptyList(),
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        ),
        Block(
            id = MockDataFactory.randomString(),
            content = Block.Content.Text(
                marks = emptyList(),
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )
    )

    val page = listOf(
        Block(
            id = root,
            fields = Block.Fields.empty(),
            content = Block.Content.Smart(),
            children = listOf(header.id) + blocks.map { it.id }
        )
    ) + listOf(header, title) + blocks

    private val blockViewsReadMode = listOf<BlockView>(
        blocks[0].let { p ->
            BlockView.Text.Paragraph(
                id = p.id,
                marks = emptyList(),
                text = p.content<Block.Content.Text>().text,
                mode = BlockView.Mode.READ
            )
        },
        blocks[1].let { p ->
            BlockView.Text.Paragraph(
                id = p.id,
                marks = emptyList(),
                text = p.content<Block.Content.Text>().text,
                mode = BlockView.Mode.READ
            )
        }
    )

    private val blockViewsEditMode = listOf<BlockView>(
        blocks[0].let { p ->
            BlockView.Text.Paragraph(
                id = p.id,
                marks = emptyList(),
                text = p.content<Block.Content.Text>().text,
                mode = BlockView.Mode.EDIT
            )
        },
        blocks[1].let { p ->
            BlockView.Text.Paragraph(
                id = p.id,
                marks = emptyList(),
                text = p.content<Block.Content.Text>().text,
                mode = BlockView.Mode.EDIT
            )
        }
    )

    private val titleEditModeView = BlockView.Title.Basic(
        id = title.id,
        text = title.content<TXT>().text,
        isFocused = false,
        mode = BlockView.Mode.EDIT
    )

    private val titleReadModeView = BlockView.Title.Basic(
        id = title.id,
        text = title.content<TXT>().text,
        isFocused = false,
        mode = BlockView.Mode.READ
    )

    private val flow: Flow<List<Event.Command>> = flow {
        delay(100)
        emit(
            listOf(
                Event.Command.ShowObject(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )
    }

    @Test
    fun `all blocks should be in read mode after long-pressing a simple block and this clicked block should be selected`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(titleReadModeView) + listOf<BlockView>(
                    blocks[0].let { p ->
                        BlockView.Text.Paragraph(
                            id = p.id,
                            marks = emptyList(),
                            text = p.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ
                        )
                    },
                    blocks[1].let { p ->
                        BlockView.Text.Paragraph(
                            id = p.id,
                            marks = emptyList(),
                            text = p.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ,
                            isSelected = true
                        )
                    }
                )
            )
        )
    }

    @Test
    fun `should enter edit mode after action menu is closed by dismiss`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onDismissBlockActionMenu(true)

        val testObserver = vm.state.test()

        val initial = blockViewsEditMode

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(titleEditModeView) + initial
            )
        )
    }

    @Test
    fun `should enter read mode with one selected block after action menu is closed by action item style`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(id = paragraphs[1].id, action = ActionItemType.Style)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(titleReadModeView) + listOf<BlockView>(
                    blocks[0].let { p ->
                        BlockView.Text.Paragraph(
                            id = p.id,
                            marks = emptyList(),
                            text = p.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ
                        )
                    },
                    blocks[1].let { p ->
                        BlockView.Text.Paragraph(
                            id = p.id,
                            isSelected = true,
                            marks = emptyList(),
                            text = p.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ
                        )
                    }
                )
            )
        )
    }

    @Test
    fun `should enter edit mode after action menu is closed by action item turn into`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(id = paragraphs[1].id, action = ActionItemType.TurnInto)

        val testObserver = vm.state.test()

        val initial = blockViewsEditMode

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(titleEditModeView) + initial
            )
        )
    }

    @Test
    fun `should enter edit mode after action menu is closed by action item delete`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(id = paragraphs[1].id, action = ActionItemType.Delete)

        val testObserver = vm.state.test()

        val initial = blockViewsEditMode

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(titleEditModeView) + initial
            )
        )
    }

    @Test
    fun `should enter edit mode after action menu is closed by action item duplicate`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(id = paragraphs[1].id, action = ActionItemType.Duplicate)

        val testObserver = vm.state.test()

        val initial = blockViewsEditMode

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(titleEditModeView) + initial
            )
        )
    }

    @Test
    fun `should enter edit mode after action menu is closed by action item download`() {

        val paragraphs = blocks
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[1].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        vm.onActionMenuItemClicked(id = paragraphs[1].id, action = ActionItemType.Download)

        val testObserver = vm.state.test()

        val initial = blockViewsEditMode

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            testObserver.assertValue(
                ViewState.Success(
                    blocks = listOf(titleEditModeView) + initial
                )
            )
        }
    }
}