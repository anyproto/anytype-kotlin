package com.agileburo.anytype.features.editor

import android.os.Bundle
import android.view.KeyEvent
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
import com.agileburo.anytype.domain.block.interactor.UnlinkBlocks
import com.agileburo.anytype.domain.block.model.Block
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
class DeleteBlockTesting : EditorTestSetup() {

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
     * SHOULD DELETE SECOND BLOCK, THEN FOCUS FIRST ONE AND PLACE CURSOR AT ITS END
     */

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.textContent,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstHeader1WithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.H1,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.headerOne,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstHeader2WithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.H2,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.headerTwo,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstHeader3WithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.H3,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.headerThree,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstHighlightWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.QUOTE,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.highlightContent,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstCheckboxWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.CHECKBOX,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.checkboxContent,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstBulletedWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.BULLET,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.bulletedListContent,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstNumberedWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.NUMBERED,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.numberedListContent,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondParagraphByDeletingItsTextThenFocusFirstToggleWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.TOGGLE,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.toggleContent,
            targetViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteSecondHeader1ByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.H1,
            firstViewId = R.id.textContent,
            targetViewId = R.id.headerOne
        )
    }

    @Test
    fun shouldDeleteSecondHeader2ByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.H2,
            firstViewId = R.id.textContent,
            targetViewId = R.id.headerTwo
        )
    }

    @Test
    fun shouldDeleteSecondHeader3ByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.H3,
            firstViewId = R.id.textContent,
            targetViewId = R.id.headerThree
        )
    }

    @Test
    fun shouldDeleteSecondHighlightByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.QUOTE,
            firstViewId = R.id.textContent,
            targetViewId = R.id.highlightContent
        )
    }

    @Test
    fun shouldDeleteSecondCheckboxByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.CHECKBOX,
            firstViewId = R.id.textContent,
            targetViewId = R.id.checkboxContent
        )
    }

    @Test
    fun shouldDeleteSecondBulletedByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.BULLET,
            firstViewId = R.id.textContent,
            targetViewId = R.id.bulletedListContent
        )
    }

    @Test
    fun shouldDeleteSecondNumberedByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.NUMBERED,
            firstViewId = R.id.textContent,
            targetViewId = R.id.numberedListContent
        )
    }

    @Test
    fun shouldDeleteSecondToggleByDeletingItsTextThenFocusFirstParagraphWithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.P,
            secondStyle = Block.Content.Text.Style.TOGGLE,
            firstViewId = R.id.textContent,
            targetViewId = R.id.toggleContent
        )
    }

    private fun shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
        firstStyle: Block.Content.Text.Style,
        secondStyle: Block.Content.Text.Style,
        firstViewId: Int,
        targetViewId: Int
    ) {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = firstStyle
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = secondStyle
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
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(a.id)
            ),
            Event.Command.DeleteBlock(
                context = root,
                targets = listOf(b.id)
            )
        )

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(b.id)
        )

        stubInterceptEvents()
        stubOpenDocument(document)
        stubUpdateText()

        stubUnlinkBlocks(params, events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(2, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Delete text from B

        repeat(4) { target.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL)) }

        // Check results

        verifyBlocking(unlinkBlocks, times(1)) { invoke(params) }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, firstViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(firstViewId)
            assertEquals(
                expected = 3,
                actual = view.selectionStart
            )
            assertEquals(
                expected = 3,
                actual = view.selectionEnd
            )
        }

        // Release pending coroutines

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //endregion



    // STUBBING & SETUP

    private fun stubUnlinkBlocks(
        params: UnlinkBlocks.Params,
        events: List<Event.Command>
    ) {
        unlinkBlocks.stub {
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