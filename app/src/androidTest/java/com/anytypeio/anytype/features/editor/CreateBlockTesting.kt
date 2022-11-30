package com.anytypeio.anytype.features.editor

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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.test_utils.utils.TestUtils
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class CreateBlockTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val args = bundleOf(EditorFragment.ID_KEY to root)

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
        shouldReplaceParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
            targetStyle = Block.Content.Text.Style.TOGGLE,
            targetViewId = R.id.toggleContent
        )
    }

    private fun createNewParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
        targetStyle: Block.Content.Text.Style,
        targetViewId: Int
    ) {

        // SETUP
        val a = MockBlockFactory.text(
            content = StubTextContent(
                text = "",
                style = targetStyle,
            )
        )

        val new = MockBlockFactory.paragraph(
            text = ""
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
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
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubCreateBlocks(params, new, events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Press ENTER on empty text block A

        target.perform(ViewActions.pressImeActionButton())

        // Check results

        verifyBlocking(createBlock, times(1)) { asFlow(params) }

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
        }

        Thread.sleep(100)

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position at block B

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
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

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    private fun shouldReplaceParagraphByPressingEnterInsideAnyEmptyTextBlockExceptLists(
        targetStyle: Block.Content.Text.Style,
        targetViewId: Int
    ) {

        // SETUP

        val a = MockBlockFactory.text(
            content = StubTextContent(
                text = "",
                style = targetStyle,
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = a.id,
                style = Block.Content.Text.Style.P
            )
        )

        val params = UpdateTextStyle.Params(
            context = root,
            targets = listOf(a.id),
            style = Block.Content.Text.Style.P
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubUpdateTextStyle(events = events)

        val scenario = launchFragment(args)

        // TESTING

        val target = Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Press ENTER on empty text block A

        target.perform(ViewActions.pressImeActionButton())

        // Check results

        verifyBlocking(updateTextStyle, times(1)) { invoke(params) }

        Thread.sleep(100)

        Espresso.onView(
            TestUtils.withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)
        ).apply {
            check(ViewAssertions.matches(ViewMatchers.withText("")))
            check(ViewAssertions.matches(ViewMatchers.hasFocus()))
        }

        // Check cursor position at block B

        scenario.onFragment { fragment ->
            val item = fragment.binding.recycler.getChildAt(0)
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

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
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
                execute(params)
            } doReturn Resultat.success(
                Pair(new.id, Payload(context = root, events = events))
            )
        }
    }


    private fun launchFragment(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
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