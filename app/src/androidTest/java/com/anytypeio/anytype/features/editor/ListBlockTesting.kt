package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.android.synthetic.main.fragment_page.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ListBlockTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val args = bundleOf(PageFragment.ID_KEY to root)

    @Before
    override fun setup() {
        super.setup()
    }

    //region SCENARIO 1

    /**
     * SCENARIO I
     * SHOULD CREATE A LIST ITEM OF THE SAME STYLE BY PRESSING ENTER ON A LIST ITEM
     */

    @Test
    fun shouldCreateNewBulletByPressingEnterAtEndOfBullet() {
        shouldCreateListItemWithSameStyleByPressingEnterAtEndOfListItem(
            style = Block.Content.Text.Style.BULLET,
            view = R.id.bulletedListContent
        )
    }

    @Test
    fun shouldCreateNewNumberedByPressingEnterAtEndOfNumbered() {
        shouldCreateListItemWithSameStyleByPressingEnterAtEndOfListItem(
            style = Block.Content.Text.Style.NUMBERED,
            view = R.id.numberedListContent
        )
    }

    @Test
    fun shouldCreateCheckboxByPressingEnterAtEndOfCheckbox() {
        shouldCreateListItemWithSameStyleByPressingEnterAtEndOfListItem(
            style = Block.Content.Text.Style.CHECKBOX,
            view = R.id.checkboxContent
        )
    }

    private fun shouldCreateListItemWithSameStyleByPressingEnterAtEndOfListItem(
        style: Block.Content.Text.Style,
        view: Int
    ) {

        // SETUP
        
        val text ="Should create a new list item with the same style by pressing ENTER at the end of the target list item"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = text,
                marks = emptyList(),
                style = style
            )
        )

        val new = Block(
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
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        val events = listOf(
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(a.id, new.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(new)
            )
        )

        val command = Command.Split(
            context = root,
            target = a.id,
            range = text.length..text.length,
            style = style,
            mode = BlockSplitMode.BOTTOM
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubSplitBlocks(
            command = command,
            new = new.id,
            events = events
        )

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, view)
        )

        target.apply {
            perform(ViewActions.click())
        }

        Thread.sleep(100)

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(view).setSelection(text.length)
        }

        // Press ENTER on empty text block A

        target.perform(ViewActions.pressImeActionButton())

        // Check results

        verifyBlocking(repo, times(1)) { split(command) }

        Thread.sleep(100)

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, view)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText(a.content<Block.Content.Text>().text)))
        }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, view)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position at block B

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
            item.findViewById<TextInputWidget>(view).apply {
                assertEquals(
                    expected = 0,
                    actual = selectionStart
                )
                assertEquals(
                    expected = 0,
                    actual = selectionEnd
                )
            }
        }

        // Release pending coroutines

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //endregion

    //region SCENARIO 2

    /**
     * SCENARIO II
     * SHOULD REPLACE A LIST ITEM BY A PARAGRAPH IF ITS TEXT IS EMPTY ON ENTER PRESS.
     */

    @Test
    fun shouldReplaceBulletByParagraphIfItsTextIsEmptyOnEnterPress() {
        shouldReplaceListItemByParagraphIfItsTextIsEmptyOnEnterPress(
            style = Block.Content.Text.Style.BULLET,
            view = R.id.bulletedListContent
        )
    }

    @Test
    fun shouldReplaceNumberedByParagraphIfItsTextIsEmptyOnEnterPress() {
        shouldReplaceListItemByParagraphIfItsTextIsEmptyOnEnterPress(
            style = Block.Content.Text.Style.NUMBERED,
            view = R.id.numberedListContent
        )
    }

    @Test
    fun shouldReplaceCheckboxByParagraphIfItsTextIsEmptyOnEnterPress() {
        shouldReplaceListItemByParagraphIfItsTextIsEmptyOnEnterPress(
            style = Block.Content.Text.Style.CHECKBOX,
            view = R.id.checkboxContent
        )
    }

    private fun shouldReplaceListItemByParagraphIfItsTextIsEmptyOnEnterPress(
        style: Block.Content.Text.Style,
        view: Int
    ) {

        // SETUP

        val description ="Should replace the target list item by a paragraph on enter press if its text is empty"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = description,
                marks = emptyList(),
                style = style
            )
        )

        val b = Block(
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
            children = listOf(a.id, b.id)
        )

        val document = listOf(page, a, b)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = b.id,
                style = Block.Content.Text.Style.P
            )
        )

        val params = UpdateTextStyle.Params(
            context = root,
            targets = listOf(b.id),
            style = Block.Content.Text.Style.P
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubUpdateTextStyle(params, events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, view)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Press ENTER on empty text block A

        target.perform(ViewActions.pressImeActionButton())

        // Check results

        Thread.sleep(100)

        verifyBlocking(updateTextStyle, times(1)) { invoke(params) }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, view)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText(a.content<Block.Content.Text>().text)))
        }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position at block B

        Thread.sleep(100)

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
            item.findViewById<TextInputWidget>(R.id.textContent).apply {
                assertEquals(
                    expected = 0,
                    actual = selectionStart
                )
                assertEquals(
                    expected = 0,
                    actual = selectionEnd
                )
            }
        }

        // Release pending coroutines

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    // STUBBING & SETUP

    private fun stubCreateBlocks(
        params: CreateBlock.Params,
        new: Block,
        events: List<Event.Command>
    ) {
        createBlock.stub {
            onBlocking {
                invoke(params)
            } doReturn Either.Right(
                Pair(new.id, Payload(context = root, events = events))
            )
        }
    }

    private fun stubUpdateTextStyle(
        params: UpdateTextStyle.Params,
        events: List<Event.Command.GranularChange>
    ) {
        updateTextStyle.stub {
            onBlocking { invoke(params) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestPageFragment> {
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