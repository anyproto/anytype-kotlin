package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory.page
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory.profile
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorMenuTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not dispatch command for opening profile menu if document is not started`() {

        // SETUP

        val doc = profile(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertNoValue()
    }

    @Test
    fun `should dispatch command for opening page menu if document is started`() {

        // SETUP

        val doc = page(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertValue { value ->
            value.peekContent() == Command.OpenDocumentMenu(
                isArchived = false,
                isFavorite = false,
                isLocked = false,
                fromName = ""
            )
        }
    }

    @Test
    fun `should dispatch command for opening page menu with restrictions if document is started`() {

        // SETUP

        val doc = page(root)

        val objectRestrictions = listOf(ObjectRestriction.LAYOUT_CHANGE, ObjectRestriction.DELETE)

        stubInterceptEvents()
        stubOpenDocument(document = doc, objectRestrictions = objectRestrictions)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertValue { value ->
            value.peekContent() == Command.OpenDocumentMenu(
                isArchived = false,
                isFavorite = false,
                isLocked = false,
                fromName = ""
            )
        }
    }

    @Test
    fun `should not dispatch command for opening page menu if document is not started`() {

        // SETUP

        val doc = page(root)

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        val observer = vm.commands.test()

        observer.assertNoValue()

        vm.onDocumentMenuClicked()

        observer.assertNoValue()
    }
}