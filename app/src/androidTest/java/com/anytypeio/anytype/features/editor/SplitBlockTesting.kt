package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.android.synthetic.main.fragment_page.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

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

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.P

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitH1() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.H1

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitH2() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.H2

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitH3() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.H3

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitHighlight() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.QUOTE

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        Thread.sleep(100)

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitCheckbox() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.CHECKBOX

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        Thread.sleep(100)

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitBullet() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.BULLET

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitNumbered() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.NUMBERED

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSplitToggle() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val style = Block.Content.Text.Style.TOGGLE

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(block.id)
        )

        val document = listOf(page, block)

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

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

        val target = onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
            perform(ViewActions.typeText(text))
        }

        target.check(ViewAssertions.matches(ViewMatchers.withText(text)))

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(targetViewId).setSelection(3)
        }

        Thread.sleep(100)

        // Press ENTER

        target.perform(ViewActions.pressImeActionButton())

        Thread.sleep(100)

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
        }

        Thread.sleep(100)

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //region SETUPØ

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }

    private fun launchFragment(args: Bundle) : FragmentScenario<TestPageFragment> {
        return launchFragmentInContainer<TestPageFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    //endregion SETUP
}