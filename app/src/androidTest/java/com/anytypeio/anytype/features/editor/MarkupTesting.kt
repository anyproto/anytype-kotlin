package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MarkupTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val args = bundleOf(EditorFragment.ID_KEY to root)

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldNormalizeAndRenderMarkup() {

        // SETUP

        val text = "Start FooBar   end"

        val mentionTarget = MockDataFactory.randomUuid()

        val a = MockBlockFactory.text(
            content = StubTextContent(
                text = text,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, 18),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                        param = null
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, 18),
                        type = Block.Content.Text.Mark.Type.KEYBOARD,
                        param = null
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, 18),
                        type = Block.Content.Text.Mark.Type.ITALIC,
                        param = null
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(0, 18),
                        type = Block.Content.Text.Mark.Type.BOLD,
                        param = null
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(6, 12),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        val fields = Block.Fields(mapOf(Block.Fields.NAME_KEY to "FooBa"))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubAnalytics()
        stubOpenDocument(
            document = document,
            details = Block.Details(mapOf(mentionTarget to fields))
        )
        stubUpdateText()

        launchFragment(args)

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.textContent).apply {
                checkHasText("Start FooBa   end")
            }
        }

        // Release pending coroutines

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
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