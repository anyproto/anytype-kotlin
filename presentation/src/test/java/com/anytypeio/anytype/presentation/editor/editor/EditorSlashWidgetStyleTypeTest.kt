package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.TurnIntoStyle
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorSlashWidgetStyleTypeTest : EditorPresentationTestSetup() {

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
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should save selection and focus when clicked on BULLETED`() {
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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubTurnIntoStyle()
        stubUpdateText()
        stubSearchObjects()
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
                    slashStart = 7
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Type.Bulleted)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should hide slash widget event when clicked on BULLETED `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubTurnIntoStyle()
        stubUpdateText()
        stubUpdateText()
        stubSearchObjects()
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

        vm.onSlashItemClicked(SlashItem.Style.Type.Bulleted)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should invoke turnIntoStyle when clicked on BOLD`() {
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
                style = Block.Content.Text.Style.CHECKBOX
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubTurnIntoStyle()
        stubUpdateText()
        stubSearchObjects()
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

        vm.onSlashItemClicked(SlashItem.Style.Type.Bulleted)

        val params = TurnIntoStyle.Params(
            context = root,
            targets = listOf(block2.id),
            style = Block.Content.Text.Style.BULLET
        )

        verifyBlocking(turnIntoStyle, times(1)) { invoke(params) }
    }
}