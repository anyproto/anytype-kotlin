package com.agileburo.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.CreateBlock
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.features.editor.base.EditorTestSetup
import com.agileburo.anytype.features.editor.base.TestPageFragment
import com.agileburo.anytype.mocking.MockDataFactory
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.ui.page.PageFragment
import com.agileburo.anytype.utils.CoroutinesTestRule
import com.agileburo.anytype.utils.TestUtils
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
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
class CreateBlockTesting : EditorTestSetup() {

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
     * SHOULD CREATE A NEW PARAGRAPH BY PRESSING ENTER INSIDE ANY EMPTY TEXT BLOCK EXCEPT LISTS
     */

    @Test
    fun createNewParagraphByPressingEnterInsideEmptyParagraph() {
        createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.P,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun createNewParagraphByPressingEnterInsideEmptyH1() {
        createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.H1,
            targetViewId = R.id.headerOne
        )
    }

    @Test
    fun createNewParagraphByPressingEnterInsideEmptyH2() {
        createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.H2,
            targetViewId = R.id.headerTwo
        )
    }

    @Test
    fun createNewParagraphByPressingEnterInsideEmptyH3() {
        createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.H3,
            targetViewId = R.id.headerThree
        )
    }

    @Test
    fun createNewParagraphByPressingEnterInsideEmptyHighlight() {
        createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.QUOTE,
            targetViewId = R.id.highlightContent
        )
    }

    @Test
    fun createNewParagraphByPressingEnterInsideEmptyToggle() {
        createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.TOGGLE,
            targetViewId = R.id.toggleContent
        )
    }

    private fun createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
        targetStyle: Block.Content.Text.Style,
        targetViewId: Int
    ) {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = targetStyle
            )
        )

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
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

        val params = CreateBlock.Params(
            context = root,
            position = Position.BOTTOM,
            target = a.id,
            prototype = Block.Prototype.Text(
                style = Block.Content.Text.Style.P
            )
        )

        stubInterceptEvents()
        stubOpenDocument(document)
        stubUpdateText()
        stubCreateBlocks(params, new, events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Press ENTER on empty text block A

        target.perform(ViewActions.pressImeActionButton())

        // Check results

        verifyBlocking(createBlock, times(1)) { invoke(params) }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
        }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.textContent)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position at block B

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(2)
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

    //endregion

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