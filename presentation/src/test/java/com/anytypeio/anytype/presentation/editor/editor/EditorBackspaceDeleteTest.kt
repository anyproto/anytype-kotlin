package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.StubBulleted
import com.anytypeio.anytype.core_models.StubCheckbox
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubNumbered
import com.anytypeio.anytype.core_models.StubQuote
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.StubToggle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyZeroInteractions
import kotlin.test.assertEquals

class EditorBackspaceDeleteTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val title = StubTitle()

    val header = StubHeader(children = listOf(title.id))

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should focus parent text block - when its child is deleted`() {

        // SETUP

        val child = Block(
            id = "CHILD",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.values().filter { style ->
                    style != Block.Content.Text.Style.TITLE
                            && style != Block.Content.Text.Style.DESCRIPTION
                            && style != Block.Content.Text.Style.CALLOUT
                            && style != Block.Content.Text.Style.TOGGLE
                            && style != Block.Content.Text.Style.BULLET
                            && style != Block.Content.Text.Style.CHECKBOX
                            && style != Block.Content.Text.Style.NUMBERED
                            && style != Block.Content.Text.Style.QUOTE
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
    fun `should focus previous nested text block - when the next one is deleted`() {

        // SETUP

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
                    style != Block.Content.Text.Style.TITLE
                            && style != Block.Content.Text.Style.DESCRIPTION
                            && style != Block.Content.Text.Style.CALLOUT
                            && style != Block.Content.Text.Style.TOGGLE
                            && style != Block.Content.Text.Style.BULLET
                            && style != Block.Content.Text.Style.CHECKBOX
                            && style != Block.Content.Text.Style.NUMBERED
                            && style != Block.Content.Text.Style.QUOTE
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
    fun `should focus the previous nested textual block - when the next text block is deleted`() {

        // SETUP

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
                    style != Block.Content.Text.Style.TITLE
                            && style != Block.Content.Text.Style.DESCRIPTION
                            && style != Block.Content.Text.Style.CALLOUT
                            && style != Block.Content.Text.Style.TOGGLE
                            && style != Block.Content.Text.Style.BULLET
                            && style != Block.Content.Text.Style.CHECKBOX
                            && style != Block.Content.Text.Style.NUMBERED
                            && style != Block.Content.Text.Style.QUOTE
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
    fun `should delete the previous bookmark - when pressing backspace in an empty text block following this bookmark`() {

        // SETUP

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
    fun `should delete the previous bookmark - when pressing backspace in an non-empty text block following this bookmark`() {

        // SETUP

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

        releasePendingTextUpdates()

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
    fun `should reset style to paragraph - when pressing backspace in empty checkbox block`() {

        // SETUP

        val checkbox = StubCheckbox(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, checkbox.id)
        )

        val document = listOf(page, header, title, checkbox)

        stubOpenDocument(document = document)
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = checkbox.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(id = checkbox.id)
        }

        verifyZeroInteractions(unlinkBlocks)
        verifyZeroInteractions(updateText)
        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(checkbox.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in empty bulleted block`() {

        // SETUP

        val bulleted = StubBulleted(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, bulleted.id)
        )

        val document = listOf(page, header, title, bulleted)

        stubOpenDocument(document = document)
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = bulleted.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(id = bulleted.id)
        }

        verifyZeroInteractions(unlinkBlocks)
        verifyZeroInteractions(updateText)
        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(bulleted.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in empty numbered block`() {

        // SETUP

        val numbered = StubNumbered(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, numbered.id)
        )

        val document = listOf(page, header, title, numbered)

        stubOpenDocument(document = document)
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = numbered.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(id = numbered.id)
        }

        verifyZeroInteractions(unlinkBlocks)
        verifyZeroInteractions(updateText)
        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(numbered.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in empty toggle block`() {

        // SETUP

        val toggle = StubToggle(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, toggle.id)
        )

        val document = listOf(page, header, title, toggle)

        stubOpenDocument(document = document)
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = toggle.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(id = toggle.id)
        }

        verifyZeroInteractions(unlinkBlocks)
        verifyZeroInteractions(updateText)
        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(toggle.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in empty quote block`() {

        // SETUP

        val quote = StubQuote(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, quote.id)
        )

        val document = listOf(page, header, title, quote)

        stubOpenDocument(document = document)
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = quote.id,
                hasFocus = true
            )
            onEmptyBlockBackspaceClicked(id = quote.id)
        }

        verifyZeroInteractions(unlinkBlocks)
        verifyZeroInteractions(updateText)
        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(quote.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in empty callout block`() {
        // TODO Add test when callout block is ready.
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in non-empty checkbox block after deleting all text`() {

        // SETUP

        val checkbox = StubCheckbox(text = "Foo")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, checkbox.id)
        )

        val document = listOf(page, header, title, checkbox)

        stubOpenDocument(document = document)
        stubUpdateText()
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = checkbox.id,
                hasFocus = true
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Checkbox(
                    id = checkbox.id,
                    text = "",
                    isChecked = checkbox.content<TXT>().isChecked ?: false,
                    isFocused = true,
                    marks = emptyList(),
                )
            )
            onEmptyBlockBackspaceClicked(id = checkbox.id)
        }

        verifyZeroInteractions(unlinkBlocks)

        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(checkbox.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }

        // Checking that block text is updated remotely

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    target = checkbox.id,
                    text = "",
                    marks = emptyList()
                )
            )
        }

        // Checking that document is also updated locally

        orchestrator.stores.document.get().first { it.id == checkbox.id }.let { block ->
            assertEquals(
                expected = checkbox.copy(
                    content = checkbox.content<TXT>().copy(
                        text = "",
                        marks = emptyList()
                    )
                ),
                actual = block
            )
        }

        releasePendingTextUpdates()
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in non-empty bulleted block after deleting all text`() {

        // SETUP

        val bulleted = StubBulleted(text = "Foo")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, bulleted.id)
        )

        val document = listOf(page, header, title, bulleted)

        stubOpenDocument(document = document)
        stubUpdateText()
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = bulleted.id,
                hasFocus = true
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Bulleted(
                    id = bulleted.id,
                    text = "",
                    isFocused = true,
                    marks = emptyList(),
                )
            )
            onEmptyBlockBackspaceClicked(id = bulleted.id)
        }

        verifyZeroInteractions(unlinkBlocks)

        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(bulleted.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }

        // Checking that block text is updated remotely

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    target = bulleted.id,
                    text = "",
                    marks = emptyList()
                )
            )
        }

        // Checking that document is also updated locally

        orchestrator.stores.document.get().first { it.id == bulleted.id }.let { block ->
            assertEquals(
                expected = bulleted.copy(
                    content = bulleted.content<TXT>().copy(
                        text = "",
                        marks = emptyList()
                    )
                ),
                actual = block
            )
        }

        releasePendingTextUpdates()
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in non-empty quote block after deleting all text`() {

        // SETUP

        val quote = StubQuote(text = "Foo")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, quote.id)
        )

        val document = listOf(page, header, title, quote)

        stubOpenDocument(document = document)
        stubUpdateText()
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = quote.id,
                hasFocus = true
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Highlight(
                    id = quote.id,
                    text = "",
                    isFocused = true,
                    marks = emptyList(),
                )
            )
            onEmptyBlockBackspaceClicked(id = quote.id)
        }

        verifyZeroInteractions(unlinkBlocks)

        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(quote.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }

        // Checking that block text is updated remotely

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    target = quote.id,
                    text = "",
                    marks = emptyList()
                )
            )
        }

        // Checking that document is also updated locally

        orchestrator.stores.document.get().first { it.id == quote.id }.let { block ->
            assertEquals(
                expected = quote.copy(
                    content = quote.content<TXT>().copy(
                        text = "",
                        marks = emptyList()
                    )
                ),
                actual = block
            )
        }

        releasePendingTextUpdates()
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in non-empty toggle block after deleting all text`() {

        // SETUP

        val toggle = StubToggle(text = "Foo")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, toggle.id)
        )

        val document = listOf(page, header, title, toggle)

        stubOpenDocument(document = document)
        stubUpdateText()
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = toggle.id,
                hasFocus = true
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Toggle(
                    id = toggle.id,
                    text = "",
                    isFocused = true,
                    marks = emptyList(),
                )
            )
            onEmptyBlockBackspaceClicked(id = toggle.id)
        }

        verifyZeroInteractions(unlinkBlocks)

        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(toggle.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }

        // Checking that block text is updated remotely

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    target = toggle.id,
                    text = "",
                    marks = emptyList()
                )
            )
        }

        // Checking that document is also updated locally

        orchestrator.stores.document.get().first { it.id == toggle.id }.let { block ->
            assertEquals(
                expected = toggle.copy(
                    content = toggle.content<TXT>().copy(
                        text = "",
                        marks = emptyList()
                    )
                ),
                actual = block
            )
        }

        releasePendingTextUpdates()
    }

    @Test
    fun `should reset style to paragraph - when pressing backspace in non-empty numbered block after deleting all text`() {

        // SETUP

        val numbered = StubNumbered(text = "Foo")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, numbered.id)
        )

        val document = listOf(page, header, title, numbered)

        stubOpenDocument(document = document)
        stubUpdateText()
        stubUpdateTextStyle()

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = numbered.id,
                hasFocus = true
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Numbered(
                    id = numbered.id,
                    text = "",
                    isFocused = true,
                    marks = emptyList(),
                    number = 1
                )
            )
            onEmptyBlockBackspaceClicked(id = numbered.id)
        }

        verifyZeroInteractions(unlinkBlocks)

        verifyBlocking(updateTextStyle, times(1)) {
            invoke(
                UpdateTextStyle.Params(
                    context = root,
                    targets = listOf(numbered.id),
                    style = Block.Content.Text.Style.P
                )
            )
        }

        // Checking that block text is updated remotely

        verifyBlocking(updateText, times(1)) {
            invoke(
                UpdateText.Params(
                    context = root,
                    target = numbered.id,
                    text = "",
                    marks = emptyList()
                )
            )
        }

        // Checking that document is also updated locally

        orchestrator.stores.document.get().first { it.id == numbered.id }.let { block ->
            assertEquals(
                expected = numbered.copy(
                    content = numbered.content<TXT>().copy(
                        text = "",
                        marks = emptyList()
                    )
                ),
                actual = block
            )
        }

        releasePendingTextUpdates()
    }

    private fun releasePendingTextUpdates() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}