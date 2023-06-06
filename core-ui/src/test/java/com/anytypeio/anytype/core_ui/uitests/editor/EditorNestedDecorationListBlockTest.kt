package com.anytypeio.anytype.core_ui.uitests.editor

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.ViewInteraction
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.StubBulletedView
import com.anytypeio.anytype.core_ui.StubCheckboxView
import com.anytypeio.anytype.core_ui.StubNumberedView
import com.anytypeio.anytype.core_ui.StubToggleView
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.uitests.givenAdapter
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.checkHasChildViewCount
import com.anytypeio.anytype.test_utils.utils.checkHasMarginStart
import com.anytypeio.anytype.test_utils.utils.checkHasViewGroupChildWithBackground
import com.anytypeio.anytype.test_utils.utils.checkHasViewGroupChildWithMarginLeft
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = [ "androidx.loader.content" ]
)
class EditorNestedDecorationListBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    /**
     *   Block with background
     *   ...Numbered block with background (rendered block)
     */
    @Test
    fun `numbered should have two backgrounds with indentation - when current block of another block`() {
        scenario.onFragment {

            // SETUP

            val bg1 = ThemeColor.YELLOW
            val bg2 = ThemeColor.ORANGE

            val numbered = StubNumberedView(
                indent = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = bg1
                    ),
                    BlockView.Decoration(
                        background = bg2
                    )
                ),
                backgroundColor = bg2
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(numbered))
            recycler.adapter = adapter

            val rvMatcher = com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher()

            // TESTING

            val decorationContainerView = rvMatcher.onItemView(0, R.id.decorationContainer)
            val graphicPlusTextContainerView = rvMatcher.onItemView(0, R.id.graphicPlusTextContainer)

            // Checking our decorations

            `first child view should have its background and zero margin, second child view should have its background and one-indent margin`(
                decorationContainerView, bg1, bg2
            )

            // Checking content left indentation

            graphicPlusTextContainerView.checkHasMarginStart(
                marginStart = context.resources.getDimension(R.dimen.default_indent).toInt() * 2
            )
        }
    }

    /**
     *   Block with background
     *   ...Bulleted block with background (rendered block)
     */
    @Test
    fun `bulleted should have two backgrounds with indentation - when current block of another block`() {
        scenario.onFragment {

            // SETUP

            val bg1 = ThemeColor.YELLOW
            val bg2 = ThemeColor.ORANGE

            val bulleted = StubBulletedView(
                indent = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = bg1
                    ),
                    BlockView.Decoration(
                        background = bg2
                    )
                ),
                backgroundColor = bg2
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(bulleted))
            recycler.adapter = adapter

            val rvMatcher = com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher()

            // TESTING

            val decorationContainerView = rvMatcher.onItemView(0, R.id.decorationContainer)
            val graphicPlusTextContainerView = rvMatcher.onItemView(0, R.id.graphicPlusTextContainer)

            // Checking our decorations

            `first child view should have its background and zero margin, second child view should have its background and one-indent margin`(
                decorationContainerView, bg1, bg2
            )

            // Checking content left indentation

            graphicPlusTextContainerView.checkHasMarginStart(
                marginStart = context.resources.getDimension(R.dimen.default_indent).toInt() * 2
            )
        }
    }

    /**
     *   Block with background
     *   ...Checkbox block with background (rendered block)
     */
    @Test
    fun `checkbox should have two backgrounds with indentation - when current block of another block`() {
        scenario.onFragment {

            // SETUP

            val bg1 = ThemeColor.YELLOW
            val bg2 = ThemeColor.ORANGE

            val bulleted = StubCheckboxView(
                indent = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = bg1
                    ),
                    BlockView.Decoration(
                        background = bg2
                    )
                ),
                backgroundColor = bg2
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(bulleted))
            recycler.adapter = adapter

            val rvMatcher = com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher()

            // TESTING

            val decorationContainerView = rvMatcher.onItemView(0, R.id.decorationContainer)
            val graphicPlusTextContainerView = rvMatcher.onItemView(0, R.id.graphicPlusTextContainer)

            // Checking our decorations

            `first child view should have its background and zero margin, second child view should have its background and one-indent margin`(
                decorationContainerView, bg1, bg2
            )

            // Checking content left indentation

            graphicPlusTextContainerView.checkHasMarginStart(
                marginStart = context.resources.getDimension(R.dimen.default_indent).toInt() * 2
            )
        }
    }

    /**
     *   Block with background
     *   ...Toggle block with background (rendered block)
     */
    @Test
    fun `toggle should have two backgrounds with indentation - when current block of another block`() {
        scenario.onFragment {

            // SETUP

            val bg1 = ThemeColor.YELLOW
            val bg2 = ThemeColor.ORANGE

            val bulleted = StubToggleView(
                indent = 1,
                decorations = listOf(
                    BlockView.Decoration(
                        background = bg1
                    ),
                    BlockView.Decoration(
                        background = bg2
                    )
                ),
                backgroundColor = bg2
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(bulleted))
            recycler.adapter = adapter

            val rvMatcher = com.anytypeio.anytype.test_utils.R.id.recycler.rVMatcher()

            // TESTING

            val decorationContainerView = rvMatcher.onItemView(0, R.id.decorationContainer)
            val graphicPlusTextContainerView = rvMatcher.onItemView(0, R.id.graphicPlusTextContainer)

            // Checking our decorations

            `first child view should have its background and zero margin, second child view should have its background and one-indent margin`(
                decorationContainerView, bg1, bg2
            )

            // Checking content left indentation

            graphicPlusTextContainerView.checkHasMarginStart(
                marginStart = context.resources.getDimension(R.dimen.default_indent).toInt() * 2
            )
        }
    }

    private fun `first child view should have its background and zero margin, second child view should have its background and one-indent margin`(
        decorationContainerView: ViewInteraction,
        bg1: ThemeColor,
        bg2: ThemeColor
    ) {
        decorationContainerView.checkIsDisplayed()
        decorationContainerView.checkHasChildViewCount(2)

        decorationContainerView.checkHasViewGroupChildWithBackground(
            pos = 0,
            background = context.resources.veryLight(bg1, 0),
        )
        decorationContainerView.checkHasViewGroupChildWithMarginLeft(
            pos = 0,
            margin = 0
        )

        decorationContainerView.checkHasViewGroupChildWithBackground(
            pos = 1,
            background = context.resources.veryLight(bg2, 0),
        )
        decorationContainerView.checkHasViewGroupChildWithMarginLeft(
            pos = 1,
            margin = context.resources.getDimension(R.dimen.default_indent).toInt()
        )
    }

    private fun givenRecycler(fr: Fragment): RecyclerView {
        val root = checkNotNull(fr.view)
        return root.findViewById<RecyclerView>(com.anytypeio.anytype.test_utils.R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
    }
}