package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
import com.anytypeio.anytype.domain.page.CreateNewDocument
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.mention.Mention
import com.anytypeio.anytype.presentation.page.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class EditorMentionTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var documentEmojiIconProvider: DocumentEmojiIconProvider

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should update text with cursor position`() {

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

        val mentionTrigger = "@a"
        val from = 11
        val givenText = "page about $mentionTrigger music"
        val mentionText = "Avant-Garde Jazz"
        val mentionHash = "ryew78yfhiuwehudc"

        val a = Block(
            id = "dfhkshfjkhsdjhfjkhsjkd",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 9
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 14,
                            endInclusive = 18
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        getListPages.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(GetListPages.Response(emptyList()))
        }

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = a.id,
                selection = IntRange(12, 12)
            )
            onMentionEvent(
                MentionEvent.MentionSuggestStart(
                    cursorCoordinate = 500,
                    mentionStart = from
                )
            )
            onCreateMentionInText(
                mention = Mention(
                    id = mentionHash,
                    emoji = null,
                    image = null,
                    title = mentionText
                ),
                mentionTrigger = mentionTrigger
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Document(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Text.Paragraph(
                            id = a.id,
                            cursor = 28,
                            isSelected = false,
                            isFocused = true,
                            marks = listOf(
                                Markup.Mark(
                                    from = 0,
                                    to = 3,
                                    type = Markup.Type.BOLD
                                ),
                                Markup.Mark(
                                    from = 5,
                                    to = 9,
                                    type = Markup.Type.ITALIC
                                ),
                                Markup.Mark(
                                    from = 29,
                                    to = 33,
                                    type = Markup.Type.STRIKETHROUGH
                                ),
                                Markup.Mark(
                                    from = from,
                                    to = from + mentionText.length,
                                    type = Markup.Type.MENTION,
                                    param = mentionHash,
                                    extras = mapOf(
                                        "image" to null,
                                        "emoji" to null
                                    )
                                )
                            ),
                            backgroundColor = null,
                            color = null,
                            indent = 0,
                            text = "page about Avant-Garde Jazz  music",
                            mode = BlockView.Mode.EDIT
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should create new page with proper name and add new mention with page id`() {

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

        val mentionTrigger = "@Jazz"
        val from = 11
        val givenText = "page about $mentionTrigger music"
        val newPageName = "Jazz"
        val newPageId = MockDataFactory.randomUuid()
        val emoji = "smile:emoji"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        getListPages.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(GetListPages.Response(emptyList()))
        }

        Mockito.`when`(documentEmojiIconProvider.random()).thenReturn(emoji)

        createNewDocument.stub {
            onBlocking {
                invoke(
                    CreateNewDocument.Params(
                        name = newPageName
                    )
                )
            } doReturn Either.Right(
                CreateNewDocument.Result(
                    name = newPageName,
                    id = newPageId,
                    emoji = emoji
                )
            )
        }

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = a.id,
                selection = IntRange(12, 12)
            )
            onMentionEvent(
                MentionEvent.MentionSuggestStart(
                    cursorCoordinate = 500,
                    mentionStart = from
                )
            )
            onAddMentionNewPageClicked(
                name = mentionTrigger
            )
        }

        verifyBlocking(createNewDocument, times(1)) {
            invoke(
                CreateNewDocument.Params(
                    name = newPageName
                )
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Document(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Text.Paragraph(
                            id = a.id,
                            cursor = 16,
                            isSelected = false,
                            isFocused = true,
                            marks = listOf(
                                Markup.Mark(
                                    from = 0,
                                    to = 3,
                                    type = Markup.Type.BOLD
                                ),
                                Markup.Mark(
                                    from = from,
                                    to = from + newPageName.length,
                                    type = Markup.Type.MENTION,
                                    param = newPageId,
                                    extras = mapOf(
                                        "image" to null,
                                        "emoji" to null
                                    )
                                )
                            ),
                            backgroundColor = null,
                            color = null,
                            indent = 0,
                            text = "page about Jazz  music",
                            mode = BlockView.Mode.EDIT
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should create new page with untitled name and add new mention with page id`() {

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

        val mentionTrigger = "@"
        val from = 11
        val givenText = "page about $mentionTrigger music"
        val newPageName = ""
        val newPageId = MockDataFactory.randomUuid()
        val emoji = "smile:emoji"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        getListPages.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(GetListPages.Response(emptyList()))
        }

        Mockito.`when`(documentEmojiIconProvider.random()).thenReturn(emoji)

        createNewDocument.stub {
            onBlocking {
                invoke(
                    CreateNewDocument.Params(
                        name = newPageName
                    )
                )
            } doReturn Either.Right(
                CreateNewDocument.Result(
                    name = newPageName,
                    id = newPageId,
                    emoji = emoji
                )
            )
        }

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = a.id,
                selection = IntRange(12, 12)
            )
            onMentionEvent(
                MentionEvent.MentionSuggestStart(
                    cursorCoordinate = 500,
                    mentionStart = from
                )
            )
            onAddMentionNewPageClicked(
                name = mentionTrigger
            )
        }

        verifyBlocking(createNewDocument, times(1)) {
            invoke(
                CreateNewDocument.Params(
                    name = newPageName
                )
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Document(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Text.Paragraph(
                            id = a.id,
                            cursor = from + PageViewModel.MENTION_TITLE_EMPTY.length + 1,
                            isSelected = false,
                            isFocused = true,
                            marks = listOf(
                                Markup.Mark(
                                    from = 0,
                                    to = 3,
                                    type = Markup.Type.BOLD
                                ),
                                Markup.Mark(
                                    from = from,
                                    to = from + PageViewModel.MENTION_TITLE_EMPTY.length,
                                    type = Markup.Type.MENTION,
                                    param = newPageId,
                                    extras = mapOf(
                                        "image" to null,
                                        "emoji" to null
                                    )
                                )
                            ),
                            backgroundColor = null,
                            color = null,
                            indent = 0,
                            text = "page about Untitled  music",
                            mode = BlockView.Mode.EDIT
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should close mention menu after back pressed event`() {
        val mentionTrigger = "@Pag"
        val from = 11
        val givenText = "page about $mentionTrigger music"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        getListPages.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(GetListPages.Response(emptyList()))
        }

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = a.id,
                selection = IntRange(12, 12)
            )
            onMentionEvent(
                MentionEvent.MentionSuggestStart(
                    cursorCoordinate = 500,
                    mentionStart = from
                )
            )
            onMentionEvent(
                MentionEvent.MentionSuggestText(
                    text = "Pag"
                )
            )
        }

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = false
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = true
                ),
                stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                    isVisible = true,
                    cursorCoordinate = 500,
                    mentionFilter = "Pag",
                    mentionFrom = from,
                    updateList = false
                )
            )
        )

        vm.apply {
            onBackPressedCallback()
        }

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = false
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = true
                ),
                stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
            )
        )
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}