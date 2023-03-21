package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.const.DateConst.DEFAULT_DATE_FORMAT
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectRelationDateValueTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val root = MockDataFactory.randomUuid()

    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val store: ObjectStore = DefaultObjectStore()
    private val db = ObjectSetDatabase(store)
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        TestRelationDateValueFragment.testVmFactory = RelationDateValueViewModel.Factory(
            relations = DataViewObjectRelationProvider(
                objectState = state,
                storeOfRelations = storeOfRelations
            ),
            values = DataViewObjectValueProvider(db = db, objectState = state)
        )
    }

    @Test
    fun shouldSetNoDateValue() {

        // SETUP

        val relationText = "Birth date"
        val valueDate: Long? = null

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
            format = Relation.Format.DATE,
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
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivNoDateCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetTodayDateValue() {

        // SETUP

        val relationText = "Birth date"
        val valueDate: Long = System.currentTimeMillis() / 1000

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
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
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
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivTodayCheck)).check(matches(isDisplayed()))
        onView(withId(R.id.ivNoDateCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetTomorrowDateValue() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
        val valueDate: Long = calendar.timeInMillis / 1000

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
            format = Relation.Format.DATE,
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
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivNoDateCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetYesterdayDateValue() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val valueDate: Long = calendar.timeInMillis / 1000

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
            format = Relation.Format.DATE,
            source = Relation.Source.values().random()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relation.key to valueDate
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
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        onView(withId(R.id.ivNoDateCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    @Test
    fun shouldSetExactDayDateValue() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 45) }
        val valueDate: Long = calendar.timeInMillis / 1000

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
            format = Relation.Format.DATE,
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
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        val exactDateFormat = valueDate.formatTimeInMillis(DEFAULT_DATE_FORMAT)
        onView(withId(R.id.ivNoDateCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(exactDateFormat)))
    }

    @Test
    fun shouldSetExactDayDateValueAndThenUpdateWithDatePicker() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 45) }
        val valueDate: Long = calendar.timeInMillis / 1000

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
            format = Relation.Format.DATE,
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

        val fragment = launchFragment(
            bundleOf(
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        val exactDateFormat = valueDate.formatTimeInMillis(DEFAULT_DATE_FORMAT)
        onView(withId(R.id.ivNoDateCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(exactDateFormat)))

        val dateUpdate = Calendar.getInstance().apply { add(Calendar.DATE, 17) }
        val valueUpdate: Long = dateUpdate.timeInMillis / 1000
        fragment.onFragment {
            it.onPickDate(valueUpdate)
        }

        val updatedDateFormat = valueUpdate.formatTimeInMillis(DEFAULT_DATE_FORMAT)
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(updatedDateFormat)))
    }

    @Test
    fun shouldSetExactDayDateValueAndThenUpdateToTodayTomorrowAndYesterday() {

        // SETUP

        val relationText = "Birth date"
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 45) }
        val valueDate: Long = calendar.timeInMillis / 1000

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
            format = Relation.Format.DATE,
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

        val fragment = launchFragment(
            bundleOf(
                RelationDateValueFragment.CONTEXT_ID to root,
                RelationDateValueFragment.RELATION_KEY to relation.key,
                RelationDateValueFragment.OBJECT_ID to target,
                RelationDateValueFragment.FLOW_KEY to RelationDateValueFragment.FLOW_DATAVIEW
            )
        )

        val exactDateFormat = valueDate.formatTimeInMillis(DEFAULT_DATE_FORMAT)
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText(exactDateFormat)))

        fragment.onFragment { it.vm.onTodayClicked() }

        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))

        fragment.onFragment { it.vm.onTomorrowClicked() }

        onView(withId(R.id.ivTomorrowCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))

        fragment.onFragment { it.vm.onYesterdayClicked() }

        onView(withId(R.id.ivNoDateCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTomorrowCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivTodayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivYesterdayCheck)).check(matches((isDisplayed())))
        onView(withId(R.id.ivExactDayCheck)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tvDate)).check(matches(withText("")))
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationDateValueFragment> {
        return launchFragmentInContainer<TestRelationDateValueFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}