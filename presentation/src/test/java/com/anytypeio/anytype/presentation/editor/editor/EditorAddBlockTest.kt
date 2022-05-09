package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class EditorAddBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should replace currently focused text block instead of adding a new block after this text block if this text block is empty`() {

        // SETUP

        val text = ""

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = text,
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, block.id)
            ),
            header,
            title,
            block
        )

        val newStyle = Block.Content.Text.Style.values().random()

        val params = ReplaceBlock.Params(
            context = root,
            target = block.id,
            prototype = Block.Prototype.Text(
                style = newStyle
            )
        )

        stubInterceptEvents()
        stubOpenDocument(document = page)
        stubReplaceBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = block.id, hasFocus = true)
            onAddBlockToolbarClicked()
        }

        // User is now selecting a new text block

        vm.onAddTextBlockClicked(style = newStyle)

        verifyZeroInteractions(createBlock)

        verifyBlocking(replaceBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should add new text block after currently focused text block if this focused text block is not empty`() {

        // SETUP

        val text = MockDataFactory.randomString()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = text,
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, block.id)
            ),
            header,
            title,
            block
        )

        val newStyle = Block.Content.Text.Style.values().random()

        val params = CreateBlock.Params(
            context = root,
            target = block.id,
            prototype = Block.Prototype.Text(
                style = newStyle
            ),
            position = Position.BOTTOM
        )

        stubInterceptEvents()
        stubOpenDocument(document = page)
        stubCreateBlock(params)


        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = block.id, hasFocus = true)
            onAddBlockToolbarClicked()
        }

        // User is now selecting a new text block

        vm.onAddTextBlockClicked(style = newStyle)

        verifyZeroInteractions(replaceBlock)
        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    private fun stubReplaceBlock(
        params: ReplaceBlock.Params
    ) {
        replaceBlock.stub {
            onBlocking { invoke(params) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomUuid(),
                    Payload(
                        context = root,
                        emptyList()
                    )
                )
            )
        }
    }

    private fun stubCreateBlock(
        params: CreateBlock.Params
    ) {
        createBlock.stub {
            onBlocking { invoke(params) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomUuid(),
                    Payload(
                        context = root,
                        emptyList()
                    )
                )
            )
        }
    }
}