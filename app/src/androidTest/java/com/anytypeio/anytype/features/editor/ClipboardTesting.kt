package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.data.auth.model.ClipEntity
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.android.synthetic.main.fragment_page.*
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class ClipboardTesting : EditorTestSetup() {

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
     * SHOULD PASTE PLAIN TEXT AT THE END OF THE TEXT BLOCK
     */

    @Test
    fun pastePlainTextAtTheEndOfParagraph() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.P,
            view = R.id.textContent
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfH1() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.H1,
            view = R.id.headerOne
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfH2() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.H2,
            view = R.id.headerTwo
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfH3() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.H3,
            view = R.id.headerThree
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfHighlight() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.QUOTE,
            view = R.id.highlightContent
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfCheckbox() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.CHECKBOX,
            view = R.id.checkboxContent
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfBullet() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.BULLET,
            view = R.id.bulletedListContent
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfNumbered() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.NUMBERED,
            view = R.id.numberedListContent
        )
    }

    @Test
    fun pastePlainTextAtTheEndOfToggle() {
        pastePlainTextAtTheEnd(
            style = Block.Content.Text.Style.TOGGLE,
            view = R.id.toggleContent
        )
    }

    private fun pastePlainTextAtTheEnd(
        style: Block.Content.Text.Style,
        view: Int
    ) {
        val text = "Foo"
        val pasted = "Bar"
        val result = "FooBar"

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
                text = result
            )
        )

        val command = Command.Paste(
            context = root,
            focus = a.id,
            range = 3..3,
            text = pasted,
            html = null,
            blocks = emptyList(),
            selected = emptyList()
        )

        clipboard.stub {
            onBlocking { clip() } doReturn ClipEntity(
                text = pasted,
                html = null,
                uri = null
            )
        }

        repo.stub {
            onBlocking { paste(any()) } doReturn Response.Clipboard.Paste(
                cursor = 6,
                payload = Payload(
                    context = root,
                    events = events
                ),
                blocks = emptyList(),
                isSameBlockCursor = true
            )
        }

        stubInterceptEvents()
        stubInterceptThreadStatus( )
        stubOpenDocument(document)
        stubUpdateText()

        val scenario = launchFragment(args)

        // TESTING

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, view))

        // Click to open action mode

        target.perform(click())
        target.perform(longClick())

        // Press "Paste"

        onView(anyOf(withText("Вставить"), withText("Paste"))).inRoot(isPlatformPopup())
            .perform(click());

        // Check results

        target.apply {
            check(matches(withText(result)))
            check(matches(hasFocus()))
        }

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(0)
            item.findViewById<TextInputWidget>(view).apply {
                assertEquals(expected = result.length, actual = selectionStart)
                assertEquals(expected = result.length, actual = selectionEnd)
            }
        }

        verifyBlocking(repo, times(1)) {
            paste(
                argThat {
                    this.context == command.context
                            &&
                            this.blocks == command.blocks
                            &&
                            this.html == command.html
                            &&
                            this.text == command.text
                            &&
                            this.selected == command.selected
                            &&
                            this.range == command.range
                            &&
                            this.focus == command.focus
                }
            )
        }

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //endregion

    //region SCENARIO II

    /**
     * SCENARIO II: PASTE TWO TEXT BLOCKS AT THE END OF THE FIRST TEXT BLOCK AFTER TITLE.
     */

    @Test
    fun pasteTwoParagraphsAtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.P,
            firstPastedBlockView = R.id.textContent,
            secondPastedBlockStyle = Block.Content.Text.Style.P,
            secondPastedBlockView = R.id.textContent
        )
    }

    @Test
    fun pasteTwoH1AtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.H1,
            firstPastedBlockView = R.id.headerOne,
            secondPastedBlockStyle = Block.Content.Text.Style.H1,
            secondPastedBlockView = R.id.headerOne
        )
    }

    @Test
    fun pasteTwoH2AtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.H2,
            firstPastedBlockView = R.id.headerTwo,
            secondPastedBlockStyle = Block.Content.Text.Style.H2,
            secondPastedBlockView = R.id.headerTwo
        )
    }

    @Test
    fun pasteTwoH3AtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.H3,
            firstPastedBlockView = R.id.headerThree,
            secondPastedBlockStyle = Block.Content.Text.Style.H3,
            secondPastedBlockView = R.id.headerThree
        )
    }

    @Test
    fun pasteTwoHighlightAtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.QUOTE,
            firstPastedBlockView = R.id.highlightContent,
            secondPastedBlockStyle = Block.Content.Text.Style.QUOTE,
            secondPastedBlockView = R.id.highlightContent
        )
    }

    @Test
    fun pasteTwoCheckboxAtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.CHECKBOX,
            firstPastedBlockView = R.id.checkboxContent,
            secondPastedBlockStyle = Block.Content.Text.Style.CHECKBOX,
            secondPastedBlockView = R.id.checkboxContent
        )
    }

    @Test
    fun pasteTwoBulletAtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.BULLET,
            firstPastedBlockView = R.id.bulletedListContent,
            secondPastedBlockStyle = Block.Content.Text.Style.BULLET,
            secondPastedBlockView = R.id.bulletedListContent
        )
    }

    @Test
    fun pasteTwoNumberedtAtEndOfFirstParagraphAfterTitle() {
        pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
            targetStyle = Block.Content.Text.Style.P,
            targetBlockView = R.id.textContent,
            firstPastedBlockStyle = Block.Content.Text.Style.NUMBERED,
            firstPastedBlockView = R.id.numberedListContent,
            secondPastedBlockStyle = Block.Content.Text.Style.NUMBERED,
            secondPastedBlockView = R.id.numberedListContent
        )
    }

    private fun pasteTwoTextBlocksAtEndOfFirstTextBlockAfterTitle(
        targetStyle: Block.Content.Text.Style,
        targetBlockView: Int,
        firstPastedBlockStyle: Block.Content.Text.Style,
        firstPastedBlockView: Int,
        secondPastedBlockStyle: Block.Content.Text.Style,
        secondPastedBlockView: Int
    ) {

        // SETUP

        val text = "They have a world to win"

        val uri = MockDataFactory.randomString()

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = text,
                marks = emptyList(),
                style = targetStyle
            )
        )

        val pasted = Pair(
            "Le dix-huitième siècle doit être mis au Panthéon",
            "These are indeed glorious times for the Engineers"
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = pasted.first,
                marks = emptyList(),
                style = firstPastedBlockStyle
            )
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = pasted.second,
                marks = emptyList(),
                style = secondPastedBlockStyle
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
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(a.id, b.id, c.id)
            ),
            Event.Command.AddBlock(
                context = root,
                blocks = listOf(b, c)
            )
        )

        val command = Command.Paste(
            context = root,
            focus = a.id,
            range = text.length..text.length,
            text = text,
            html = null,
            blocks = listOf(b, c),
            selected = emptyList()
        )

        clipboard.stub {
            onBlocking { clip() } doReturn ClipEntity(
                text = text,
                html = null,
                uri = uri
            )
        }

        clipboard.stub {
            onBlocking { blocks() } doReturn listOf(b, c)
        }

        uriMatcher.stub {
            on { isAnytypeUri(uri) } doReturn true
        }

        repo.stub {
            onBlocking { paste(any()) } doReturn Response.Clipboard.Paste(
                cursor = -1,
                payload = Payload(
                    context = root,
                    events = events
                ),
                blocks = listOf(b.id, c.id),
                isSameBlockCursor = false
            )
        }

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()

        val scenario = launchFragment(args)

        // TESTING

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, targetBlockView))

        // Click to open action mode

        target.perform(click())
        target.perform(longClick())

        // Press "Paste"

        onView(anyOf(withText("Вставить"), withText("Paste"))).inRoot(isPlatformPopup())
            .perform(click());

        // Check results

        verifyBlocking(repo, times(1)) {
            paste(
                argThat {
                    this.context == command.context
                            &&
                            this.blocks == command.blocks
                            &&
                            this.html == command.html
                            &&
                            this.text == command.text
                            &&
                            this.selected == command.selected
                            &&
                            this.range == command.range
                            &&
                            this.focus == command.focus
                }
            )
        }

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, targetBlockView)).apply {
            check(matches(withText(text)))
        }

        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, firstPastedBlockView)).apply {
            check(matches(withText(pasted.first)))
        }

        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, secondPastedBlockView)).apply {
            check(matches(withText(pasted.second)))
            check(matches(hasFocus()))
        }

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(2)
            item.findViewById<TextInputWidget>(secondPastedBlockView).apply {
                assertEquals(expected = pasted.second.length, actual = selectionStart)
                assertEquals(expected = pasted.second.length, actual = selectionEnd)
            }
        }
    }

    //endregion

    // STUBBING & SETUP

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