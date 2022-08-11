package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.page.CreateDocument
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorTitleAddBlockTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
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
    fun `should create a new text block after title with INNER position if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val style = Block.Content.Text.Style.values().random()

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.Text(style)
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()


        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddTextBlockClicked(style)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new text block below title`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val style = Block.Content.Text.Style.values().random()

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.Text(style)
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()


        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddTextBlockClicked(style)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new document after title if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val params = CreateDocument.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateDocument(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddNewPageClicked()
        }

        verifyBlocking(createDocument, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new document below document title`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val params = CreateDocument.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateDocument(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddNewPageClicked()
        }

        verifyBlocking(createDocument, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new file block below title if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val types = Block.Content.File.Type.values().filter { it != Block.Content.File.Type.NONE }

        val type = types.random()

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.File(
                type = type,
                state = Block.Content.File.State.EMPTY
            )
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddFileBlockClicked(type = type)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new file block below document title`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val types = Block.Content.File.Type.values().filter { it != Block.Content.File.Type.NONE }

        val type = types.random()

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.File(
                type = type,
                state = Block.Content.File.State.EMPTY
            )
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
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
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.Bookmark.New
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddBookmarkBlockClicked()
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new bookmark block below title block`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.Bookmark.New
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddBookmarkBlockClicked()
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new divider block after title if document has only title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.DividerLine
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddDividerBlockClicked(style = Block.Content.Divider.Style.LINE)
        }

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should create a new divider block after document title`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val params = CreateBlock.Params(
            context = root,
            target = title.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.DividerLine
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubCreateBlock(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            onAddDividerBlockClicked(style = Block.Content.Divider.Style.LINE)
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