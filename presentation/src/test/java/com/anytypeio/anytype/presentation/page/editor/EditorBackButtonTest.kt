package com.anytypeio.anytype.presentation.page.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_ui.widgets.ActionItemType
import com.anytypeio.anytype.domain.page.ClosePage
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EditorBackButtonTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should close style toolbar when the system back button is pressed`() {

        val doc = MockTypicalDocumentFactory.page(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)
        assertFalse(stateBefore.stylingToolbar.isVisible)

        vm.onActionMenuItemClicked(doc[3].id, ActionItemType.Style)

        val stateStyleToolbarShow = vm.controlPanelViewState.value

        assertTrue(stateStyleToolbarShow?.stylingToolbar?.isVisible == true)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        val stateBackPressed = vm.controlPanelViewState.value

        assertTrue(stateBackPressed?.stylingToolbar?.isVisible == false)
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
        assertFalse(stateBefore.stylingToolbar.isVisible)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        val stateBackPressed = vm.controlPanelViewState.value

        assertTrue(stateBackPressed?.stylingToolbar?.isVisible == false)

        val params = ClosePage.Params(id = root)
        verifyBlocking(closePage, times(1)) { invoke(params) }
    }
}