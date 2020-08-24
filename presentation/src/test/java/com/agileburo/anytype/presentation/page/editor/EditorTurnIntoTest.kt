package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.domain.block.interactor.TurnIntoDocument
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

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
                block = UiBlock.PAGE
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
}