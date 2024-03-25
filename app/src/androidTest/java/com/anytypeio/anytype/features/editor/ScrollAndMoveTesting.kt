package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.TestUtils.withRecyclerView
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollAndMoveTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

//    @get:Rule
//    val coroutineTestRule = CoroutinesTestRule()

    val args = bundleOf(EditorFragment.CTX_KEY to root)

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = StubTextContent(
            style = Block.Content.Text.Style.TITLE,
            text = "Scroll-and-move UI testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    @Deprecated("Currently can't make it work due to animations running in production code")
    fun shouldEnterScrollAndMoveMode() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = StubTextContent(
                text = "Should enter multi-select mode",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = StubTextContent(
                text = "Should enter scroll-and-move mode",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubAnalytics()
        stubOpenDocument(
            document = document
        )

        launchFragment(args)

        // TESTING

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.title)).apply {
            perform(click())
        }

        Thread.sleep(100)

        onView(withId(R.id.btnBlockActions)).apply {
            perform(click())
        }

        Thread.sleep(100)

        // Release pending coroutines

        advance(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    // STUBBING & SETUP

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
//        coroutineTestRule.advanceTime(millis)
    }
}