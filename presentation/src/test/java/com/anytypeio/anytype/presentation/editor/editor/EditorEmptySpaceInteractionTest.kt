package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.StubCodeSnippet
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLinkToObjectBlock
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.VIRTUAL_TRAILING_BLOCK_ID
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

class EditorEmptySpaceInteractionTest : EditorPresentationTestSetup() {

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

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    private fun trailingPlaceholderOrNull(vm: com.anytypeio.anytype.presentation.editor.EditorViewModel): BlockView.Text.Paragraph? =
        vm.views.lastOrNull()?.takeIf { it.id == VIRTUAL_TRAILING_BLOCK_ID } as? BlockView.Text.Paragraph

    @Test
    fun `should ignore outside click if document isn't started yet`() = runTest {
        val vm = buildViewModel()
        vm.onOutsideClicked()
        advanceUntilIdle()
        verifyNoInteractions(createBlock)
    }

    @Test
    fun `should show focused trailing placeholder without any middleware calls on outside click if page contains only title with icon`() = runTest {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id)
        )

        val doc = listOf(page, header, title)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        // Clicking the empty area must produce zero middleware calls.
        verifyNoInteractions(createBlock)
        verifyNoInteractions(updateText)

        val placeholder = trailingPlaceholderOrNull(vm)
        assertTrue(
            placeholder != null && placeholder.isFocused,
            "Expected a focused virtual trailing placeholder as the last view"
        )
    }

    @Test
    fun `should show trailing placeholder instead of creating a block if the last block is a non-empty paragraph`() = runTest {

        // SETUP

        val block = StubParagraph(text = MockDataFactory.randomString())

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(trailingPlaceholderOrNull(vm) != null)
    }

    @Test
    fun `should show trailing placeholder instead of creating a block if the last block is a link block`() = runTest {

        // SETUP

        val link = StubLinkToObjectBlock()

        val root = StubSmartBlock(
            id = root,
            children = listOf(
                header.id,
                link.id
            )
        )

        val doc = listOf(
            root, header, title, link
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(doc)
        stubGetNetworkMode()

        val vm = buildViewModel()

        vm.onStart(id = root.id, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(trailingPlaceholderOrNull(vm) != null)
    }

    @Test
    fun `should not show trailing placeholder on outside-clicked event if object has restriction BLOCKS`() = runTest {

        // SETUP

        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTitleAndOnePageLinkBlock(
            rootId = root,
            titleBlockId = firstChild,
            pageBlockId = secondChild
        )

        stubInterceptEvents()
        stubOpenDocument(document = page, objectRestrictions = listOf(ObjectRestriction.BLOCKS))

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(trailingPlaceholderOrNull(vm) == null)
    }

    @Test
    fun `should show trailing placeholder instead of creating a block if the last block is a table block`() = runTest {

        // SETUP

        val table = StubTable(children = listOf())
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = Block(
            id = root,
            children = listOf(header.id) + listOf(table.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val document = listOf(page, header, title, table)

        stubInterceptEvents()
        stubOpenDocument(document)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(trailingPlaceholderOrNull(vm) != null)
    }

    @Test
    fun `should show trailing placeholder instead of creating a block if the last block is a code snippet block`() = runTest {

        // SETUP

        val snippet = StubCodeSnippet(children = listOf())
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = Block(
            id = root,
            children = listOf(header.id) + listOf(snippet.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val document = listOf(page, header, title, snippet)

        stubInterceptEvents()
        stubOpenDocument(document)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(trailingPlaceholderOrNull(vm) != null)
    }

    @Test
    fun `should never create blocks or duplicate the placeholder on repeated outside clicks`() = runTest {

        // SETUP

        val block = StubParagraph(text = MockDataFactory.randomString())

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        repeat(3) {
            vm.onOutsideClicked()
            advanceUntilIdle()
        }

        verifyNoInteractions(createBlock)

        assertEquals(
            expected = 1,
            actual = vm.views.count { it.id == VIRTUAL_TRAILING_BLOCK_ID },
            message = "Repeated taps must never produce more than one placeholder"
        )
    }

    @Test
    fun `should create exactly one block carrying the first input when typing into the placeholder`() = runTest {

        // SETUP

        val block = StubParagraph(text = MockDataFactory.randomString())

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        val placeholder = trailingPlaceholderOrNull(vm)!!

        vm.onTextBlockTextChanged(placeholder.copy(text = "h"))

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = "",
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P,
                            text = "h"
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should swap the placeholder for the created block preserving text and focus`() = runTest {

        // SETUP

        val block = StubParagraph(text = MockDataFactory.randomString())

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        val created = StubParagraph(text = "h")

        stubInterceptEvents()
        stubOpenDocument(doc)

        createBlock.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                Pair(
                    created.id,
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.AddBlock(
                                context = root,
                                blocks = listOf(created)
                            ),
                            Event.Command.UpdateStructure(
                                context = root,
                                id = root,
                                children = listOf(header.id, block.id, created.id)
                            )
                        )
                    )
                )
            )
        }

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        val placeholder = trailingPlaceholderOrNull(vm)!!

        vm.onTextBlockTextChanged(placeholder.copy(text = "h"))

        advanceUntilIdle()

        // The placeholder is gone, replaced by the created block carrying the input.
        assertTrue(vm.views.none { it.id == VIRTUAL_TRAILING_BLOCK_ID })

        val last = vm.views.last() as BlockView.Text.Paragraph
        assertEquals(expected = created.id, actual = last.id)
        assertEquals(expected = "h", actual = last.text)
        assertTrue(last.isFocused, "Focus must move to the created block")

        // The block was created already carrying the text — no follow-up set-text needed.
        verifyNoInteractions(updateText)
    }

    @Test
    fun `should show placeholder below a foreign empty trailing paragraph instead of focus-reusing it`() = runTest {

        // SETUP

        val foreign = StubParagraph(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, foreign.id)
        )

        val doc = listOf(page, header, title, foreign)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        // The foreign empty block is not deleted and not reused.
        verifyNoInteractions(unlinkBlocks)

        val placeholder = trailingPlaceholderOrNull(vm)
        assertTrue(placeholder != null && placeholder.isFocused)

        val foreignView = vm.views.first { it.id == foreign.id } as BlockView.Text.Paragraph
        assertTrue(
            !foreignView.isFocused,
            "A foreign empty trailing block must not be focus-reused"
        )
    }

    @Test
    fun `should focus-reuse an empty trailing paragraph created by this session`() = runTest {

        // SETUP

        val own = StubParagraph(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, own.id)
        )

        val doc = listOf(page, header, title, own)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        orchestrator.memory.sessionCreatedBlockIds.add(own.id)

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(trailingPlaceholderOrNull(vm) == null)

        val ownView = vm.views.first { it.id == own.id } as BlockView.Text.Paragraph
        assertTrue(ownView.isFocused, "An empty trailing block created by this session is focus-reused")
    }

    @Test
    fun `should remove the placeholder when focus is lost with nothing typed`() = runTest {

        // SETUP

        val block = StubParagraph(text = MockDataFactory.randomString())

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        assertTrue(trailingPlaceholderOrNull(vm) != null)

        vm.onBlockFocusChanged(id = VIRTUAL_TRAILING_BLOCK_ID, hasFocus = false)

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
        assertTrue(
            vm.views.none { it.id == VIRTUAL_TRAILING_BLOCK_ID },
            "Nothing typed: the placeholder simply goes away, nothing is created"
        )
    }

    @Test
    fun `should create a block on enter pressed inside the empty placeholder`() = runTest {

        // SETUP

        val block = StubParagraph(text = MockDataFactory.randomString())

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        vm.onEnterKeyClicked(
            target = VIRTUAL_TRAILING_BLOCK_ID,
            text = "",
            marks = emptyList(),
            range = 0..0
        )

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = "",
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P,
                            text = ""
                        )
                    )
                )
            )
        }
    }
}
