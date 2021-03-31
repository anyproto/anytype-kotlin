package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.TurnIntoDocument
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorTurnIntoTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should start turning text block into page in edit mode`() {

        // SETUP

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(parent.id)
        )

        val document = listOf(page, parent, child)

        val params = TurnIntoDocument.Params(context = root, targets = listOf(child.id))

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubTurnIntoDocument(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(
                id = child.id,
                hasFocus = true
            )
            onTurnIntoBlockClicked(
                target = child.id,
                uiBlock = UiBlock.PAGE
            )
        }

        verifyBlocking(turnIntoDocument, times(1)) { invoke(params) }
    }

    @Test
    fun `should start turning into page in multi-select mode`() {

        // SETUP

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(parent.id)
        )

        val document = listOf(page, parent, child)

        val params = TurnIntoDocument.Params(context = root, targets = listOf(child.id))

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))
        stubTurnIntoDocument(params)

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = child.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
            coroutineTestRule.advanceTime(PageViewModel.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            onTextInputClicked(child.id)
            onTurnIntoMultiSelectBlockClicked(UiBlock.PAGE)
        }

        verifyBlocking(turnIntoDocument, times(1)) { invoke(params) }
    }

    @Test
    fun `should open turn-into panel with restrictions for text block in edit mode`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id)
        )

        val document = listOf(page, a, b)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = a.id, hasFocus = true)
            onClickListener(
                ListenerType.LongClick(
                    target = b.id,
                    dimensions = BlockDimensions(
                        MockDataFactory.randomInt(),
                        MockDataFactory.randomInt(),
                        MockDataFactory.randomInt(),
                        MockDataFactory.randomInt(),
                        MockDataFactory.randomInt(),
                        MockDataFactory.randomInt()
                    )
                )
            )
            onActionMenuItemClicked(
                id = b.id,
                action = ActionItemType.TurnInto
            )
        }

        val result = vm.commands.value?.peekContent()

        val expectedExcludedTypes = listOf(
            UiBlock.LINE_DIVIDER.name,
            UiBlock.THREE_DOTS.name,
            UiBlock.BOOKMARK.name,
            UiBlock.LINK_TO_OBJECT.name,
            UiBlock.FILE.name,
            UiBlock.VIDEO.name,
            UiBlock.IMAGE.name,
            UiBlock.RELATION.name
        )

        check(result is Command.OpenTurnIntoPanel) { "Wrong command" }

        assertEquals(
            expected = emptyList(),
            actual = result.excludedCategories
        )

        assertEquals(
            expected = b.id,
            actual = result.target
        )

        assertTrue {
            result.excludedTypes.size == expectedExcludedTypes.size
        }

        assertTrue {
            result.excludedTypes.containsAll(expectedExcludedTypes)
        }
    }

    @Test
    fun `should open turn-into panel with restrictions for one text block and one file block in multi-select mode`() {

        // SETUP

        val child1 = Block(
            id = "CHILD1",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val child2 = Block(
            id = "CHILD2",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.DONE
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(parent.id)
        )

        val document = listOf(page, parent, child1, child2)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = child1.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
            coroutineTestRule.advanceTime(PageViewModel.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            onTextInputClicked(child1.id)
            onClickListener(
                ListenerType.File.View(child2.id)
            )
            onMultiSelectTurnIntoButtonClicked()
        }

        val result = vm.commands.value?.peekContent()

        val expectedExcludedTypes = listOf(
            UiBlock.LINE_DIVIDER.name,
            UiBlock.THREE_DOTS.name,
            UiBlock.BOOKMARK.name,
            UiBlock.LINK_TO_OBJECT.name,
            UiBlock.FILE.name,
            UiBlock.VIDEO.name,
            UiBlock.IMAGE.name,
            UiBlock.RELATION.name
        )

        check(result is Command.OpenMultiSelectTurnIntoPanel) { "Wrong command" }

        assertEquals(
            expected = listOf(UiBlock.RELATION.name),
            actual = result.excludedCategories
        )

        assertTrue {
            result.excludedTypes.size == expectedExcludedTypes.size
        }

        assertTrue {
            result.excludedTypes.containsAll(expectedExcludedTypes)
        }
    }

    @Test
    fun `should not open turn-into panel with restrictions for file in multi-select mode`() {

        // SETUP

        val child1 = Block(
            id = "CHILD1",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val child2 = Block(
            id = "CHILD2",
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.DONE
            )
        )

        val parent = Block(
            id = "PARENT",
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(parent.id)
        )

        val document = listOf(page, parent, child1, child2)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        // TESTING

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = child1.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
            coroutineTestRule.advanceTime(PageViewModel.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            onClickListener(ListenerType.Picture.View(child2.id))
            onMultiSelectTurnIntoButtonClicked()
        }

        // Verify there was no command for opening turn-into panel.

        vm.commands.test().assertNoValue()
    }
}