package com.anytypeio.anytype.features.editor

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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
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

    private val args = bundleOf(PageFragment.ID_KEY to root)

    @Before
    override fun setup() {
        super.setup()
    }

    //region SCENARIO I

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

    @Test
    fun shouldDeleteSecondH1ByDeletingItsTextThenFocusFirstHeader1WithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.H1,
            secondStyle = Block.Content.Text.Style.H1,
            firstViewId = R.id.headerOne,
            targetViewId = R.id.headerOne
        )
    }

    @Test
    fun shouldDeleteSecondH2ByDeletingItsTextThenFocusFirstHeader2WithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.H2,
            secondStyle = Block.Content.Text.Style.H2,
            firstViewId = R.id.headerTwo,
            targetViewId = R.id.headerTwo
        )
    }

    @Test
    fun shouldDeleteSecondH3ByDeletingItsTextThenFocusFirstHeader3WithCursorAtItsEnd() {
        shouldDeleteSecondBlockByDeletingItsTextThenFocusFirstOneWithCursorAtItsEnd(
            firstStyle = Block.Content.Text.Style.H3,
            secondStyle = Block.Content.Text.Style.H3,
            firstViewId = R.id.headerThree,
            targetViewId = R.id.headerThree
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
        stubInterceptThreadStatus()
        stubUpdateText()

        stubUnlinkBlocks(params, events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Delete text from B

        repeat(4) { target.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL)) }

        // Check results

        verifyBlocking(unlinkBlocks, times(1)) { invoke(params) }

        Thread.sleep(100)

        Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, firstViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Foo")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(0)
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

    //region SCENARIO II

    /**
     * SCENARIO II
     * SHOULD DELETE THE FIRST BLOCK AFTER TITLE, THEN FOCUS THE DOCUMENT'S TITLE
     */

//    @Test
//    fun shouldDeleteFirstParagraphAfterTitleByDeletingItsTextThenFocusTitle() {
//        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
//            firstStyle = Block.Content.Text.Style.P,
//            secondStyle = Block.Content.Text.Style.P,
//            firstViewId = R.id.textContent,
//            secondViewId = R.id.textContent
//        )
//    }

    @Test
    fun shouldDeleteFirstH1AfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.H1,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.headerOne,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstH2AfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.H2,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.headerTwo,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstH3AfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.H3,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.headerThree,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstHighlightAfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.QUOTE,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.highlightContent,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstCheckboxAfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.CHECKBOX,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.checkboxContent,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstBulletAfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.BULLET,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.bulletedListContent,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstNumberedAfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.NUMBERED,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.numberedListContent,
            secondViewId = R.id.textContent
        )
    }

    @Test
    fun shouldDeleteFirstToggleAfterTitleByDeletingItsTextThenFocusTitle() {
        shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
            firstStyle = Block.Content.Text.Style.TOGGLE,
            secondStyle = Block.Content.Text.Style.P,
            firstViewId = R.id.toggleContent,
            secondViewId = R.id.textContent
        )
    }

    private fun shouldDeleteFirstBlockAfterTitleByDeletingItsTextThenFocusTitle(
        title: String = "Title",
        firstStyle: Block.Content.Text.Style,
        secondStyle: Block.Content.Text.Style,
        firstViewId: Int,
        secondViewId: Int
    ) {

        // SETUP

        val titleBlock = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = title,
                marks = emptyList(),
                style = Block.Content.Text.Style.TITLE
            )
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(titleBlock.id)
        )

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
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, titleBlock, a, b)

        val events = listOf(
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(header.id, b.id)
            ),
            Event.Command.DeleteBlock(
                context = root,
                targets = listOf(a.id)
            )
        )

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(a.id)
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()

        stubOpenDocument(
            document = document,
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf("name" to title)
                    )
                )
            )
        )

        stubUpdateText()

        stubUnlinkBlocks(params, events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, firstViewId)
        )

        target.perform(ViewActions.click())

        // Set cursor programmatically

        scenario.onFragment { fragment ->
            fragment.recycler.findViewById<TextInputWidget>(firstViewId).setSelection(
                a.content<Block.Content.Text>().text.length
            )
        }

        Thread.sleep(100)

        // Delete text from B

        repeat(4) { target.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL)) }

        // Check results

        verifyBlocking(unlinkBlocks, times(1)) { invoke(params) }

        Thread.sleep(100)

        Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.title)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText(title)))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, secondViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("Bar")))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(0)
            val view = item.findViewById<TextInputWidget>(R.id.title)
            assertEquals(
                expected = title.length,
                actual = view.selectionStart
            )
            assertEquals(
                expected = title.length,
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