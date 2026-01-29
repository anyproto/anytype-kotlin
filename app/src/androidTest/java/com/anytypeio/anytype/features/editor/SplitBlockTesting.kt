package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.action.ViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.MockBlockFactory.text
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsFocused
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@RunWith(AndroidJUnit4::class)
@LargeTest
class SplitBlockTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldSplitParagraph() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.P

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            mode = BlockSplitMode.BOTTOM,
            range = 3..3,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.textContent

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitH1() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.H1

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.headerOne

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitH2() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.H2

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.headerTwo

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitH3() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.H3

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.headerThree

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitHighlight() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.QUOTE

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.highlightContent

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        Thread.sleep(100)

        target.checkHasText(text)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitCheckbox() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.CHECKBOX

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.checkboxContent

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        Thread.sleep(100)

        target.checkHasText(text)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitBullet() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.BULLET

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.bulletedListContent

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitNumbered() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.NUMBERED

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.numberedListContent

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitToggle() {

        // SETUP

        val args = bundleOf(EditorFragment.CTX_KEY to root, EditorFragment.SPACE_ID_KEY to defaultSpace)

        val text = "FooBar"

        val style = Block.Content.Text.Style.TOGGLE

        val block = text(
            content = StubTextContent(
                text = "",
                style = style
            )
        )

        val new = text(
            content = StubTextContent(
                text = "Bar",
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = block.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(block.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val command = Command.Split(
            context = root,
            target = block.id,
            range = 3..3,
            mode = BlockSplitMode.BOTTOM,
            style = style
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.toggleContent

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()
        val target = rvMatcher.onItemView(0, targetViewId)

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.checkHasText(text)

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.binding.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        rvMatcher.onItemView(0, targetViewId).checkHasText("Foo")

        Thread.sleep(100)

        rvMatcher.onItemView(1, targetViewId).apply {
            checkHasText("Bar")
            checkIsFocused()
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            assertEquals(
                expected = 0,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 0,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //region SETUPØ

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }

    private fun launchFragment(args: Bundle) : FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    //endregion SETUP
}