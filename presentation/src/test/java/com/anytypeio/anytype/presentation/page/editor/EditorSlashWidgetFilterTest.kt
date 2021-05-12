package com.anytypeio.anytype.presentation.page.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.page.editor.slash.SlashCommand
import com.anytypeio.anytype.presentation.page.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.mockito.MockitoAnnotations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EditorSlashWidgetFilterTest : EditorPresentationTestSetup()  {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test for SlashEvent.Filter
     * 1. Filter = "" -> SlashCommand.FilterItems.empty() +
     * 3. Filter = "b" -> SlashCommand.FilterItems.empty() +
     * 2. Filter = "/" -> SlashCommand.FilterItems with Main Items, other is empty +
     * 3. Filter = "/b"
     * 4. Filter = "/B"
     * 5. Filter = "/bo"
     * 6. Filter = "/bold
     * 7. Filter = "Align l"
     * 8. Filter = "Align r
     * 9. Filter = "bzxc" - close slash widget on three zero searches
     */

    //region {1}
    @Test
    fun `should return empty Update command when filter is empty`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
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

        val event = SlashEvent.Filter(filter = "", viewType = HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expected = SlashCommand.UpdateItems.empty()
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {2}
    @Test
    fun `should return empty Update command when filter is not started from slash`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
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

        val event = SlashEvent.Filter(filter = "b", viewType = HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expected = SlashCommand.UpdateItems.empty()
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {3}
    @Test
    fun `should return Update command with main items when filter only slash`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
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

        val event = SlashEvent.Filter(filter = "/", viewType = HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expected = SlashCommand.UpdateItems(
            mainItems = listOf(
                SlashItem.Main.Style,
                SlashItem.Main.Media,
                SlashItem.Main.Objects,
                SlashItem.Main.Relations,
                SlashItem.Main.Other,
                SlashItem.Main.Actions,
                SlashItem.Main.Alignment,
                SlashItem.Main.Color,
                SlashItem.Main.Background,
            ),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = command)
    }
    //endregion
}