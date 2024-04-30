package com.anytypeio.anytype.presentation.editor.editor

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorSlashWidgetCutFilterTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stubSpaceManager()
        stubGetNetworkMode()
        stubFileLimitEvents()
        stubGetObjectTypes(emptyList())
        stubAnalyticSpaceHelperDelegate()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun after() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Text`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Text
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Title`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Title
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Callout`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Callout
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Heading`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Heading
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Subheading`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Subheading
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Highlighted`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Highlighted
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Checkbox`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Checkbox
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Bullet`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Bulleted
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Numbered`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Numbered
        )
    }

    @Test
    fun `should invoke updateText useCase when clicked on Slash Item Toggle`() {
        `should invoke updateText useCase when clicked on Slash Item Style`(
            slashItem = SlashItem.Style.Type.Toggle
        )
    }

    private fun `should invoke updateText useCase when clicked on Slash Item Style`(
        slashItem: SlashItem
    ) = runTest {

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "FooBar",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, 6),
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubUpdateText()
        stubTurnIntoStyle()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(3, 3)
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Start(
                    cursorCoordinate = 820,
                    slashStart = 3
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Foo/Bar",
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = 0,
                            to = 7
                        )
                    ),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = block.id,
                selection = IntRange(4, 4)
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/s",
                    viewType = 0
                )
            )
            advanceUntilIdle()
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Foo/sBar",
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = 0,
                            to = 8
                        )
                    ),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = block.id,
                selection = IntRange(5, 5)
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/su",
                    viewType = 0
                )
            )
            advanceUntilIdle()
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Foo/suBar",
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = 0,
                            to = 9
                        )
                    ),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = block.id,
                selection = IntRange(6, 6)
            )
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(
            item = slashItem
        )

        advanceUntilIdle()

        val params = UpdateText.Params(
            context = root,
            target = block.id,
            text = "FooBar",
            marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 6),
                    type = Block.Content.Text.Mark.Type.BOLD
                )
            )
        )

        verifyBlocking(updateText, times(1)) { invoke(params) }
    }
}