package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorBackspaceDeleteTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should focus parent text block when its child is deleted`() {

        // SETUP

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

        val child = Block(
            id = "CHILD",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.values().filter { style ->
                    style != Block.Content.Text.Style.TITLE && style != Block.Content.Text.Style.DESCRIPTION && style != Block.Content.Text.Style.CALLOUT
                }.random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(child.id)
        )

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(child.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = parent.id,
                    children = emptyList()
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = child.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(
                id = child.id
            )
        }

        vm.focus.test().assertValue(parent.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        isFocused = false,
                        text = title.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = parent.id,
                        isFocused = true,
                        cursor = parent.content<Block.Content.Text>().text.length,
                        text = parent.content<Block.Content.Text>().text
                    )
                )
            )
        )
    }

    @Test
    fun `should focus previous nested text block when the next one is deleted`() {

        // SETUP

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

        val child1 = Block(
            id = "CHILD1",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val child2 = Block(
            id = "CHILD2",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.values().filter { style ->
                    style != Block.Content.Text.Style.TITLE && style != Block.Content.Text.Style.DESCRIPTION && style != Block.Content.Text.Style.CALLOUT
                }.random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(child2.id)
        )

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(child2.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = parent.id,
                    children = listOf(child1.id)
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = child2.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(
                id = child2.id
            )
        }

        vm.focus.test().assertValue(child1.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        isFocused = false,
                        text = title.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = parent.id,
                        isFocused = false,
                        text = parent.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Bulleted(
                        id = child1.id,
                        isFocused = true,
                        indent = 1,
                        cursor = child1.content<Block.Content.Text>().text.length,
                        text = child1.content<Block.Content.Text>().text
                    )
                )
            )
        )
    }

    @Test
    fun `should focus the previous nested textual block when the next text block is deleted`() {

        // SETUP

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

        val child1 = Block(
            id = "CHILD1-TEXT",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val child2 = Block(
            id = "CHILD2-TEXT",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().filter { style ->
                    style != Block.Content.Text.Style.TITLE && style != Block.Content.Text.Style.DESCRIPTION && style != Block.Content.Text.Style.CALLOUT
                }.random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(child2.id)
        )

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(child2.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = parent.id,
                    children = listOf(child1.id)
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = child2.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(
                id = child2.id
            )
        }

        vm.focus.test().assertValue(child1.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        isFocused = false,
                        text = title.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = parent.id,
                        isFocused = false,
                        text = parent.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Bulleted(
                        indent = 1,
                        id = child1.id,
                        isFocused = true,
                        cursor = child1.content<Block.Content.Text>().text.length,
                        text = child1.content<Block.Content.Text>().text
                    )
                )
            )
        )
    }

    @Test
    fun `should delete the previous bookmark when pressing backspace in an empty text block following this bookmark`() {

        // SETUP

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

        val bookmark = Block(
            id = MockDataFactory.randomString(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Bookmark(
                url = MockDataFactory.randomString(),
                description = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                favicon = null,
                image = null
            )
        )

        val paragraph = Block(
            id = MockDataFactory.randomString(),
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
            content = Block.Content.Smart(),
            children = listOf(header.id, bookmark.id, paragraph.id)
        )

        val document = listOf(page, header, title, bookmark, paragraph)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(bookmark.id)
        )

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(bookmark.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, bookmark.id, paragraph.id)
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = paragraph.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(
                id = paragraph.id
            )
        }

        vm.focus.test().assertValue(paragraph.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        isFocused = false,
                        text = title.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        isFocused = true,
                        text = paragraph.content<Block.Content.Text>().text
                    ),
                )
            )
        )

        verifyBlocking(unlinkBlocks, times(1)) {
            invoke(
                UnlinkBlocks.Params(
                    context = root,
                    targets = listOf(bookmark.id)
                )
            )
        }
    }

    @Test
    fun `should delete the previous bookmark when pressing backspace in an non-empty text block following this bookmark`() {

        // SETUP

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

        val bookmark = Block(
            id = MockDataFactory.randomString(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Bookmark(
                url = MockDataFactory.randomString(),
                description = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                favicon = null,
                image = null
            )
        )

        val paragraph = Block(
            id = MockDataFactory.randomString(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, bookmark.id, paragraph.id)
        )

        val document = listOf(page, header, title, bookmark, paragraph)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(bookmark.id)
        )

        stubOpenDocument(document = document)
        stubUpdateText()
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(bookmark.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, paragraph.id)
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = paragraph.id,
                hasFocus = true
            )
            onNonEmptyBlockBackspaceClicked(
                id = paragraph.id,
                marks = emptyList(),
                text = paragraph.content<Block.Content.Text>().text
            )
        }

        vm.focus.test().assertValue(paragraph.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        isFocused = false,
                        text = title.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        isFocused = true,
                        text = paragraph.content<Block.Content.Text>().text
                    ),
                )
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(unlinkBlocks, times(1)) {
            invoke(
                UnlinkBlocks.Params(
                    context = root,
                    targets = listOf(bookmark.id)
                )
            )
        }
    }
}