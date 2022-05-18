package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.lighter
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.checkHasBackgroundColor
import com.anytypeio.anytype.test_utils.utils.checkHasNoBackground
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.anytypeio.anytype.test_utils.R as TestResource

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = ["androidx.loader.content"]
)
class TitleBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val resources: Resources = context.resources
    private lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should show title block with default background`() {
        scenario.onFragment {

            // SETUP

            val title = givenTitleBlock()

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(title))
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.apply {
                onItemView(0, R.id.root).checkIsDisplayed()
                onItemView(0, R.id.title).checkHasNoBackground()
                onItemView(0, R.id.title).checkHasText(title.text!!)
            }
        }
    }

    @Test
    fun `should show title block with red background`() {
        scenario.onFragment {

            // SETUP

            val redBackground = ThemeColor.RED

            val title = givenTitleBlock(
                backgroundColor = redBackground.code
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(
                listOf(title)
            )
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.apply {
                onItemView(0, R.id.root).checkIsDisplayed()
                onItemView(0, R.id.title).checkHasBackgroundColor(
                    resources.lighter(redBackground, 0)
                )
                onItemView(0, R.id.title).checkHasText(title.text!!)
            }
        }
    }

    private fun givenTitleBlock(
        isFocused: Boolean = false,
        mode: BlockView.Mode = BlockView.Mode.EDIT,
        backgroundColor: String = ThemeColor.DEFAULT.code
    ) = BlockView.Title.Basic(
        text = MockDataFactory.randomString(),
        id = MockDataFactory.randomUuid(),
        mode = mode,
        isFocused = isFocused,
        backgroundColor = backgroundColor
    )

    private fun givenRecycler(fr: Fragment): RecyclerView {
        val root = checkNotNull(fr.view)
        return root.findViewById<RecyclerView>(TestResource.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
    }
}