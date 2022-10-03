package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import com.anytypeio.anytype.test_utils.R as R_test


@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = [
        // required to access final members on androidx.loader.content.ModernAsyncTask
        "androidx.loader.content"
    ]
)
class BlockAdapterToggleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should intercept toggle click - when switching from read to edit mode`() {
        scenario.onFragment {

            var triggeredTimes = 0

            val recycler = givenRecycler(it)

            val block = givenToggle()

            val adapter = givenAdapter(
                views = listOf(block),
                onToggleClicked = { triggeredTimes++ }
            )

            recycler.adapter = adapter

            R_test.id.recycler.rVMatcher().apply {
                onItemView(0, R.id.toggle).checkIsDisplayed()
                onItemView(0, R.id.toggle).performClick()
            }

            assertEquals(
                actual = triggeredTimes,
                expected = 1
            )

            adapter.updateWithDiffUtil(
                listOf(
                    block.copy(
                        mode = BlockView.Mode.EDIT
                    )
                )
            )

            R_test.id.recycler.rVMatcher().apply {
                onItemView(0, R.id.toggle).performClick()
            }

            assertEquals(
                actual = triggeredTimes,
                expected = 2
            )
        }
    }

    private fun givenRecycler(it: Fragment): RecyclerView =
        it.view!!.findViewById<RecyclerView>(R_test.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }

    private fun givenToggle() = BlockView.Text.Toggle(
        mode = BlockView.Mode.READ,
        text = MockDataFactory.randomString(),
        id = MockDataFactory.randomUuid(),
        isFocused = false
    )
}