package com.anytypeio.anytype.presentation.editor

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingMode
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals


class ControlPanelStateReducerTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    @Mock
    lateinit var gateway: Gateway
    lateinit var urlBuilder: UrlBuilder

    private val reducer = ControlPanelMachine.Reducer()

    val paragraph = Block(
        id = MockDataFactory.randomUuid(),
        children = emptyList(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.P,
            marks = emptyList()
        ),
        fields = Block.Fields.empty()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
    }

    @Test
    fun `state should have visible block toolbar and focus from the event`() {

        val given = ControlPanelState.init()

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions when cursor before mentions start and widget is visible`() {

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(9, 9),
            target = paragraph
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should not hide mentions when cursor after mention start`() {

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(11, 11),
            target = paragraph
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions after focus changed`() {

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions after selection chaged`() {

        val id = MockDataFactory.randomUuid()

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false,
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(8, 8),
            target = Block(
                id = id,
                children = emptyList(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true,
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should have only focus changed`() {

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy()

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should return to initial state when focus is cleared`() {

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnClearFocusClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = ControlPanelState.init()

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should be zero selected blocks when enter selected state`() {

        val id = MockDataFactory.randomUuid()

        val nonSelected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false,
                count = 2
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val selected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 0
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = nonSelected,
                event = ControlPanelMachine.Event.MultiSelect.OnEnter()
            )
        }

        assertEquals(
            expected = selected,
            actual = result
        )
    }

    @Test
    fun `should be three selected blocks when click on block`() {

        val id = MockDataFactory.randomUuid()

        val selectedZero = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 0
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 3
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = selectedZero,
                event = ControlPanelMachine.Event.MultiSelect.OnBlockClick(count = 3)
            )
        }

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update style toolbar state with italic true after selection changed`() {

        val selectionFirst = IntRange(0, 2)
        val selectionSecond = IntRange(4, 6)

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = paragraph.id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark.Bold(0, 3),
                        Markup.Mark.Italic(4, 7)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = true,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = selectionSecond,
                    target = paragraph
                )
            )
        }

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = paragraph.id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark.Bold(0, 3),
                        Markup.Mark.Italic(4, 7)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = true,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update style toolbar state without markup after selection changed`() {

        val id = MockDataFactory.randomUuid()
        val selectionFirst = IntRange(0, 2)
        val selectionSecond = IntRange(4, 6)

        val given = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(Markup.Mark.Bold(0, 3))
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = true,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = selectionSecond,
                    target = paragraph
                )
            )
        }

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(Markup.Mark.Bold(0, 3))
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should show style toolbar in markup mode after main toolbar style clicked`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.P,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..3,
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                color = "yellow",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty(),
            backgroundColor = "red"
        )

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        val given = ControlPanelState(
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
            slashWidget = ControlPanelState.Toolbar.SlashWidget(
                isVisible = false
            )
        )

        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = paragraph
                )
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = block,
                    selection = IntRange(0, 3),
                    focused = true,
                    urlBuilder = urlBuilder,
                    details = Block.Details(mapOf())
                )
            )
        }

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(Markup.Mark.Bold(0, 3))
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = true,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should show style toolbar in block mode after main toolbar style clicked`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.P,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..3,
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                color = "yellow",
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty(),
            backgroundColor = "red"
        )

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(1, 1),
                    target = paragraph
                )
            )
        }

        val given = ControlPanelState(
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

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = block,
                    selection = IntRange(1,1),
                    focused = true,
                    urlBuilder = urlBuilder,
                    details = Block.Details(mapOf())
                )
            )
        }

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.CENTER,
                    marks = listOf(
                        Markup.Mark.Bold(0, 3)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.CENTER,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.BLOCK
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update style toolbar when selection is changed and not zero or empty`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.P,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..3,
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                color = "yellow",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty(),
            backgroundColor = "red"
        )

        //Focus block
        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        //Select Foo
        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = paragraph
                )
            )
        }

        val given = ControlPanelState(
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


        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = paragraph
                )
            )
        }

        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnEditorContextMenuStyleClicked(
                    selection = IntRange(0, 3),
                    target = block,
                    urlBuilder = urlBuilder,
                    details = Block.Details(mapOf())
                )
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = ControlPanelState(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(
                        isVisible = false
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    styleTextToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = true,
                        target = ControlPanelState.Toolbar.Styling.Target(
                            id = id,
                            text = "Foo Bar",
                            color = "yellow",
                            background = "red",
                            alignment = Alignment.CENTER,
                            marks = listOf(
                                Markup.Mark.Bold(0, 3)
                            )
                        ),
                        config = StyleConfig(
                            visibleTypes = listOf(
                                StylingType.STYLE,
                                StylingType.TEXT_COLOR,
                                StylingType.BACKGROUND
                            ),
                            enabledAlignment = listOf(
                                Alignment.START,
                                Alignment.CENTER,
                                Alignment.END
                            ),
                            enabledMarkup = listOf(
                                Markup.Type.BOLD,
                                Markup.Type.ITALIC,
                                Markup.Type.STRIKETHROUGH,
                                Markup.Type.KEYBOARD,
                                Markup.Type.LINK
                            )
                        ),
                        props = ControlPanelState.Toolbar.Styling.Props(
                            isBold = true,
                            isItalic = false,
                            isStrikethrough = false,
                            isCode = false,
                            isLinked = false,
                            alignment = Alignment.CENTER,
                            color = "yellow",
                            background = "red"
                        ),
                        mode = StylingMode.MARKUP
                    ),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = false
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                    slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
                ),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 7),
                    target = paragraph
                )
            )
        }

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.CENTER,
                    marks = listOf(
                        Markup.Mark.Bold(0, 3)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.CENTER,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should reset style toolbar only when selection range is 0 on block that in focus`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.P,
                marks = listOf(),
                color = "yellow",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty(),
            backgroundColor = "red"
        )

        //Focus block
        val afterFocusStateResult = runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        val afterFocusStateExpected = ControlPanelState(
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

        assertEquals(
            expected = afterFocusStateExpected,
            actual = afterFocusStateResult
        )

        //Select last position
        val afterSelectionStateResult = runBlocking {
            reducer.reduce(
                state = afterFocusStateResult,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    target = paragraph,
                    selection = IntRange(6, 6)
                )
            )
        }

        val afterSelectionStateExpected = ControlPanelState(
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

        assertEquals(
            expected = afterSelectionStateExpected,
            actual = afterSelectionStateResult
        )

        //Click on [T] style button
        val afterShowStyleToolbarStateResult = runBlocking {
            reducer.reduce(
                state = afterSelectionStateExpected,
                event = ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = block,
                    selection = IntRange(6,6),
                    focused = true,
                    urlBuilder = urlBuilder,
                    details = Block.Details(mapOf())
                )
            )
        }

        val afterShowStyleToolbarStateExpected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    id = id,
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf()
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.BLOCK
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )

        assertEquals(
            expected = afterShowStyleToolbarStateExpected,
            actual = afterShowStyleToolbarStateResult
        )

        //New selection block
        val result = runBlocking {
            reducer.reduce(
                state = afterShowStyleToolbarStateResult,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    target = paragraph,
                    selection = IntRange(1, 1)
                )
            )
        }

        val expected = ControlPanelState(
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
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `state should have visible navigation toolbar on init`() {

        val given = ControlPanelState.init()

        val expected = given.copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = given
        )
    }

    @Test
    fun `state should have visible navigation toolbar on clear focus`() {

        val stateOnInit = ControlPanelState.init()

        val eventFocus = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val stateAfterFocus = runBlocking {
            reducer.reduce(
                state = stateOnInit,
                event = eventFocus
            )
        }

        val eventClearFocus = ControlPanelMachine.Event.OnClearFocusClicked

        val stateAfterClearFocus = runBlocking {
            reducer.reduce(
                state = stateAfterFocus,
                event = eventClearFocus
            )
        }

        val stateAfterClearFocusExpected = ControlPanelState.init().copy(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = true
            )
        )

        assertEquals(
            expected = stateAfterClearFocusExpected,
            actual = stateAfterClearFocus
        )
    }

    @Test
    fun `state should have visible navigation toolbar on exit multi select `() {

        val stateOnInit = ControlPanelState.init()

        val eventFocus = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val stateAfterFocus = runBlocking {
            reducer.reduce(
                state = stateOnInit,
                event = eventFocus
            )
        }

        val stateAfterEnterMultiSelect = runBlocking {
            reducer.reduce(
                state = stateAfterFocus,
                event = ControlPanelMachine.Event.MultiSelect.OnEnter()
            )
        }

        val stateAfterExitMultiSelect = runBlocking {
            reducer.reduce(
                state = stateAfterEnterMultiSelect,
                event = ControlPanelMachine.Event.MultiSelect.OnExit
            )
        }

        val stateAfterExitMultiSelectExpected = ControlPanelState.init().copy(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = true
            )
        )

        assertEquals(
            expected = stateAfterExitMultiSelectExpected,
            actual = stateAfterExitMultiSelect
        )
    }
}