package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
import com.anytypeio.anytype.domain.page.CreateNewDocument
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_TITLE_EMPTY
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class EditorMentionTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var documentEmojiIconProvider: DocumentEmojiIconProvider

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
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
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
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        searchObjects.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(listOf())
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
                id = mentionHash,
                name = mentionText,
                mentionTrigger = mentionTrigger
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
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
                                Markup.Mark.Bold(
                                    from = 0,
                                    to = 3
                                ),
                                Markup.Mark.Italic(
                                    from = 5,
                                    to = 9
                                ),
                                Markup.Mark.Strikethrough(
                                    from = 29,
                                    to = 33
                                ),
                                Markup.Mark.Mention.Loading(
                                    from = from,
                                    to = from + mentionText.length,
                                    param = mentionHash
                                )
                            ),
                            indent = 0,
                            text = "page about Avant-Garde Jazz  music",
                            mode = BlockView.Mode.EDIT,
                            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                                listOf(
                                    BlockView.Decoration(
                                        background = a.parseThemeBackgroundColor()
                                    )
                                )
                            } else {
                                emptyList()
                            }
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should create new page with proper name, default type and add new mention with page id`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubGetDefaultObjectType(type = ObjectTypeIds.NOTE)

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        searchObjects.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(listOf())
        }

        Mockito.`when`(documentEmojiIconProvider.random()).thenReturn(emoji)

        createNewDocument.stub {
            onBlocking {
                invoke(
                    CreateNewDocument.Params(
                        name = newPageName,
                        type = ObjectTypeIds.NOTE
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
                mentionText = mentionTrigger
            )
        }

        verifyBlocking(createNewDocument, times(1)) {
            invoke(
                CreateNewDocument.Params(
                    name = newPageName,
                    type = ObjectTypeIds.NOTE
                )
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
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
                                Markup.Mark.Bold(
                                    from = 0,
                                    to = 3
                                ),
                                Markup.Mark.Mention.Loading(
                                    from = from,
                                    to = from + newPageName.length,
                                    param = newPageId
                                )
                            ),
                            indent = 0,
                            text = "page about Jazz  music",
                            mode = BlockView.Mode.EDIT,
                            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                                listOf(
                                    BlockView.Decoration(
                                        background = a.parseThemeBackgroundColor()
                                    )
                                )
                            } else {
                                emptyList()
                            }
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should create new page with untitled name, default type and add new mention with page id`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubGetDefaultObjectType(type = "_otarticle")

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        searchObjects.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(listOf())
        }

        Mockito.`when`(documentEmojiIconProvider.random()).thenReturn(emoji)

        createNewDocument.stub {
            onBlocking {
                invoke(
                    CreateNewDocument.Params(
                        name = newPageName,
                        type = "_otarticle"
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
                mentionText = mentionTrigger
            )
        }

        verifyBlocking(createNewDocument, times(1)) {
            invoke(
                CreateNewDocument.Params(
                    name = newPageName,
                    type = "_otarticle"
                )
            )
        }

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.EDIT
                        ),
                        BlockView.Text.Paragraph(
                            id = a.id,
                            cursor = from + MENTION_TITLE_EMPTY.length + 1,
                            isSelected = false,
                            isFocused = true,
                            marks = listOf(
                                Markup.Mark.Bold(
                                    from = 0,
                                    to = 3
                                ),
                                Markup.Mark.Mention.Loading(
                                    from = from,
                                    to = from + MENTION_TITLE_EMPTY.length,
                                    param = newPageId
                                )
                            ),
                            indent = 0,
                            text = "page about Untitled  music",
                            mode = BlockView.Mode.EDIT,
                            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                                listOf(
                                    BlockView.Decoration(
                                        background = a.parseThemeBackgroundColor()
                                    )
                                )
                            } else {
                                emptyList()
                            }
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
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        searchObjects.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(listOf())
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
                    isVisible = false
                ),
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                    isVisible = true,
                    cursorCoordinate = 500,
                    mentionFilter = "Pag",
                    mentionFrom = from,
                    updateList = false
                ),
                slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
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
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
            )
        )

        clearPendingCoroutines()
    }

    @ExperimentalTime
    @Test
    fun `test mention filters`() {

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Start end",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()

        val vm = buildViewModel()

        vm.onStart(root)

        //TESTING

        runBlocking {
            vm.mentionSearchQuery.test {
                vm.apply {
                    onBlockFocusChanged(
                        id = a.id,
                        hasFocus = true
                    )
                    onSelectionChanged(
                        id = a.id,
                        selection = IntRange(6, 6)
                    )
                    vm.onMentionEvent(
                        MentionEvent.MentionSuggestStart(
                            cursorCoordinate = 999,
                            mentionStart = 6
                        )
                    )
                    vm.onMentionEvent(
                        MentionEvent.MentionSuggestText(text = "@")
                    )
                    vm.onTextBlockTextChanged(
                        view = BlockView.Text.Paragraph(
                            id = a.id,
                            marks = emptyList(),
                            text = "Start @end"
                        )
                    )
                    onSelectionChanged(
                        id = a.id,
                        selection = IntRange(7, 7)
                    )
                }

                assertEquals(
                    expected = "@", actual = expectMostRecentItem()
                )

                vm.onMentionEvent(
                    MentionEvent.MentionSuggestText(text = "@t")
                )
                vm.onTextBlockTextChanged(
                    view = BlockView.Text.Paragraph(
                        id = a.id,
                        marks = emptyList(),
                        text = "Start @tend"
                    )
                )
                vm.onSelectionChanged(
                    id = a.id,
                    selection = IntRange(8, 8)
                )
                assertEquals(
                    expected = "@t", actual = expectMostRecentItem()
                )

                vm.onMentionEvent(
                    MentionEvent.MentionSuggestText(text = "@to")
                )
                vm.onTextBlockTextChanged(
                    view = BlockView.Text.Paragraph(
                        id = a.id,
                        marks = emptyList(),
                        text = "Start @toend"
                    )
                )
                vm.onSelectionChanged(
                    id = a.id,
                    selection = IntRange(9, 9)
                )
                assertEquals(
                    expected = "@to", actual = expectMostRecentItem()
                )
            }
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should update mention text with details amend event`() {

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

        val mentionTarget = MockDataFactory.randomUuid()
        val givenText = "Start Foo end"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, 5),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(6, 9),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(10, 13),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        val params = InterceptEvents.Params(context = root)

        openPage.stub {
            onBlocking { execute(any()) } doReturn Resultat.success(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                details = Block.Details(),
                                relations = emptyList(),
                                blocks = document,
                                objectRestrictions = emptyList()
                            ),
                            Event.Command.Details.Amend(
                                context = root,
                                target = mentionTarget,
                                details = mapOf(Block.Fields.NAME_KEY to "Foob")
                            )
                        )
                    )
                )
            )
        }
        stubInterceptEvents()
        stubSearchObjects()

        val vm = buildViewModel()

        verifyNoInteractions(interceptEvents)

        vm.onStart(root)

        val actual = vm.state.value
        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.Text.Paragraph(
                    id = a.id,
                    cursor = null,
                    isSelected = false,
                    isFocused = false,
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = 0,
                            to = 5
                        ),
                        Markup.Mark.Mention.Base(
                            from = 6,
                            to = 10,
                            param = mentionTarget,
                            isArchived = false
                        ),
                        Markup.Mark.Strikethrough(
                            from = 11,
                            to = 14
                        )
                    ),
                    indent = 0,
                    text = "Start Foob end",
                    mode = BlockView.Mode.EDIT,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = a.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )

        verify(interceptEvents, times(1)).build(params = params)

        assertEquals(expected = expected, actual = actual)
        clearPendingCoroutines()
    }

    @Test
    fun `should update mention text with details amend event when new text is empty`() {

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

        val mentionTarget = MockDataFactory.randomUuid()
        val givenText = "Start F end"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = givenText,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, 5),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(6, 7),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(8, 11),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        val params = InterceptEvents.Params(context = root)

        openPage.stub {
            onBlocking { execute(any()) } doReturn Resultat.success(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                details = Block.Details(),
                                relations = emptyList(),
                                blocks = document,
                                objectRestrictions = emptyList()
                            ),
                            Event.Command.Details.Amend(
                                context = root,
                                target = mentionTarget,
                                details = mapOf(Block.Fields.NAME_KEY to "")
                            )
                        )
                    )
                )
            )
        }
        stubInterceptEvents()
        stubSearchObjects()

        val vm = buildViewModel()

        verifyNoInteractions(interceptEvents)

        vm.onStart(root)

        val actual = vm.state.value
        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.Text.Paragraph(
                    id = a.id,
                    cursor = null,
                    isSelected = false,
                    isFocused = false,
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = 0,
                            to = 5
                        ),
                        Markup.Mark.Mention.Base(
                            from = 6,
                            to = 14,
                            param = mentionTarget,
                            isArchived = false
                        ),
                        Markup.Mark.Strikethrough(
                            from = 15,
                            to = 18
                        )
                    ),
                    indent = 0,
                    text = "Start Untitled end",
                    mode = BlockView.Mode.EDIT,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = a.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )

        verify(interceptEvents, times(1)).build(params = params)

        assertEquals(expected = expected, actual = actual)
        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}