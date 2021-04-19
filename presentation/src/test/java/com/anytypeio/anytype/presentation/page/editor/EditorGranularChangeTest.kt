package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorGranularChangeTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should update checkbox via granular change event`() {

        // SETUP

        val delay = 1000L

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

        val checkbox = Block(
            id = MockDataFactory.randomString(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                isChecked = false,
                marks = emptyList(),
                style = Block.Content.Text.Style.CHECKBOX
            )
        )

        val doc = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(
                    type = Block.Content.Smart.Type.PAGE
                ),
                children = listOf(header.id, checkbox.id)
            ),
            header,
            title,
            checkbox
        )

        val events = flow<List<Event>> {
            delay(delay)
            emit(
                listOf(
                    Event.Command.GranularChange(
                        context = root,
                        id = checkbox.id,
                        checked = true
                    )
                )
            )
        }

        stubOpenDocument(doc)
        stubInterceptEvents(flow = events)

        val vm = buildViewModel()

        // Expected value before granular change vent

        val before = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Checkbox(
                    id = checkbox.id,
                    text = checkbox.content<Block.Content.Text>().text
                )
            )
        )

        // Expected value after granular change vent

        val after = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Checkbox(
                    id = checkbox.id,
                    text = checkbox.content<Block.Content.Text>().text,
                    isChecked = true
                )
            )
        )

        // TESTING

        vm.onStart(root)

        // Checking that checkbox is not checked

        vm.state.test().assertValue(before)

        // Moving time forward to receive granular change event

        coroutineTestRule.advanceTime(delay)

        // Checking that checkbox is checked

        vm.state.test().assertValue(after)
    }
}