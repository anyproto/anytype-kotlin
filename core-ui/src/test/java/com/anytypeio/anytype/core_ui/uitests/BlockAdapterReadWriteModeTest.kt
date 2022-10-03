package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.os.Build
import android.view.KeyEvent
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.onItemView
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
class BlockAdapterReadWriteModeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `split-line-enter press listener should be enabled - when switching from read to edit mode`() {
        scenario.onFragment {

            var trigger = 0

            val recycler = givenRecycler(it)

            val block = givenParagraph()

            val adapter = givenAdapter(
                views = listOf(block),
                onSplitLineEnterClicked = { _, _, _ -> trigger++ }
            )

            recycler.adapter = adapter

            val updated = listOf(
                block.copy(
                    mode = BlockView.Mode.EDIT,
                    cursor = 5,
                    isFocused = true
                )
            )

            adapter.updateWithDiffUtil(items = updated)

            R_test.id.recycler.rVMatcher().apply {

                val selectionStart =
                    onItemView(0, R.id.textContent).extractView<TextView>().selectionStart

                assertEquals(
                    expected = 5,
                    actual = selectionStart
                )

                onItemView(0, R.id.textContent).perform(ViewActions.pressImeActionButton())

                assertEquals(
                    expected = 1,
                    actual = trigger
                )
            }
        }
    }

    @Test
    fun `endline-enter press listener should be enabled - when switching from read to edit mode`() {
        scenario.onFragment {

            var trigger = 0

            val recycler = givenRecycler(it)

            val block = givenParagraph()

            val adapter = givenAdapter(
                views = listOf(block),
                onSplitLineEnterClicked = { _, _, _ -> trigger++ }
            )

            recycler.adapter = adapter

            val updated = listOf(
                block.copy(
                    mode = BlockView.Mode.EDIT,
                    cursor = block.text.length,
                    isFocused = true
                )
            )

            adapter.updateWithDiffUtil(items = updated)

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.textContent).perform(ViewActions.pressImeActionButton())

                assertEquals(
                    expected = 1,
                    actual = trigger
                )
            }
        }
    }

    @Test
    fun `on-non-empty-block-backspace-press listener should be enabled - when switching from read to edit mode`() {
        scenario.onFragment {

            var trigger = 0

            val recycler = givenRecycler(it)

            val block = givenParagraph()

            val adapter = givenAdapter(
                views = listOf(block),
                onNonEmptyBlockBackspaceClicked = { _, _ -> trigger++ }
            )

            recycler.adapter = adapter

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.textContent).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

                assertEquals(
                    expected = 0,
                    actual = trigger
                )
            }

            val updated = listOf(
                block.copy(
                    mode = BlockView.Mode.EDIT,
                    cursor = 0,
                    isFocused = true
                )
            )

            adapter.updateWithDiffUtil(items = updated)

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.textContent).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

                assertEquals(
                    expected = 1,
                    actual = trigger
                )
            }
        }
    }

    @Test
    fun `on-empty-block-backspace-press listener should be enabled - when switching from read to edit mode`() {
        scenario.onFragment {

            var trigger = 0

            val recycler = givenRecycler(it)

            val block = givenParagraph()

            val adapter = givenAdapter(
                views = listOf(block),
                onEmptyBlockBackspaceClicked = { trigger++ }
            )

            recycler.adapter = adapter

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.textContent).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

                assertEquals(
                    expected = 0,
                    actual = trigger
                )
            }

            val updated = listOf(
                block.copy(
                    text = "",
                    mode = BlockView.Mode.EDIT,
                    cursor = 0,
                    isFocused = true
                )
            )

            adapter.updateWithDiffUtil(items = updated)

            R_test.id.recycler.rVMatcher().apply {

                onItemView(0, R.id.textContent).perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

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

    private fun givenParagraph() = BlockView.Text.Paragraph(
        mode = BlockView.Mode.READ,
        text = MockDataFactory.randomString(),
        id = MockDataFactory.randomUuid(),
        cursor = null
    )
}