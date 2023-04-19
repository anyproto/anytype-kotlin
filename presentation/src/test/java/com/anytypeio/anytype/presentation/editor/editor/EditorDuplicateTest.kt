package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorDuplicateTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val title = StubTitle()

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should duplicate blocks in order of the rendered document, and not in order of selection`() {

        // SETUP

        val a = MockBlockFactory.paragraph()
        val b = MockBlockFactory.paragraph()
        val c = MockBlockFactory.paragraph()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, a.id, b.id, c.id)
        )

        val document = listOf(smart, header, title, a, b, c)

        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Simulating long tap on "c" block, in order to enter multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = b.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue {
            vm.actions.value.contains(ActionItemType.Duplicate)
        }

        // Selecting "a" block and "b" block.

        vm.onTextInputClicked(target = c.id)
        vm.onTextInputClicked(target = a.id)

        vm.onMultiSelectAction(ActionItemType.Duplicate)

        verifyBlocking(duplicateBlock, times(1)) {
            invoke(
                DuplicateBlock.Params(
                    context = root,
                    blocks = listOf(a.id, b.id, c.id),
                    target = c.id
                )
            )
        }
    }

    @Test
    fun `should preserve selection when duplicating currently selected divider`() {

        // SETUP

        val a = MockBlockFactory.paragraph()
        val b = MockBlockFactory.divider(style = Block.Content.Divider.Style.LINE)
        val c = MockBlockFactory.paragraph()

        val copy = b.copy(id = MockDataFactory.randomUuid())

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, a.id, b.id, c.id)
        )

        val document = listOf(smart, header, title, a, b, c)

        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubDuplicateBlock(
            new = copy.id,
            events = listOf(
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(copy)
                ),
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, a.id, b.id, copy.id, c.id)
                )
            )
        )


        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Simulating long tap on "c" block, in order to enter multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = b.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue {
            vm.actions.value.contains(ActionItemType.Duplicate)
        }

        // Selecting "a" block and "b" block.

        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ
                    ),
                    BlockView.Text.Paragraph(
                        id = a.id,
                        text = a.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = a.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.DividerLine(
                        id = b.id,
                        isSelected = true,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = b.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.Text.Paragraph(
                        id = c.id,
                        text = c.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = c.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    )
                )
            )
        )

        vm.onMultiSelectAction(ActionItemType.Duplicate)

        verifyBlocking(duplicateBlock, times(1)) {
            invoke(
                DuplicateBlock.Params(
                    context = root,
                    blocks = listOf(b.id),
                    target = b.id
                )
            )
        }

        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ
                    ),
                    BlockView.Text.Paragraph(
                        id = a.id,
                        text = a.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = a.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.DividerLine(
                        id = b.id,
                        isSelected = true,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = b.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.DividerLine(
                        id = copy.id,
                        isSelected = false,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = copy.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.Text.Paragraph(
                        id = c.id,
                        text = c.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = c.parseThemeBackgroundColor()
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

    @Test
    fun `nested block duplicate`() {
        // SETUP

        val b = MockBlockFactory.paragraph()
        val a = MockBlockFactory.paragraph(children = listOf(b.id))
        val c = MockBlockFactory.paragraph()

        val copy = a.copy(id = MockDataFactory.randomUuid())

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, a.id, c.id)
        )

        val document = listOf(smart, header, title, a, b, c)

        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Simulating long tap on "a" block, in order to enter in multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = a.id,
                dimensions = BlockDimensions()
            )
        )

        assertTrue {
            vm.actions.value.contains(ActionItemType.Duplicate)
        }

        // "a" and its child "b" should be selected:

        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ
                    ),
                    BlockView.Text.Paragraph(
                        id = a.id,
                        text = a.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = a.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.Text.Paragraph(
                        id = b.id,
                        text = b.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true,
                        indent = 1,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = b.parseThemeBackgroundColor()
                                ),
                                BlockView.Decoration(
                                    background = c.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.Text.Paragraph(
                        id = c.id,
                        text = c.content<Block.Content.Text>().text,
                        mode = BlockView.Mode.READ,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = c.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    )
                )
            )
        )

        vm.onMultiSelectAction(ActionItemType.Duplicate)

        verifyBlocking(duplicateBlock, times(1)) {
            invoke(
                DuplicateBlock.Params(
                    context = root,
                    blocks = listOf(a.id),
                    target = a.id
                )
            )
        }
    }
}