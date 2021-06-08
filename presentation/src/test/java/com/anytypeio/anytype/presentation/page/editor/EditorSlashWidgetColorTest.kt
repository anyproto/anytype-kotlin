package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.UpdateBackgroundColor
import com.anytypeio.anytype.domain.block.interactor.UpdateTextColor
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.PageViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.page.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.page.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorSlashWidgetColorTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //region {TEXT COLOR}
    @Test
    fun `should selected red color when block text color is red`() {

        val code = ThemeColor.RED.title

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                color = code,
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.ColorWithBack,
            SlashItem.Color.Text(ThemeColor.DEFAULT.title, false),
            SlashItem.Color.Text(ThemeColor.GREY.title, false),
            SlashItem.Color.Text(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Text(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Text(ThemeColor.RED.title, true),
            SlashItem.Color.Text(ThemeColor.PINK.title, false),
            SlashItem.Color.Text(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Text(ThemeColor.BLUE.title, false),
            SlashItem.Color.Text(ThemeColor.ICE.title, false),
            SlashItem.Color.Text(ThemeColor.TEAL.title, false),
            SlashItem.Color.Text(ThemeColor.GREEN.title, false)
        )

        assertEquals(
            expected = expected,
            actual = (command as SlashWidgetState.UpdateItems).colorItems
        )
    }

    @Test
    fun `should selected default color when block text color is null`() {

        val code: String? = null

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                color = code,
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.ColorWithBack,
            SlashItem.Color.Text(ThemeColor.DEFAULT.title, true),
            SlashItem.Color.Text(ThemeColor.GREY.title, false),
            SlashItem.Color.Text(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Text(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Text(ThemeColor.RED.title, false),
            SlashItem.Color.Text(ThemeColor.PINK.title, false),
            SlashItem.Color.Text(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Text(ThemeColor.BLUE.title, false),
            SlashItem.Color.Text(ThemeColor.ICE.title, false),
            SlashItem.Color.Text(ThemeColor.TEAL.title, false),
            SlashItem.Color.Text(ThemeColor.GREEN.title, false)
        )

        assertEquals(
            expected = expected,
            actual = command.colorItems
        )
    }

    @Test
    fun `should selected default color when block text color is default`() {

        val code: String = ThemeColor.DEFAULT.title

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                color = code,
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.ColorWithBack,
            SlashItem.Color.Text(ThemeColor.DEFAULT.title, true),
            SlashItem.Color.Text(ThemeColor.GREY.title, false),
            SlashItem.Color.Text(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Text(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Text(ThemeColor.RED.title, false),
            SlashItem.Color.Text(ThemeColor.PINK.title, false),
            SlashItem.Color.Text(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Text(ThemeColor.BLUE.title, false),
            SlashItem.Color.Text(ThemeColor.ICE.title, false),
            SlashItem.Color.Text(ThemeColor.TEAL.title, false),
            SlashItem.Color.Text(ThemeColor.GREEN.title, false)
        )

        assertEquals(
            expected = expected,
            actual = command.colorItems
        )
    }

    @Test
    fun `should save selection and focus when text color picked`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(4, 4)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 4
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)
        vm.onSlashItemClicked(SlashItem.Color.Text(code = "red", isSelected = false))

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should hide slash widget when text color picked`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)
        vm.onSlashItemClicked(SlashItem.Color.Text(code = "red", isSelected = false))

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send updateTextColor UseCase when text color picked`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateTextColor()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        val code = ThemeColor.ICE.title

        vm.onSlashItemClicked(SlashItem.Main.Color)
        vm.onSlashItemClicked(SlashItem.Color.Text(code = code, isSelected = false))

        val params = UpdateTextColor.Params(
            context = root,
            targets = listOf(block.id),
            color = code
        )

        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
        verifyBlocking(updateTextColor, times(1)) { invoke(params) }
    }
    //endregion

    //region {BACKGROUND COLOR}
    @Test
    fun `should selected green color when block background color is green`() {

        val code = ThemeColor.GREEN.title

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                backgroundColor = code,
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.BackgroundWithBack,
            SlashItem.Color.Background(ThemeColor.DEFAULT.title, false),
            SlashItem.Color.Background(ThemeColor.GREY.title, false),
            SlashItem.Color.Background(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Background(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Background(ThemeColor.RED.title, false),
            SlashItem.Color.Background(ThemeColor.PINK.title, false),
            SlashItem.Color.Background(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Background(ThemeColor.BLUE.title, false),
            SlashItem.Color.Background(ThemeColor.ICE.title, false),
            SlashItem.Color.Background(ThemeColor.TEAL.title, false),
            SlashItem.Color.Background(ThemeColor.GREEN.title, true)
        )

        assertEquals(
            expected = expected,
            actual = command.backgroundItems
        )
    }

    @Test
    fun `should selected default color when block background color is null`() {

        val code: String? = null

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                backgroundColor = code,
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.BackgroundWithBack,
            SlashItem.Color.Background(ThemeColor.DEFAULT.title, true),
            SlashItem.Color.Background(ThemeColor.GREY.title, false),
            SlashItem.Color.Background(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Background(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Background(ThemeColor.RED.title, false),
            SlashItem.Color.Background(ThemeColor.PINK.title, false),
            SlashItem.Color.Background(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Background(ThemeColor.BLUE.title, false),
            SlashItem.Color.Background(ThemeColor.ICE.title, false),
            SlashItem.Color.Background(ThemeColor.TEAL.title, false),
            SlashItem.Color.Background(ThemeColor.GREEN.title, false)
        )

        assertEquals(
            expected = expected,
            actual = command.backgroundItems
        )
    }

    @Test
    fun `should selected default color when block background color is default`() {

        val code: String = ThemeColor.DEFAULT.title

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                color = code,
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.BackgroundWithBack,
            SlashItem.Color.Background(ThemeColor.DEFAULT.title, true),
            SlashItem.Color.Background(ThemeColor.GREY.title, false),
            SlashItem.Color.Background(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Background(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Background(ThemeColor.RED.title, false),
            SlashItem.Color.Background(ThemeColor.PINK.title, false),
            SlashItem.Color.Background(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Background(ThemeColor.BLUE.title, false),
            SlashItem.Color.Background(ThemeColor.ICE.title, false),
            SlashItem.Color.Background(ThemeColor.TEAL.title, false),
            SlashItem.Color.Background(ThemeColor.GREEN.title, false)
        )

        assertEquals(
            expected = expected,
            actual = command.backgroundItems
        )
    }

    @Test
    fun `should save selection and focus when background color picked`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(4, 4)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )

            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 4
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)
        vm.onSlashItemClicked(SlashItem.Color.Background(code = "red", isSelected = false))

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should hide slash widget when background color picked`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)
        vm.onSlashItemClicked(SlashItem.Color.Background(code = "red", isSelected = false))

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send updateBackgroundColor UseCase when background color picked`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        val code = ThemeColor.PURPLE.title

        vm.onSlashItemClicked(SlashItem.Main.Background)
        vm.onSlashItemClicked(SlashItem.Color.Background(code = code, isSelected = false))

        val params = UpdateBackgroundColor.Params(
            context = root,
            targets = listOf(block.id),
            color = code
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
        verifyBlocking(updateBackgroundColor, times(1)) { invoke(params) }
    }
    //endregion
}