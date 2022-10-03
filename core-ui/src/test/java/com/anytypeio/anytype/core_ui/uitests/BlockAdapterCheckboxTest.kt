package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.os.Build
import android.widget.EditText
import androidx.core.text.getSpans
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.CheckedCheckboxColorSpan
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.test_utils.utils.view_action.extractView
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
class BlockAdapterCheckboxTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should not have checkbox highlight span - when turn checkbox from true to false`() {
        scenario.onFragment {

            val recycler = givenRecycler(it)

            val block = givenCheckbox()

            val adapter = givenAdapter(listOf(block))

            recycler.adapter = adapter

            R_test.id.recycler.rVMatcher().apply {
                val spans =
                    onItemView(
                        0,
                        R.id.checkboxContent
                    ).extractView<EditText>().text.getSpans<CheckedCheckboxColorSpan>(0)

                assertEquals(
                    expected = 1,
                    actual = spans.size
                )

                onItemView(0, R.id.checkboxIcon).performClick()

                val spansAfter =
                    onItemView(0, R.id.checkboxContent).extractView<EditText>().text
                        .getSpans<CheckedCheckboxColorSpan>(0)

                assertEquals(
                    expected = 0,
                    actual = spansAfter.size
                )
            }
        }
    }

    @Test
    fun `checkbox-clicked listener should be enabled - when switching from read to edit mode`() {
        scenario.onFragment {

            var trigger = 0

            val recycler = givenRecycler(it)

            val block = givenCheckbox(BlockView.Mode.READ)

            val adapter = givenAdapter(
                views = listOf(block),
                onCheckboxClicked = { trigger++ }
            )

            recycler.adapter = adapter

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.checkboxIcon).performClick()

                assertEquals(
                    expected = 0,
                    actual = trigger
                )
            }

            val updated = listOf(
                block.copy(
                    mode = BlockView.Mode.EDIT
                )
            )

            adapter.updateWithDiffUtil(items = updated)

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.checkboxIcon).performClick()

                assertEquals(
                    expected = 1,
                    actual = trigger
                )
            }
        }
    }

    private fun givenRecycler(it: Fragment): RecyclerView =
        it.view!!.findViewById<RecyclerView>(R_test.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }

    private fun givenCheckbox(mode: BlockView.Mode = BlockView.Mode.EDIT) = BlockView.Text.Checkbox(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        isFocused = true,
        isChecked = true,
        mode = mode
    )
}