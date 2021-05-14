package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.UpdateBlocksMark
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.page.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class EditorSlashWidgetMarksTest : EditorPresentationTestSetup() {

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
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Testing clicks on Slash Style Markups
     * 1.1 Click on BOLD -> should hide slash widget
     * 1.2 Click on BOLD -> save in Store.Focus, targetId and selection
     * 1.3 Click on BOLD -> invoke updateBlocksMark useCase with proper mark
     * 2.1 Click on ITALIC -> should hide slash widget
     * 2.2 Click on ITALIC -> save in Store.Focus, targetId and selection
     * 2.3 Click on ITALIC -> invoke updateBlocksMark useCase with proper mark
     * 3.1 Click on BREAKTHROUGH -> should hide slash widget
     * 3.2 Click on BREAKTHROUGH -> save in Store.Focus, targetId and selection
     * 3.3 Click on BREAKTHROUGH -> invoke updateBlocksMark useCase with proper mark
     * 4.1 Click on CODE -> should hide slash widget
     * 4.2 Click on CODE -> save in Store.Focus, targetId and selection
     * 4.3 Click on CODE -> invoke updateBlocksMark useCase with proper mark
     * 5. Click on STYLE MARKUP -> save Focus.empty when selection is null
     */

    //region {1}
    @Test
    fun `should hide slash widget event when clicked on BOLD `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
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

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on BOLD`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

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
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on BOLD`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(3, 10),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    )
                ),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block2.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block2.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    //region {2}
    @Test
    fun `should hide slash widget event when clicked on ITALIC `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
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

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on ITALIC`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(11, 11)

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
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on ITALIC`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block2.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block2.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.ITALIC
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    //region {3}
    @Test
    fun `should hide slash widget event when clicked on BREAKTHROUGH `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
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

        vm.onSlashItemClicked(SlashItem.Style.Markup.Breakthrough)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on BREAKTHROUGH`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(5, 5)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Breakthrough)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block2.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on BREAKTHROUGH`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

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
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Breakthrough)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    //region {4}
    @Test
    fun `should hide slash widget event when clicked on CODE `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
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

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on CODE`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(15, 15)

        vm.apply {
            onSelectionChanged(
                id = block3.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block3.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block3.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on CODE`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

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
                    slashStart = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.KEYBOARD
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    @Test
    fun `should save empty focus when selection is null`() {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
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
            onBlockFocusChanged(
                id = block.id,
                hasFocus = false
            )
        }

        // TESTING

        runBlocking {
            orchestrator.stores.textSelection.update(
                Editor.TextSelection(
                    id = block.id,
                    selection = null
                )
            )

            vm.onSlashItemClicked(SlashItem.Style.Markup.Breakthrough)

            val focus = orchestrator.stores.focus.current()

            assertEquals(Editor.Focus.empty(), focus)
        }
    }
}