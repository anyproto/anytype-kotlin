package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorSlashWidgetObjectTypeTest : EditorPresentationTestSetup() {

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
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should invoke createObject UseCase on clicked on object type item`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")

        stubInterceptEvents()
        stubUpdateText()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubCreateObject(root, a.id)
        stubOpenDocument(doc)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
            onSlashTextWatcherEvent(
                SlashEvent.Filter(
                    filter = "/",
                    viewType = Types.HOLDER_NUMBERED
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(
            SlashItem.ObjectType(
                url = type2.url,
                name = type2.name,
                emoji = type2.emoji,
                layout = type2.layout,
                description = type2.description
            )
        )

        val params = CreateObject.Params(
            context = root,
            target = a.id,
            position = Position.BOTTOM,
            type = type2.url,
            layout = type2.layout
        )

        verifyBlocking(createObject, times(1)) { invoke(params) }
    }
}