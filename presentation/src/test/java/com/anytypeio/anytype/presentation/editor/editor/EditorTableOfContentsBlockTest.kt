package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
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

class EditorTableOfContentsBlockTest : EditorPresentationTestSetup() {

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

        val blockText1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText4 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText5 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText6 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockHeaderOne1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H1
            ),
            children = emptyList()
        )

        val blockHeaderOne2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H1
            ),
            children = emptyList()
        )

        val blockHeaderTwo1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderTwo2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderThree = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H3
            ),
            children = emptyList()
        )

        val blockToC = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.TableOfContents,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(
                header.id,
                blockToC.id,
                blockText1.id,
                blockHeaderOne1.id,
                blockText2.id,
                blockHeaderTwo1.id,
                blockText3.id,
                blockHeaderOne2.id,
                blockText4.id,
                blockHeaderTwo2.id,
                blockText5.id,
                blockHeaderThree.id,
                blockText6.id
            )
        )

        val doc = listOf(
            page, header, title,
            blockToC,
            blockText1,
            blockHeaderOne1,
            blockText2,
            blockHeaderTwo1,
            blockText3,
            blockHeaderOne2,
            blockText4,
            blockHeaderTwo2,
            blockText5,
            blockHeaderThree,
            blockText6
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()

        val vm = buildViewModel()

        //TESTING
        vm.apply { onStart(root) }

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.TableOfContents(
                    id = blockToC.id,
                    background = blockToC.parseThemeBackgroundColor(),
                    isSelected = false,
                    items = listOf(
                        BlockView.TableOfContentsItem(
                            id = blockHeaderOne1.id,
                            name = blockHeaderOne1.content.asText().text,
                            depth = 0
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo1.id,
                            name = blockHeaderTwo1.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderOne2.id,
                            name = blockHeaderOne2.content.asText().text,
                            depth = 0
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo2.id,
                            name = blockHeaderTwo2.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree.id,
                            name = blockHeaderThree.content.asText().text,
                            depth = 2
                        )
                    )
                ),
                BlockView.Text.Paragraph(
                    id = blockText1.id,
                    indent = 0,
                    text = blockText1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText1.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.One(
                    id = blockHeaderOne1.id,
                    indent = 0,
                    text = blockHeaderOne1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderOne1.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText2.id,
                    indent = 0,
                    text = blockText2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText2.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo1.id,
                    indent = 0,
                    text = blockHeaderTwo1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo1.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText3.id,
                    indent = 0,
                    text = blockText3.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText3.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.One(
                    id = blockHeaderOne2.id,
                    indent = 0,
                    text = blockHeaderOne2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderOne2.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText4.id,
                    indent = 0,
                    text = blockText4.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText4.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo2.id,
                    indent = 0,
                    text = blockHeaderTwo2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo2.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText5.id,
                    indent = 0,
                    text = blockText5.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText5.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree.id,
                    indent = 0,
                    text = blockHeaderThree.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText6.id,
                    indent = 0,
                    text = blockText6.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText6.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )
        val result = vm.state.test().value()

        assertEquals(expected, result)
    }

    @Test
    fun `should update table of contents block after header text update`() {
        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val blockText1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockText3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val blockHeaderOne = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H1
            ),
            children = emptyList()
        )

        val blockHeaderTwo = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderThree = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H3
            ),
            children = emptyList()
        )

        val blockToC = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.TableOfContents,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(
                header.id,
                blockToC.id,
                blockText1.id,
                blockHeaderOne.id,
                blockText2.id,
                blockHeaderTwo.id,
                blockText3.id,
                blockHeaderThree.id
            )
        )

        val doc = listOf(
            page, header, title,
            blockToC,
            blockText1,
            blockHeaderOne,
            blockText2,
            blockHeaderTwo,
            blockText3,
            blockHeaderThree
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()

        val vm = buildViewModel()

        //TESTING
        vm.apply {
            onStart(root)
        }

        val expectedBefore = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.TableOfContents(
                    id = blockToC.id,
                    background = blockToC.parseThemeBackgroundColor(),
                    isSelected = false,
                    items = listOf(
                        BlockView.TableOfContentsItem(
                            id = blockHeaderOne.id,
                            name = blockHeaderOne.content.asText().text,
                            depth = 0
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo.id,
                            name = blockHeaderTwo.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree.id,
                            name = blockHeaderThree.content.asText().text,
                            depth = 2
                        )
                    )
                ),
                BlockView.Text.Paragraph(
                    id = blockText1.id,
                    indent = 0,
                    text = blockText1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText1.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.One(
                    id = blockHeaderOne.id,
                    indent = 0,
                    text = blockHeaderOne.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderOne.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText2.id,
                    indent = 0,
                    text = blockText2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText2.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo.id,
                    indent = 0,
                    text = blockHeaderTwo.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText3.id,
                    indent = 0,
                    text = blockText3.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText3.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree.id,
                    indent = 0,
                    text = blockHeaderThree.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )

        val resultBefore = vm.state.test().value()

        assertEquals(expectedBefore, resultBefore)

        val newText = MockDataFactory.randomString()
        vm.apply {
            onTextBlockTextChanged(
                view = BlockView.Text.Header.Two(
                    id = blockHeaderTwo.id,
                    text = newText
                )
            )
        }

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.TableOfContents(
                    id = blockToC.id,
                    background = blockToC.parseThemeBackgroundColor(),
                    isSelected = false,
                    items = listOf(
                        BlockView.TableOfContentsItem(
                            id = blockHeaderOne.id,
                            name = blockHeaderOne.content.asText().text,
                            depth = 0
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo.id,
                            name = newText,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree.id,
                            name = blockHeaderThree.content.asText().text,
                            depth = 2
                        )
                    )
                ),
                BlockView.Text.Paragraph(
                    id = blockText1.id,
                    indent = 0,
                    text = blockText1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText1.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.One(
                    id = blockHeaderOne.id,
                    indent = 0,
                    text = blockHeaderOne.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderOne.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Paragraph(
                    id = blockText2.id,
                    indent = 0,
                    text = blockText2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText2.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo.id,
                    indent = 0,
                    text = newText
                ),
                BlockView.Text.Paragraph(
                    id = blockText3.id,
                    indent = 0,
                    text = blockText3.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockText3.parseThemeBackgroundColor()
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree.id,
                    indent = 0,
                    text = blockHeaderThree.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )
        val result = vm.state.test().value()

        assertEquals(expected, result)
    }

    @Test
    fun `should have proper depth for headers`() {
        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val blockHeaderOne1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H1
            ),
            children = emptyList()
        )

        val blockHeaderTwo1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderTwo2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderThree = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H3
            ),
            children = emptyList()
        )

        val blockHeaderThree2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H3
            ),
            children = emptyList()
        )

        val blockToC = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.TableOfContents,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(
                header.id,
                blockToC.id,
                blockHeaderOne1.id,
                blockHeaderTwo1.id,
                blockHeaderThree.id,
                blockHeaderTwo2.id,
                blockHeaderThree2.id
            )
        )

        val doc = listOf(
            page, header, title,
            blockToC,
            blockHeaderOne1,
            blockHeaderTwo1,
            blockHeaderThree,
            blockHeaderTwo2,
            blockHeaderThree2
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()

        val vm = buildViewModel()

        //TESTING
        vm.apply { onStart(root) }

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.TableOfContents(
                    id = blockToC.id,
                    background = blockToC.parseThemeBackgroundColor(),
                    isSelected = false,
                    items = listOf(
                        BlockView.TableOfContentsItem(
                            id = blockHeaderOne1.id,
                            name = blockHeaderOne1.content.asText().text,
                            depth = 0
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo1.id,
                            name = blockHeaderTwo1.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree.id,
                            name = blockHeaderThree.content.asText().text,
                            depth = 2
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo2.id,
                            name = blockHeaderTwo2.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree2.id,
                            name = blockHeaderThree2.content.asText().text,
                            depth = 2
                        )
                    )
                ),
                BlockView.Text.Header.One(
                    id = blockHeaderOne1.id,
                    indent = 0,
                    text = blockHeaderOne1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderOne1.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo1.id,
                    indent = 0,
                    text = blockHeaderTwo1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo1.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree.id,
                    indent = 0,
                    text = blockHeaderThree.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo2.id,
                    indent = 0,
                    text = blockHeaderTwo2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo2.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree2.id,
                    indent = 0,
                    text = blockHeaderThree2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree2.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )
        val result = vm.state.test().value()

        assertEquals(expected, result)
    }

    @Test
    fun `should have all items in table of contents block when divs are present`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val blockHeaderOne1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H1
            ),
            children = emptyList()
        )

        val blockHeaderTwo1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderTwo2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            ),
            children = emptyList()
        )

        val blockHeaderThree = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H3
            ),
            children = emptyList()
        )

        val blockHeaderThree2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.H3
            ),
            children = emptyList()
        )

        val blockToC = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.TableOfContents,
            children = emptyList()
        )

        val div1 = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            children = listOf(blockToC.id, blockHeaderOne1.id, blockHeaderTwo1.id),
            fields = Block.Fields(emptyMap()),
        )

        val div2 = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            children = listOf(blockHeaderThree.id, blockHeaderTwo2.id, blockHeaderThree2.id),
            fields = Block.Fields(emptyMap()),
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(
                header.id,
                div1.id,
                div2.id
            )
        )

        val doc = listOf(
            page, header, title,
            div1,
            div2,
            blockToC,
            blockHeaderOne1,
            blockHeaderTwo1,
            blockHeaderThree,
            blockHeaderTwo2,
            blockHeaderThree2
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = Block.Details(),
            relations = listOf()
        )
        stubUpdateText()

        val vm = buildViewModel()

        //TESTING
        vm.apply { onStart(root) }

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    isFocused = false,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.EDIT
                ),
                BlockView.TableOfContents(
                    id = blockToC.id,
                    background = blockToC.parseThemeBackgroundColor(),
                    isSelected = false,
                    items = listOf(
                        BlockView.TableOfContentsItem(
                            id = blockHeaderOne1.id,
                            name = blockHeaderOne1.content.asText().text,
                            depth = 0
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo1.id,
                            name = blockHeaderTwo1.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree.id,
                            name = blockHeaderThree.content.asText().text,
                            depth = 2
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderTwo2.id,
                            name = blockHeaderTwo2.content.asText().text,
                            depth = 1
                        ),
                        BlockView.TableOfContentsItem(
                            id = blockHeaderThree2.id,
                            name = blockHeaderThree2.content.asText().text,
                            depth = 2
                        )
                    )
                ),
                BlockView.Text.Header.One(
                    id = blockHeaderOne1.id,
                    indent = 0,
                    text = blockHeaderOne1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderOne1.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo1.id,
                    indent = 0,
                    text = blockHeaderTwo1.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo1.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree.id,
                    indent = 0,
                    text = blockHeaderThree.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Two(
                    id = blockHeaderTwo2.id,
                    indent = 0,
                    text = blockHeaderTwo2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderTwo2.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H2
                            )
                        )
                    } else {
                        emptyList()
                    }
                ),
                BlockView.Text.Header.Three(
                    id = blockHeaderThree2.id,
                    indent = 0,
                    text = blockHeaderThree2.content.asText().text,
                    decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                        listOf(
                            BlockView.Decoration(
                                background = blockHeaderThree2.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H3
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        )
        val result = vm.state.test().value()

        assertEquals(expected, result)
    }
}