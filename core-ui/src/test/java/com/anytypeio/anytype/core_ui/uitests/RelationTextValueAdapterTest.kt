package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.os.Build
import android.text.InputType
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.relations.RelationTextValueAdapter
import com.anytypeio.anytype.presentation.sets.RelationTextValueView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.checkHasHintText
import com.anytypeio.anytype.test_utils.utils.checkHasInputType
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsNotFocused
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
class RelationTextValueAdapterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `text input should not be editable - when relation with long-text format is not editable`() {
        checkTextInputIsNotEditableWhenRelationIsNotEditable(
            RelationTextValueView.Text(
                isEditable = false,
                value = MockDataFactory.randomString()
            )
        )
    }

    @Test
    fun `text input should not be editable - when relation with number format is not editable`() {
        checkTextInputIsNotEditableWhenRelationIsNotEditable(
            RelationTextValueView.Number(
                isEditable = false,
                value = MockDataFactory.randomString()
            )
        )
    }

    @Test
    fun `text input should not be editable - when relation with email format is not editable`() {
        checkTextInputIsNotEditableWhenRelationIsNotEditable(
            RelationTextValueView.Email(
                isEditable = false,
                value = MockDataFactory.randomString()
            )
        )
    }

    @Test
    fun `text input should not be editable - when relation with url format is not editable`() {
        checkTextInputIsNotEditableWhenRelationIsNotEditable(
            RelationTextValueView.Url(
                isEditable = false,
                value = MockDataFactory.randomString()
            )
        )
    }

    @Test
    fun `text input should not be editable - when relation with phone format is not editable`() {
        checkTextInputIsNotEditableWhenRelationIsNotEditable(
            RelationTextValueView.Phone(
                isEditable = false,
                value = MockDataFactory.randomString()
            )
        )
    }

    @Test
    fun `text input should not be editable - when relation with short-text format is not editable`() {
        checkTextInputIsNotEditableWhenRelationIsNotEditable(
            RelationTextValueView.TextShort(
                isEditable = false,
                value = MockDataFactory.randomString()
            )
        )
    }

    @Test
    fun `text input show empty-content hint - when relation with short-text format is not editable and empty`() {
        textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
            RelationTextValueView.TextShort(
                isEditable = false,
                value = null
            )
        )
    }

    @Test
    fun `text input show empty-content hint - when relation with long-text format is not editable and empty`() {
        textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
            RelationTextValueView.Text(
                isEditable = false,
                value = null
            )
        )
    }

    @Test
    fun `text input show empty-content hint - when relation with number format is not editable and empty`() {
        textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
            RelationTextValueView.Number(
                isEditable = false,
                value = null
            )
        )
    }

    @Test
    fun `text input show empty-content hint - when relation with phone format is not editable and empty`() {
        textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
            RelationTextValueView.Phone(
                isEditable = false,
                value = null
            )
        )
    }

    @Test
    fun `text input show empty-content hint - when relation with email format is not editable and empty`() {
        textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
            RelationTextValueView.Email(
                isEditable = false,
                value = null
            )
        )
    }

    @Test
    fun `text input show empty-content hint - when relation with url format is not editable and empty`() {
        textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
            RelationTextValueView.Url(
                isEditable = false,
                value = null
            )
        )
    }

    //region Scenarios

    private fun checkTextInputIsNotEditableWhenRelationIsNotEditable(
        item: RelationTextValueView
    ) {
        scenario.onFragment { fragment ->

            // SETUP

            val recycler = givenRecycler(fragment)

            val adapter = givenAdapter(item)

            recycler.adapter = adapter

            // TESTING

            TestResource.id.recycler.rVMatcher().apply {
                onItemView(0, R.id.textInputField).checkHasText(item.value!!)
                onItemView(0, R.id.textInputField).checkIsNotFocused()
                onItemView(0, R.id.textInputField).checkHasInputType(InputType.TYPE_NULL)
            }
        }
    }

    private fun textInputFieldShouldHaveSpecificHintWhenValueIsMissingAndNotEditable(
        item: RelationTextValueView
    ) {
        scenario.onFragment { fragment ->

            // SETUP

            val recycler = givenRecycler(fragment)

            val adapter = givenAdapter(item)

            recycler.adapter = adapter

            // TESTING

            TestResource.id.recycler.rVMatcher().apply {
                onItemView(0, R.id.textInputField).checkHasText("")
                onItemView(0, R.id.textInputField).checkHasInputType(InputType.TYPE_NULL)
                onItemView(0, R.id.textInputField).checkHasHintText(R.string.empty)
            }
        }
    }

    //endregion

    private fun givenRecycler(fr: Fragment): RecyclerView {
        val root = checkNotNull(fr.view)
        return root.findViewById<RecyclerView>(TestResource.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun givenAdapter(item: RelationTextValueView) = RelationTextValueAdapter(
        onEditCompleted = { view, txt -> },
        actionClick = {},
        items = listOf(item)
    )
}