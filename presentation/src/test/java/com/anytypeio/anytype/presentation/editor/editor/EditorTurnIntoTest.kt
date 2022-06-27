package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.TurnIntoDocument
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorTurnIntoTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should invoke turnIntoDocument on for one text block and one file block in multi-select mode`() {

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
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2)

        stubOpenDocument(document = document)
        stubInterceptThreadStatus()
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        // TESTING

        val params = TurnIntoDocument.Params(
            context = root,
            targets = listOf(child1.id, child2.id)
        )

        stubTurnIntoDocument(params = params)

        vm.apply {
            onStart(root)
            onBlockFocusChanged(id = child1.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
            coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            onTextInputClicked(child1.id)
            onClickListener(
                ListenerType.File.View(child2.id)
            )
            onMultiSelectTurnIntoButtonClicked()
        }

        verifyBlocking(turnIntoDocument, times(1)) { invoke(params) }
    }
}