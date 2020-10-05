package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.page.CreateDocument
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorTitleAddBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should create a new text block after title with INNER position if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf()
        )

        val style = Block.Content.Text.Style.values().random()

        val params = CreateBlock.Params(
            context = root,
            target = root,
            position = Position.INNER,
            prototype = Block.Prototype.Text(style)
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()


        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddTextBlockClicked(style)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new text block at the TOP of the document's first block`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val style = Block.Content.Text.Style.values().random()

        val params = CreateBlock.Params(
            context = root,
            target = block.id,
            position = Position.TOP,
            prototype = Block.Prototype.Text(style)
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()


        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddTextBlockClicked(style)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new document after title with INNER position if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf()
        )

        val params = CreateDocument.Params(
            context = root,
            target = root,
            position = Position.INNER
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateDocument(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddNewPageClicked()
        }

        verifyBlocking(createDocument, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new document at the TOP of the document's first block`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val params = CreateDocument.Params(
            context = root,
            target = block.id,
            position = Position.TOP
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateDocument(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddNewPageClicked()
        }

        verifyBlocking(createDocument, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new file block after title with INNER position if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf()
        )

        val types = Block.Content.File.Type.values().filter { it != Block.Content.File.Type.NONE }

        val type = types.random()

        val params = CreateBlock.Params(
            context = root,
            target = root,
            position = Position.INNER,
            prototype = Block.Prototype.File(
                type = type,
                state = Block.Content.File.State.EMPTY
            )
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddFileBlockClicked(type = type)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new file block at the TOP of the document's first block`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val types = Block.Content.File.Type.values().filter { it != Block.Content.File.Type.NONE }

        val type = types.random()

        val params = CreateBlock.Params(
            context = root,
            target = block.id,
            position = Position.TOP,
            prototype = Block.Prototype.File(
                type = type,
                state = Block.Content.File.State.EMPTY
            )
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddFileBlockClicked(type = type)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new bookmark block after title with INNER position if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf()
        )

        val params = CreateBlock.Params(
            context = root,
            target = root,
            position = Position.INNER,
            prototype = Block.Prototype.Bookmark
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddBookmarkBlockClicked()
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new bookmark block at the TOP of the document's first block`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val params = CreateBlock.Params(
            context = root,
            target = block.id,
            position = Position.TOP,
            prototype = Block.Prototype.Bookmark
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddBookmarkBlockClicked()
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new divider block after title with INNER position if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf()
        )

        val params = CreateBlock.Params(
            context = root,
            target = root,
            position = Position.INNER,
            prototype = Block.Prototype.Divider
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddDividerBlockClicked()
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new divider block at the TOP of the document's first block`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val params = CreateBlock.Params(
            context = root,
            target = block.id,
            position = Position.TOP,
            prototype = Block.Prototype.Divider
        )

        val document = listOf(page)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = root,
                hasFocus = true
            )
            onAddDividerBlockClicked()
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
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
                        events = emptyList()
                    )
                )
            )
        }
    }

    private fun stubCreateDocument(
        params: CreateDocument.Params
    ) {
        createDocument.stub {
            onBlocking { invoke(params) } doReturn Either.Right(
                CreateDocument.Result(
                    id = MockDataFactory.randomUuid(),
                    payload = Payload(
                        context = root,
                        events = emptyList()
                    ),
                    target = MockDataFactory.randomUuid()
                )
            )
        }
    }
}