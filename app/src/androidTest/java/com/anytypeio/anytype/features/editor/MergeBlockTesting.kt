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
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.*
import kotlinx.android.synthetic.main.fragment_page.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class MergeBlockTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldMergeTwoParagraphs() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.P

        val target = R.id.textContent

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoH1() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.H1

        val target = R.id.headerOne

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoH2() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.H2

        val target = R.id.headerTwo

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoH3() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.H3

        val target = R.id.headerThree

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoHighlight() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.QUOTE

        val target = R.id.highlightContent

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoCheckbox() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.CHECKBOX

        val target = R.id.checkboxContent

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoBullet() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.BULLET

        val target = R.id.bulletedListContent

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoNumbered() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.NUMBERED

        val target = R.id.numberedListContent

        basicScenario(style, args, target)
    }

    @Test
    fun shouldMergeTwoToggle() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.TOGGLE

        val target = R.id.toggleContent

        basicScenario(style, args, target)
    }

    /**
     * @param style style of two block to merge
     * @param args args for fragment
     * @param targetViewId id of the target view in recycler (which we need to merge)
     */
    private fun basicScenario(
        style: Block.Content.Text.Style,
        args: Bundle,
        targetViewId: Int
    ) {
        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = style
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
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
                id = a.id,
                text = "FooBar"
            ),
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

        val params = MergeBlocks.Params(
            context = root,
            pair = Pair(a.id, b.id)
        )

        stubInterceptEvents()
        stubOpenDocument(document)
        stubUpdateText()
        stubMergelocks(
            params = params,
            events = events
        )

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(2, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Set cursor at the beginning of B

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(2)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            view.setSelection(0)
        }

        // Wait till cursor is ready

        Thread.sleep(100)

        // Press BACKSPACE

        target.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

        // Check results

        verifyBlocking(updateText, times(1)) { invoke(any()) }
        verifyBlocking(mergeBlocks, times(1)) { invoke(params) }

        Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(1, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("FooBar")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(targetViewId)
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

    /**
     * STUBBING AND SETTINGS
     */

    private fun stubMergelocks(
        params: MergeBlocks.Params,
        events: List<Event.Command>
    ) {
        mergeBlocks.stub {
            onBlocking {
                invoke(params)
            } doReturn Either.Right(
                Payload(context = root, events = events)
            )
        }
    }

    private fun launchFragment(args: Bundle) : FragmentScenario<TestPageFragment> {
        return launchFragmentInContainer<TestPageFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}