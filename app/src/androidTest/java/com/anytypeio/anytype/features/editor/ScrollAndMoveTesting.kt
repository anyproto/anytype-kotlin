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
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
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

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val args = bundleOf(PageFragment.ID_KEY to root)

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    @Deprecated("Currently can't make it work due to animations running in production code")
    fun shouldEnterScrollAndMoveMode() {

        // SETUP

        val title = "Scroll-and-move UI testing"

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Should enter multi-select mode",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Should enter scroll-and-move mode",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
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

        stubInterceptEvents()
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

        launchFragment(args)

        // TESTING

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.title)).apply {
            perform(click())
        }

        Thread.sleep(1000)

        onView(withId(R.id.structure)).apply {
            perform(click())
        }

        Thread.sleep(3000)

        // Release pending coroutines

        advance(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

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