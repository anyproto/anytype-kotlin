package com.anytypeio.anytype.features.relations

import android.os.Bundle
import android.text.InputType
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withInputType
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ReloadObject
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.espresso.TextLineCountMatcher
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class DisplayObjectRelationTextValueTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var reloadObject: ReloadObject

    @Mock
    lateinit var analytics: Analytics

    val root = MockDataFactory.randomUuid()

    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val store: ObjectStore = DefaultObjectStore()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val db = ObjectSetDatabase(store = store)

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        TestRelationTextValueFragment.testVmFactory = RelationTextValueViewModel.Factory(
            relations = DataViewObjectRelationProvider(
                objectState = state,
                storeOfRelations = storeOfRelations
            ),
            values = DataViewObjectValueProvider(db = db, objectState = state),
            reloadObject = reloadObject,
            analytics = analytics
        )
    }

    @Test
    fun shouldSetDescriptionTextAndNotDisplayActionButton() {

        // SETUP

        val relationText = "Architect"
        val valueText = "Filippo Brunelleschi"

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        state.value = ObjectState.DataView.Set(
            root = root,
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        viewers = listOf(viewer)
                    )
                )
            )
        )

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relation.key,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to RelationTextValueFragment.FLOW_DATAVIEW
            )
        )

        // TESTING

        onView(withId(R.id.textInputField)).apply {
            check(matches(withText(valueText)))
        }

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(relationText)))
        }

        onView(withId(R.id.btnAction)).apply {
            check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun shouldSetNumberTextAndNotDisplayActionButton() {

        // SETUP

        val relationText = "Year"
        val valueText = "1446"

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        state.value = ObjectState.DataView.Set(
            root = root,
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        viewers = listOf(viewer)
                    )
                )
            )
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relation.key,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to RelationTextValueFragment.FLOW_DATAVIEW
            )
        )

        // TESTING

        onView(withId(R.id.textInputField)).apply {
            check(matches(withText(valueText)))
        }

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(relationText)))
        }

        onView(withId(R.id.btnAction)).apply {
            check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun shouldSetPhoneTextAndDisplayActionButton() {

        // SETUP

        val relationText = "Phone number"
        val valueText = "+ 124242423423"

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.PHONE,
            source = Relation.Source.values().random()
        )

        state.value = ObjectState.DataView.Set(
            root = root,
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        viewers = listOf(viewer)
                    )
                )
            )
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relation.key,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to RelationTextValueFragment.FLOW_DATAVIEW
            )
        )

        // TESTING

        onView(withId(R.id.textInputField)).apply {
            check(matches(withText(valueText)))
        }

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(relationText)))
        }

        onView(withId(R.id.btnAction)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldSetEmailTextAndDisplayActionButton() {

        // SETUP

        val relationText = "Email"
        val valueText = "foo.bar@foobar.com"

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.EMAIL,
            source = Relation.Source.values().random()
        )

        state.value = ObjectState.DataView.Set(
            root = root,
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        viewers = listOf(viewer)
                    )
                )
            )
        )

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relation.key,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to RelationTextValueFragment.FLOW_DATAVIEW
            )
        )

        // TESTING

        onView(withId(R.id.textInputField)).apply {
            check(matches(withText(valueText)))
        }

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(relationText)))
        }

        onView(withId(R.id.btnAction)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldSetUrlTextAndDisplayActionButton() {

        // SETUP

        val relationText = "Url"
        val valueText = "https://anytype.io"

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.URL,
            source = Relation.Source.values().random()
        )

        state.value = ObjectState.DataView.Set(
            root = root,
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        viewers = listOf(viewer)
                    )
                )
            )
        )

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relation.key,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to RelationTextValueFragment.FLOW_DATAVIEW
            )
        )

        // TESTING

        onView(withId(R.id.textInputField)).apply {
            check(matches(withText(valueText)))
        }

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(relationText)))
        }

        onView(withId(R.id.btnAction)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldDisplayShortText() {

        // SETUP

        val relationText = "Short text"
        val valueText =
            "Anytype is a next generation software that breaks down barriers between applications, gives back privacy and data ownership to users."

        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = relationText,
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        state.value = ObjectState.DataView.Set(
            root = root,
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        viewers = listOf(viewer),

                    )
                )
            )
        )

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relation.key,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to RelationTextValueFragment.FLOW_DATAVIEW
            )
        )

        // TESTING

        onView(withId(R.id.textInputField)).apply {
            check(matches(withText(valueText)))
            check(matches(withInputType(InputType.TYPE_CLASS_TEXT)))
            check(matches(TextLineCountMatcher(1)))
        }

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(relationText)))
        }

        onView(withId(R.id.btnAction)).apply {
            check(matches(not(isDisplayed())))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationTextValueFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}