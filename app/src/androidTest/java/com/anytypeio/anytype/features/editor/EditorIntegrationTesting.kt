package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Checkbox
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Numbered
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Toggle
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_BULLET
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_CHECKBOX
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_H1
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_H2
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_H3
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_HIGHLIGHT
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_NUMBERED_1
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_PARAGRAPH
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_PARAGRAPH_1
import com.anytypeio.anytype.mocking.MockUiTests.BLOCK_TOGGLE
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import com.anytypeio.anytype.utils.scrollTo
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.*
import kotlinx.android.synthetic.main.fragment_page.*
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
Helping link For Espresso RecyclerView actions:
https://github.com/android/testing-samples/blob/master/ui/espresso/RecyclerViewSample/app/src/androidTest/java/com/example/android/testing/espresso/RecyclerViewSample/RecyclerViewSampleTest.java
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class EditorIntegrationTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    override fun setup() {
        super.setup()
    }

    @Test()
    fun shouldSetTextForTextBlocks() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val blocks = listOf(
            BLOCK_H1,
            BLOCK_H2,
            BLOCK_H3,
            BLOCK_PARAGRAPH,
            BLOCK_HIGHLIGHT,
            BLOCK_BULLET,
            BLOCK_NUMBERED_1,
            BLOCK_TOGGLE,
            BLOCK_CHECKBOX
        )

        val document = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = blocks.map { it.id }
            )
        ) + blocks

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)

        launchFragment(args)

        // TESTING

        onView(withId(R.id.recycler)).check(matches(isDisplayed()))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.headerOne))
            .check(matches(withText(BLOCK_H1.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.headerTwo))
            .check(matches(withText(BLOCK_H2.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.headerThree))
            .check(matches(withText(BLOCK_H3.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(3, R.id.textContent))
            .check(matches(withText(BLOCK_PARAGRAPH.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(4, R.id.highlightContent))
            .check(matches(withText(BLOCK_HIGHLIGHT.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(5, R.id.bulletedListContent))
            .check(matches(withText(BLOCK_BULLET.content.asText().text)))
//
        R.id.recycler.scrollTo<Numbered>(5)
//
        onView(withRecyclerView(R.id.recycler).atPositionOnView(6, R.id.numberedListContent))
            .check(matches(withText(BLOCK_NUMBERED_1.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(6, R.id.number))
            .check(matches(withText("1.")))

        R.id.recycler.scrollTo<Toggle>(7)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(7, R.id.toggleContent))
            .check(matches(withText(BLOCK_TOGGLE.content.asText().text)))

        R.id.recycler.scrollTo<Checkbox>(8)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(8, R.id.checkboxContent))
            .check(matches(withText(BLOCK_CHECKBOX.content.asText().text)))
    }

    @Test
    fun shouldAppendTextToTheEndAfterTyping() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val document = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(
                    BLOCK_PARAGRAPH_1.id
                )
            ),
            BLOCK_PARAGRAPH_1
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        launchFragment(args)

        // TESTING

        val text = " Add new text at the end"

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        target.apply {
            perform(click())
            perform(typeText(text))
            perform(closeSoftKeyboard())
        }

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        val expected = BLOCK_PARAGRAPH_1.content.asText().text + text

        target.check(matches(withText(expected)))
    }

    @Test
    fun shouldClearFocusAfterClickedOnHideKeyboard() {

        val args = bundleOf(PageFragment.ID_KEY to root)

        val document = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(
                    BLOCK_PARAGRAPH_1.id
                )
            ),
            BLOCK_PARAGRAPH_1
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)

        launchFragment(args)

        // TESTING

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        // Focusing

        target.perform(click())

        onView(withId(R.id.toolbar)).check(matches((isDisplayed())))
        target.check(matches((hasFocus())))

        // Unfocusing

        onView(withId(R.id.hideKeyboardButton)).perform(click())

        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))
        target.check(matches(not(hasFocus())))
    }

    @Test
    fun shouldSplitBlocks() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val text = "FooBar"

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = text,
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val document = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = paragraph.id,
                text = "Foo"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(paragraph.id, new.id)
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
            target = paragraph.id,
            style = Block.Content.Text.Style.P,
            mode = BlockSplitMode.BOTTOM,
            range = 3..3
        )

        stubSplitBlocks(
            new = new.id,
            events = events,
            command = command
        )

        val scenario = launchFragment(args)

        // TESTING

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        target.check(matches(withText(text)))

        target.perform(click())

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
           fragment.recycler.findViewById<TextInputWidget>(R.id.textContent).setSelection(3)
        }

        // Press ENTER

        target.perform(pressImeActionButton())

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(repo, times(1)) { split(command) }

        Thread.sleep(100)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            check(matches(withText("Foo")))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent)).apply {
            check(matches(withText("Bar")))
            check(matches(hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(R.id.textContent)
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

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION * 2)
    }

    /*
    @Test
    fun shouldCreateDividerBlockAfterFirstBlock() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeAddingDivider = 100L

        val paragraphBefore = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Block before divider",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val paragraphAfter = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Block after divider",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraphBefore.id, paragraphAfter.id)
            ),
            paragraphBefore,
            paragraphAfter
        )

        val divider = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Divider
        )

        /*
        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(delayBeforeAddingDivider)
                emit(
                    listOf(
                        Event.Command.GranularChange(
                            context = root,
                            id = paragraphBefore.id,
                            text = "Block before divider, get focus and add divider"
                        ),
                        Event.Command.UpdateStructure(
                            context = root,
                            id = page.first().id,
                            children = listOf(paragraphBefore.id, divider.id, paragraphAfter.id)
                        ),
                        Event.Command.AddBlock(
                            context = root,
                            blocks = listOf(divider)
                        )
                    )
                )
            }
        )

         */

        launchFragment(args)

        // TESTING

        advance(delayBeforeGettingEvents)

        val target1 = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))
        val target2 = onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent))

        target1.check(matches(withText(paragraphBefore.content.asText().text)))
        target2.check(matches(withText(paragraphAfter.content.asText().text)))

        advance(delayBeforeAddingDivider)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            check(matches(withText("Block before divider, get focus and add divider")))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.divider)).apply {
            check(matches(isDisplayed()))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.textContent)).apply {
            check(matches(withText("Block after divider")))
        }
    }

    @Test
    fun shouldCreateNewEmptyParagraph() {

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeAddingNewBlock = 100L

        val paragraph1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "First block",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val paragraph2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Second block",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraph1.id, paragraph2.id)
            ),
            paragraph1,
            paragraph2
        )

        val paragraphNew = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        /*
        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(delayBeforeAddingNewBlock)
                emit(
                    listOf(
                        Event.Command.UpdateStructure(
                            context = root,
                            id = page.first().id,
                            children = listOf(paragraph1.id, paragraphNew.id, paragraph2.id)
                        ),
                        Event.Command.AddBlock(
                            context = root,
                            blocks = listOf(paragraphNew)
                        )
                    )
                )
            }
        )

         */

        launchFragment(args)

        //TESTING

        advance(delayBeforeGettingEvents)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            perform(click())
        }

        advance(delayBeforeAddingNewBlock)

        Thread.sleep(1500)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            check(matches(withText("First block")))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent)).apply {
            check(matches(withText("")))
            check(matches(isDisplayed()))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.textContent)).apply {
            check(matches(withText("Second block")))
        }
    }

    /*
    @Test
    fun shouldHideOptionToolbarsOnEmptyBlockClick() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeKeyboardIsHidden = 300L

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
            }
        )

        launchFragment(args)

        // TESTING

        advance(delayBeforeGettingEvents)

        val targetBlock = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))

        targetBlock.perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.actions))).perform(click())

        advance(delayBeforeKeyboardIsHidden)

        val actionToolbar = onView(withId(R.id.actionToolbar))

        actionToolbar.check(matches(isDisplayed()))

        targetBlock.apply { perform(click()) }

        actionToolbar.check(matches(not(isDisplayed())))
    }

     */
     */

    // SETUP

    private fun launchFragment(args: Bundle) : FragmentScenario<TestPageFragment> {
        return launchFragmentInContainer<TestPageFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}