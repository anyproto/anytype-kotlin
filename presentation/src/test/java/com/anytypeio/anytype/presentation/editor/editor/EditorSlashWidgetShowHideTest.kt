package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorSlashWidgetShowHideTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

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
        stubSpaceManager()
        stubGetNetworkMode()
        stubFileLimitEvents()
        stubAnalyticSpaceHelperDelegate()
    }

    @Test
    fun `should show slash widget when slash event start happened`() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
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

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertTrue(state.slashWidget.isVisible)
    }

    @Test
    fun `should hide other toolbars when slash event start happened`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
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

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.mainToolbar.isVisible)
        assertFalse(state.navigationToolbar.isVisible)
        assertFalse(state.mentionToolbar.isVisible)
        assertFalse(state.multiSelect.isVisible)
        assertFalse(state.searchToolbar.isVisible)
        assertFalse(state.styleTextToolbar.isVisible)
    }
}