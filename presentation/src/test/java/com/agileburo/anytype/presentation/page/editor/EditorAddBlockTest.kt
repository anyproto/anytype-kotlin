package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.CreateBlock
import com.agileburo.anytype.domain.block.interactor.ReplaceBlock
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorAddBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
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
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(block.id)
            ),
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
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(block.id)
            ),
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