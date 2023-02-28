package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.domain.page.CreateBlockLinkWithObject
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.presentation.objects.getProperName
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
    fun `should invoke CreateBlockLinkWithObject UseCase on clicked on object type item with bottom position when text block is not empty`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubSearchObjects(listOf(type1, type2, type3))
        stubCreateBlockLinkWithObject(root, a.id)
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
                objectTypeView = ObjectTypeView(
                    id = type2.id,
                    name = type2.getProperName(),
                    description = type2.description,
                    emoji = type2.iconEmoji
                )
            )
        )

        val params = CreateBlockLinkWithObject.Params(
            context = root,
            target = a.id,
            position = Position.BOTTOM,
            type = type2.id
        )

        verifyBlocking(createBlockLinkWithObject, times(1)) { execute(params) }
    }

    @Test
    fun `should invoke CreateBlockLinkWithObject UseCase on clicked on object type item with replace position when text block is empty`() {
        // SETUP

        val paragraph = StubParagraph(text = "")
        val smart = StubSmartBlock(children = listOf(paragraph.id))
        val doc = listOf(smart, paragraph)

        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubSearchObjects(listOf(type1, type2, type3))
        stubCreateBlockLinkWithObject(root, paragraph.id)
        stubOpenDocument(doc)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(paragraph.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(1, 0))
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
                objectTypeView = ObjectTypeView(
                    id = type2.id,
                    name = type2.getProperName(),
                    description = type2.description,
                    emoji = type2.iconEmoji
                )
            )
        )

        val params = CreateBlockLinkWithObject.Params(
            context = root,
            target = paragraph.id,
            position = Position.REPLACE,
            type = type2.id
        )

        verifyBlocking(createBlockLinkWithObject, times(1)) { execute(params) }
    }
}