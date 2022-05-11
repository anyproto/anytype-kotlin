package com.anytypeio.anytype.presentation.editor.editor.styling

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorStyleBackgroundToolbarTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    val title = MockBlockFactory.title()
    val header = MockBlockFactory.header(listOf(title.id))
    val paragraph = MockBlockFactory.paragraph()

    @Test
    fun `should start StyleTextToolbar when text block selected`() {

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, paragraph.id)
        )

        val doc = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onClickListener(ListenerType.LongClick(target = paragraph.id))
        vm.onMultiSelectAction(ActionItemType.Style)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)

        val expected = ControlPanelState(
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                state = StyleToolbarState.Text(Block.Content.Text.Style.P)
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 1
            )
        )

        assertEquals(expected, state)
    }

    @Test
    fun `show StyleTextToolbar with proper selected style when block in focus`() {
        val textStyle = TextStyle.P
        val blockId = MockDataFactory.randomUuid()
        val doc = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = blockId,
            style = textStyle
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(root)
        with(vm) {
            onStart(root)
            onBlockFocusChanged(blockId, true)
            onBlockToolbarStyleClicked()
            onBlockFocusChanged(blockId, false)
        }

        val state = vm.controlPanelViewState.value

        assertNotNull(state)

        val expected = ControlPanelState(
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                state = StyleToolbarState.Text(textStyle)
            )
        )

        assertEquals(expected, state)
    }

    @Test
    fun `should start StyleTextToolbar when two text blocks selected`() {

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, block1.id, block2.id)
        )

        val doc = listOf(page, header, title, block1, block2)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onClickListener(ListenerType.LongClick(target = block1.id))
        vm.onTextInputClicked(target = block2.id)
        vm.onMultiSelectAction(ActionItemType.Style)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)

        val expected = ControlPanelState(
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                state = StyleToolbarState.Text(Block.Content.Text.Style.BULLET)
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 2
            )
        )

        assertEquals(expected, state)
    }

    @Test
    fun `should start StyleBackgroundToolbar when code block selected`() {

        val codeStyle = TextStyle.CODE_SNIPPET
        val codeId = MockDataFactory.randomUuid()
        val backgroundColor = ThemeColor.GREEN.title
        val doc = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = codeId,
            style = codeStyle,
            backgroundColor = backgroundColor
        )

        //stub1()
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onClickListener(ListenerType.LongClick(target = codeId))
        vm.onMultiSelectAction(ActionItemType.Style)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)

        val expected = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 1
            ),
            styleBackgroundToolbar = ControlPanelState.Toolbar.Styling.Background(
                isVisible = true,
                state = StyleToolbarState.Background(background = backgroundColor)
            )
        )

        assertEquals(expected, state)
    }

    @Test
    fun `show StyleBackgroundToolbar with proper background and then close when code block in focus`() {
        val textStyle = TextStyle.CODE_SNIPPET
        val blockId = MockDataFactory.randomUuid()
        val backgroundColor = ThemeColor.GREEN.title
        val doc = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = blockId,
            style = textStyle,
            backgroundColor = backgroundColor
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc)

        val vm = buildViewModel()

        vm.onStart(root)
        with(vm) {
            onStart(root)
            onBlockFocusChanged(blockId, true)
            onBlockToolbarStyleClicked()
            onBlockFocusChanged(blockId, false)
        }

        val state = vm.controlPanelViewState.value

        assertNotNull(state)

        val expected = ControlPanelState(
            styleBackgroundToolbar = ControlPanelState.Toolbar.Styling.Background(
                isVisible = true,
                state = StyleToolbarState.Background(background = backgroundColor)
            )
        )

        assertEquals(expected, state)

        with(vm) {
            onCloseBlockStyleBackgroundToolbarClicked()
        }

        val expectedAfterClose = ControlPanelState(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            )
        )

        val stateAfterClose = vm.controlPanelViewState.value

        assertEquals(expectedAfterClose, stateAfterClose)

    }

}