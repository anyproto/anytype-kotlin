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
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
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
class ToggleBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should show toggle create-block-button - when toggle is toggled and empty`() {
        scenario.onFragment {

            // SETUP

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(
                listOf(
                    givenToggleBlock(
                        isToggled = true,
                        isEmpty = true,
                        mode = BlockView.Mode.EDIT
                    )
                )
            )
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsDisplayed()
        }
    }

    @Test
    fun `should show toggle create-block-button - when toggle is toggled and empty after change payload`() {
        scenario.onFragment {

            // SETUP

            val initialToggle = givenToggleBlock(
                isToggled = false,
                isEmpty = false
            )
            val changedToggle = initialToggle.copy(
                toggled = true,
                isEmpty = true
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(initialToggle))
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()

            adapter.updateWithDiffUtil(listOf(changedToggle))

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsDisplayed()
        }
    }

    @Test
    fun `should not show toggle create-block-button - when toggle is toggled but not empty`() {
        scenario.onFragment {

            // SETUP

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(
                listOf(
                    givenToggleBlock(
                        isToggled = true,
                        isEmpty = false
                    )
                )
            )
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()
        }
    }

    @Test
    fun `should show toggle create-block-button - when toggled toggle block is empty after change payload`() {
        scenario.onFragment {

            // SETUP

            val initialToggle = givenToggleBlock(
                isToggled = true,
                isEmpty = false
            )

            val changedToggle = initialToggle.copy(
                isEmpty = true
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(initialToggle))
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()

            adapter.updateWithDiffUtil(listOf(changedToggle))

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsDisplayed()
        }
    }

    @Test
    fun `should not show toggle create-block-button - when toggle is not toggled but empty`() {
        scenario.onFragment {

            // SETUP

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(
                listOf(
                    givenToggleBlock(
                        isToggled = false,
                        isEmpty = true
                    )
                )
            )
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()
        }
    }

    @Test
    fun `should not show toggle create-block-button - when toggle is toggled and empty but in read mode`() {
        scenario.onFragment {

            // SETUP

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(
                listOf(
                    givenToggleBlock(
                        isToggled = true,
                        isEmpty = true,
                        mode = BlockView.Mode.READ
                    )
                )
            )
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()
        }
    }

    @Test
    fun `should hide toggle create-block-button - when toggled and empty toggle block enters read mode after change payload`() {
        scenario.onFragment {

            // SETUP

            val initialToggle = givenToggleBlock(
                isToggled = true,
                isEmpty = true,
                mode = BlockView.Mode.EDIT
            )

            val changedToggle = initialToggle.copy(
                mode = BlockView.Mode.READ
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(initialToggle))
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsDisplayed()

            adapter.updateWithDiffUtil(listOf(changedToggle))

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()
        }
    }

    @Test
    fun `should show toggle create-block-button - when toggled and empty toggle block enters edit mode after change payload`() {
        scenario.onFragment {

            // SETUP

            val initialToggle = givenToggleBlock(
                isToggled = true,
                isEmpty = true,
                mode = BlockView.Mode.READ
            )

            val changedToggle = initialToggle.copy(
                mode = BlockView.Mode.EDIT
            )

            val recycler = givenRecycler(it)
            val adapter = givenAdapter(listOf(initialToggle))
            recycler.adapter = adapter

            val rvMatcher = TestResource.id.recycler.rVMatcher()

            // TESTING

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsNotDisplayed()

            adapter.updateWithDiffUtil(listOf(changedToggle))

            rvMatcher.onItemView(0, R.id.togglePlaceholder).checkIsDisplayed()
        }
    }

    private fun givenToggleBlock(
        isToggled: Boolean = false,
        isEmpty: Boolean = false,
        isFocused: Boolean = false,
        mode: BlockView.Mode = BlockView.Mode.EDIT,
        indent: Int = 0
    ) = BlockView.Text.Toggle(
        text = MockDataFactory.randomString(),
        id = MockDataFactory.randomUuid(),
        mode = mode,
        indent = indent,
        isEmpty = isEmpty,
        isFocused = isFocused,
        toggled = isToggled
    )

    private fun givenRecycler(fr: Fragment): RecyclerView {
        val root = checkNotNull(fr.view)
        return root.findViewById<RecyclerView>(TestResource.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
    }
}