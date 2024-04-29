package com.anytypeio.anytype.presentation.editor.editor

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.block.interactor.UpdateBackgroundColor
import com.anytypeio.anytype.domain.block.interactor.UpdateTextColor
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorSlashWidgetColorTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stubSpaceManager()
        stubGetNetworkMode()
        stubFileLimitEvents()
        stubUpdateText()
        stubAnalyticSpaceHelperDelegate()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun after() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //region {TEXT COLOR}
    @Test
    fun `should selected red color when block text color is red`() = runTest {

        val code = ThemeColor.RED.code

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
            content = Block.Content.Smart,
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

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.ColorWithBack,
            SlashItem.Color.Text(ThemeColor.DEFAULT, false),
            SlashItem.Color.Text(ThemeColor.GREY, false),
            SlashItem.Color.Text(ThemeColor.YELLOW, false),
            SlashItem.Color.Text(ThemeColor.ORANGE, false),
            SlashItem.Color.Text(ThemeColor.RED, true),
            SlashItem.Color.Text(ThemeColor.PINK, false),
            SlashItem.Color.Text(ThemeColor.PURPLE, false),
            SlashItem.Color.Text(ThemeColor.BLUE, false),
            SlashItem.Color.Text(ThemeColor.ICE, false),
            SlashItem.Color.Text(ThemeColor.TEAL, false),
            SlashItem.Color.Text(ThemeColor.LIME, false)
        )

        assertEquals(
            expected = expected,
            actual = (command as SlashWidgetState.UpdateItems).colorItems
        )
    }

    @Test
    fun `should selected default color when block text color is null`() = runTest {

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
            content = Block.Content.Smart,
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

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.ColorWithBack,
            SlashItem.Color.Text(ThemeColor.DEFAULT, true),
            SlashItem.Color.Text(ThemeColor.GREY, false),
            SlashItem.Color.Text(ThemeColor.YELLOW, false),
            SlashItem.Color.Text(ThemeColor.ORANGE, false),
            SlashItem.Color.Text(ThemeColor.RED, false),
            SlashItem.Color.Text(ThemeColor.PINK, false),
            SlashItem.Color.Text(ThemeColor.PURPLE, false),
            SlashItem.Color.Text(ThemeColor.BLUE, false),
            SlashItem.Color.Text(ThemeColor.ICE, false),
            SlashItem.Color.Text(ThemeColor.TEAL, false),
            SlashItem.Color.Text(ThemeColor.LIME, false)
        )

        assertEquals(
            expected = expected,
            actual = command.colorItems
        )
    }

    @Test
    fun `should selected default color when block text color is default`() = runTest {

        val code: String = ThemeColor.DEFAULT.code

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
            content = Block.Content.Smart,
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

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.ColorWithBack,
            SlashItem.Color.Text(ThemeColor.DEFAULT, true),
            SlashItem.Color.Text(ThemeColor.GREY, false),
            SlashItem.Color.Text(ThemeColor.YELLOW, false),
            SlashItem.Color.Text(ThemeColor.ORANGE, false),
            SlashItem.Color.Text(ThemeColor.RED, false),
            SlashItem.Color.Text(ThemeColor.PINK, false),
            SlashItem.Color.Text(ThemeColor.PURPLE, false),
            SlashItem.Color.Text(ThemeColor.BLUE, false),
            SlashItem.Color.Text(ThemeColor.ICE, false),
            SlashItem.Color.Text(ThemeColor.TEAL, false),
            SlashItem.Color.Text(ThemeColor.LIME, false)
        )

        assertEquals(
            expected = expected,
            actual = command.colorItems
        )
    }

    @Test
    fun `should save selection and focus when text color picked`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(4, 4)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 4
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)
        advanceUntilIdle()
        vm.onSlashItemClicked(SlashItem.Color.Text(themeColor = ThemeColor.RED, isSelected = false))
        advanceUntilIdle()

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.requireTarget())
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should hide slash widget when text color picked`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateTextColor()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Color)
        advanceUntilIdle()
        vm.onSlashItemClicked(SlashItem.Color.Text(themeColor = ThemeColor.RED, isSelected = false))
        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send updateTextColor UseCase when text color picked`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateTextColor()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        val code = ThemeColor.ICE.code

        vm.onSlashItemClicked(SlashItem.Main.Color)
        advanceUntilIdle()

        vm.onSlashItemClicked(SlashItem.Color.Text(themeColor = ThemeColor.ICE, isSelected = false))
        advanceUntilIdle()

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
    fun `should selected green color when block background color is green`() = runTest {

        val code = ThemeColor.LIME.code

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            backgroundColor = code,
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
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
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.BackgroundWithBack,
            SlashItem.Color.Background(ThemeColor.DEFAULT, false),
            SlashItem.Color.Background(ThemeColor.GREY, false),
            SlashItem.Color.Background(ThemeColor.YELLOW, false),
            SlashItem.Color.Background(ThemeColor.ORANGE, false),
            SlashItem.Color.Background(ThemeColor.RED, false),
            SlashItem.Color.Background(ThemeColor.PINK, false),
            SlashItem.Color.Background(ThemeColor.PURPLE, false),
            SlashItem.Color.Background(ThemeColor.BLUE, false),
            SlashItem.Color.Background(ThemeColor.ICE, false),
            SlashItem.Color.Background(ThemeColor.TEAL, false),
            SlashItem.Color.Background(ThemeColor.LIME, true)
        )

        assertEquals(
            expected = expected,
            actual = command.backgroundItems
        )
    }

    @Test
    fun `should selected default color when block background color is null`() = runTest {

        val code: String? = null

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            backgroundColor = code,
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
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
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.BackgroundWithBack,
            SlashItem.Color.Background(ThemeColor.DEFAULT, true),
            SlashItem.Color.Background(ThemeColor.GREY, false),
            SlashItem.Color.Background(ThemeColor.YELLOW, false),
            SlashItem.Color.Background(ThemeColor.ORANGE, false),
            SlashItem.Color.Background(ThemeColor.RED, false),
            SlashItem.Color.Background(ThemeColor.PINK, false),
            SlashItem.Color.Background(ThemeColor.PURPLE, false),
            SlashItem.Color.Background(ThemeColor.BLUE, false),
            SlashItem.Color.Background(ThemeColor.ICE, false),
            SlashItem.Color.Background(ThemeColor.TEAL, false),
            SlashItem.Color.Background(ThemeColor.LIME, false)
        )

        assertEquals(
            expected = expected,
            actual = command.backgroundItems
        )
    }

    @Test
    fun `should selected default color when block background color is default`() = runTest {

        val code: String = ThemeColor.DEFAULT.code

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
            content = Block.Content.Smart,
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
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = listOf(
            SlashItem.Subheader.BackgroundWithBack,
            SlashItem.Color.Background(ThemeColor.DEFAULT, true),
            SlashItem.Color.Background(ThemeColor.GREY, false),
            SlashItem.Color.Background(ThemeColor.YELLOW, false),
            SlashItem.Color.Background(ThemeColor.ORANGE, false),
            SlashItem.Color.Background(ThemeColor.RED, false),
            SlashItem.Color.Background(ThemeColor.PINK, false),
            SlashItem.Color.Background(ThemeColor.PURPLE, false),
            SlashItem.Color.Background(ThemeColor.BLUE, false),
            SlashItem.Color.Background(ThemeColor.ICE, false),
            SlashItem.Color.Background(ThemeColor.TEAL, false),
            SlashItem.Color.Background(ThemeColor.LIME, false)
        )

        assertEquals(
            expected = expected,
            actual = command.backgroundItems
        )
    }

    @Test
    fun `should save selection and focus when background color picked`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(4, 4)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )

            advanceUntilIdle()

            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )

            advanceUntilIdle()

            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 4
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)

        advanceUntilIdle()

        vm.onSlashItemClicked(SlashItem.Color.Background(themeColor = ThemeColor.RED, isSelected = false))

        advanceUntilIdle()

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.requireTarget())
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should hide slash widget when background color picked`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Main.Background)
        advanceUntilIdle()
        vm.onSlashItemClicked(SlashItem.Color.Background(themeColor = ThemeColor.RED, isSelected = false))
        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send updateBackgroundColor UseCase when background color picked`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBackground()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        val code = ThemeColor.PURPLE.code

        vm.onSlashItemClicked(SlashItem.Main.Background)
        advanceUntilIdle()
        vm.onSlashItemClicked(SlashItem.Color.Background(themeColor = ThemeColor.PURPLE, isSelected = false))
        advanceUntilIdle()

        val params = UpdateBackgroundColor.Params(
            context = root,
            targets = listOf(block.id),
            color = code
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
        verifyBlocking(updateBackgroundColor, times(1)) { invoke(params) }
    }
    //endregion
}