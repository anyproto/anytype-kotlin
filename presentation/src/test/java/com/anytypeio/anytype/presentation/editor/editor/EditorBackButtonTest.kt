package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorBackButtonTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should proceed to the exit when the system back button is pressed`() {

        val doc = MockTypicalDocumentFactory.page(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)
        assertFalse(stateBefore.styleTextToolbar.isVisible)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        val stateBackPressed = vm.controlPanelViewState.value

        assertTrue(stateBackPressed?.styleTextToolbar?.isVisible == false)

        verifyBlocking(closePage, times(1)) { async(root) }
    }
}