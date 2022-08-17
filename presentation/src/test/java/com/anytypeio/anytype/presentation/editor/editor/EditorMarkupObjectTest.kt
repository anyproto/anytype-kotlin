package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType.Companion.PAGE_URL
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class EditorMarkupObjectTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should add object markup to text`() {
        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "Start Foobar End",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()

        val vm = buildViewModel()
        val linkObject = MockDataFactory.randomString()

        //TESTING
        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = block.id,
                selection = IntRange(6, 12)
            )
            proceedToAddObjectToTextAsLink(id = linkObject)
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
                            id = block.id,
                            cursor = null,
                            isSelected = false,
                            isFocused = true,
                            marks = listOf(
                                Markup.Mark.Object(
                                    from = 6,
                                    to = 12,
                                    param = linkObject,
                                    isArchived = false
                                )
                            ),
                            indent = 0,
                            text = "Start Foobar End",
                            mode = BlockView.Mode.EDIT,
                            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                                listOf(
                                    BlockView.Decoration(
                                        background = block.parseThemeBackgroundColor()
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
    }

    @Test
    fun `should add object markup to text end remove all clicked marks in range`() {
        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val linkObject = MockDataFactory.randomUuid()
        val linkWeb = MockDataFactory.randomString()
        val linkMention = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "Start Link Object Mention End",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 5
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 6,
                            endInclusive = 10
                        ),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = linkWeb
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 6,
                            endInclusive = 10
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 11,
                            endInclusive = 17
                        ),
                        type = Block.Content.Text.Mark.Type.OBJECT,
                        param = linkObject
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 18,
                            endInclusive = 25
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = linkMention
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 18,
                            endInclusive = 25
                        ),
                        type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                        param = "#000"
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 26,
                            endInclusive = 29
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()

        val vm = buildViewModel()
        val linkNew = MockDataFactory.randomString()

        //TESTING
        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = block.id,
                selection = IntRange(6, 25)
            )
            proceedToAddObjectToTextAsLink(id = linkNew)
        }

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    cursor = null,
                    isSelected = false,
                    isFocused = true,
                    marks = listOf(
                        Markup.Mark.Italic(
                            from = 26,
                            to = 29
                        ),
                        Markup.Mark.Bold(
                            from = 0,
                            to = 5
                        ),
                        Markup.Mark.Bold(
                            from = 6,
                            to = 10
                        ),
                        Markup.Mark.TextColor(
                            from = 18,
                            to = 25,
                            color = "#000"
                        ),
                        Markup.Mark.Object(
                            from = 6,
                            to = 25,
                            param = linkNew,
                            isArchived = false
                        ),

                        ),
                    indent = 0,
                    text = "Start Link Object Mention End",
                    mode = BlockView.Mode.EDIT,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )

        val actual = vm.state.value
        assertEquals(expected, actual)
    }

    @Test
    fun `should create object and add markup to text end remove all clicked marks in range`() {
        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val linkObject = MockDataFactory.randomUuid()
        val linkWeb = MockDataFactory.randomString()
        val linkMention = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "Start Link Object Mention End",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 5
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 6,
                            endInclusive = 10
                        ),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = linkWeb
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 6,
                            endInclusive = 10
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 11,
                            endInclusive = 17
                        ),
                        type = Block.Content.Text.Mark.Type.OBJECT,
                        param = linkObject
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 18,
                            endInclusive = 25
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = linkMention
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 18,
                            endInclusive = 25
                        ),
                        type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                        param = "#000"
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 26,
                            endInclusive = 29
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()
        val vm = buildViewModel()
        val linkNew = MockDataFactory.randomString()

        //TESTING
        val newObjectType = PAGE_URL
        val newObjectId = MockDataFactory.randomString()
        val newObjectName = MockDataFactory.randomString()
        stubCreateNewDocument(
            name = newObjectName,
            type = newObjectType,
            id = newObjectId
        )
        stubGetDefaultObjectType(type = newObjectType)
        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSelectionChanged(
                id = block.id,
                selection = IntRange(6, 25)
            )
            proceedToCreateObjectAndAddToTextAsLink(name = newObjectName)
        }

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    cursor = null,
                    isSelected = false,
                    isFocused = true,
                    marks = listOf(
                        Markup.Mark.Italic(
                            from = 26,
                            to = 29
                        ),
                        Markup.Mark.Bold(
                            from = 0,
                            to = 5
                        ),
                        Markup.Mark.Bold(
                            from = 6,
                            to = 10
                        ),
                        Markup.Mark.TextColor(
                            from = 18,
                            to = 25,
                            color = "#000"
                        ),
                        Markup.Mark.Object(
                            from = 6,
                            to = 25,
                            param = newObjectId,
                            isArchived = false
                        ),
                    ),
                    indent = 0,
                    text = "Start Link Object Mention End",
                    mode = BlockView.Mode.EDIT,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = block.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )

        val actual = vm.state.value
        assertEquals(expected, actual)
    }
}