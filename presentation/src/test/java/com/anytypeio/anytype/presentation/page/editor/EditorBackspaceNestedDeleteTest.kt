package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorBackspaceNestedDeleteTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
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
                style = Block.Content.Text.Style.values().random()
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
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
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
                    BlockView.Title.Document(
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
                style = Block.Content.Text.Style.values().random()
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
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
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
                    BlockView.Title.Document(
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
    fun `should focus the first previous nested text block when the first next text block is deleted`() {

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
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val child2 = Block(
            id = "CHILD2-FILE",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                hash = MockDataFactory.randomUuid(),
                state = Block.Content.File.State.UPLOADING
            )
        )

        val child3 = Block(
            id = "CHILD3-TEXT",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id, child3.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
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
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2, child3)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(child3.id)
        )

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(child3.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = parent.id,
                    children = listOf(child1.id, child2.id)
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = child3.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(
                id = child3.id
            )
        }

        vm.focus.test().assertValue(child1.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Document(
                        id = title.id,
                        isFocused = false,
                        text = title.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = parent.id,
                        isFocused = false,
                        text = parent.content<Block.Content.Text>().text
                    ),
                    BlockView.Text.Numbered(
                        id = child1.id,
                        isFocused = true,
                        indent = 1,
                        cursor = child1.content<Block.Content.Text>().text.length,
                        text = child1.content<Block.Content.Text>().text,
                        number = 1
                    ),
                    BlockView.Upload.Picture(
                        id = child2.id,
                        indent = 1
                    )
                )
            )
        )
    }

    @Test
    fun `should focus the first previous nested textual block when the first next text block is deleted`() {

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
            id = "CHILD2-FILE",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                hash = MockDataFactory.randomUuid(),
                state = Block.Content.File.State.UPLOADING
            )
        )

        val child3 = Block(
            id = "CHILD3-TEXT",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id, child3.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
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
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2, child3)

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(child3.id)
        )

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubUnlinkBlocks(
            params = params,
            events = listOf(
                Event.Command.DeleteBlock(
                    context = root,
                    targets = listOf(child3.id)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = parent.id,
                    children = listOf(child1.id, child2.id)
                )
            )

        )

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = child3.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(
                id = child3.id
            )
        }

        vm.focus.test().assertValue(child1.id)
        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Document(
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
                    ),
                    BlockView.Upload.Picture(
                        id = child2.id,
                        indent = 1
                    )
                )
            )
        )
    }
}