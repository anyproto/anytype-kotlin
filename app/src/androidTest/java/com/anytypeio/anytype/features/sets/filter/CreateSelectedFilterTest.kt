package com.anytypeio.anytype.features.sets.filter

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.utils.TestUtils
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFromSelectedValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class CreateSelectedFilterTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var analytics: Analytics

    lateinit var updateDataViewViewer: UpdateDataViewViewer
    lateinit var searchObjects: SearchObjects
    lateinit var urlBuilder: UrlBuilder

    private val root = MockDataFactory.randomUuid()
    private val session = ObjectSetSession()
    private val state = MutableStateFlow(ObjectSet.init())
    private val dispatcher = Dispatcher.Default<Payload>()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateDataViewViewer = UpdateDataViewViewer(repo)
        searchObjects = SearchObjects(repo)
        urlBuilder = UrlBuilder(gateway)
        TestCreateSelectedFilterFragment.testVmFactory = FilterViewModel.Factory(
            session = session,
            updateDataViewViewer = updateDataViewViewer,
            dispatcher = dispatcher,
            urlBuilder = urlBuilder,
            searchObjects = searchObjects,
            objectSetState = state,
            objectTypesProvider = objectTypesProvider,
            analytics = analytics
        )
    }

    @Test
    fun shouldShowNonSelectedCheckedAndSelectedNotChecked() {
        val relationKey = MockDataFactory.randomUuid()

        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        // Defining viewer containing one filter

        val filter = DVFilter(
            relationKey = relationKey,
            value = true,
            operator = Block.Content.DataView.Filter.Operator.AND,
            condition = DVFilterCondition.EQUAL
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = listOf(filter),
            sorts = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = relationKey,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.values().random()
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = "Is read",
            source = Relation.Source.values().random(),
            format = Relation.Format.CHECKBOX,
            selections = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                sources = listOf(
                    MockDataFactory.randomUuid()
                )
            )
        )

        state.value = ObjectSet(
            blocks = listOf(dv),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // Launching fragment

        launchFragment(
            bundleOf(
                CreateFilterFromSelectedValueFragment.CTX_KEY to root,
                CreateFilterFromSelectedValueFragment.RELATION_KEY to relationKey
            )
        )

        val rvMatcher = TestUtils.withRecyclerView(R.id.rvViewerFilterRecycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvCheckbox)).apply {
            checkHasText(R.string.dv_filter_checkbox_checked)
        }
        onView(rvMatcher.atPositionOnView(0, R.id.iconChecked)).apply {
            checkIsNotDisplayed()
        }

        onView(rvMatcher.atPositionOnView(1, R.id.tvCheckbox)).apply {
            checkHasText(R.string.dv_filter_checkbox_not_checked)
        }
        onView(rvMatcher.atPositionOnView(1, R.id.iconChecked)).apply {
            checkIsDisplayed()
        }
    }

    @Test
    fun shouldChangeStateFromNotCheckedToChecked() {
        val relationKey = MockDataFactory.randomUuid()

        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        // Defining viewer containing one filter

        val filter = DVFilter(
            relationKey = relationKey,
            value = true,
            operator = Block.Content.DataView.Filter.Operator.AND,
            condition = DVFilterCondition.EQUAL
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = listOf(filter),
            sorts = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = relationKey,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.values().random()
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = "Is read",
            source = Relation.Source.values().random(),
            format = Relation.Format.CHECKBOX,
            selections = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                sources = listOf(
                    MockDataFactory.randomUuid()
                )
            )
        )

        state.value = ObjectSet(
            blocks = listOf(dv),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // Launching fragment

        launchFragment(
            bundleOf(
                CreateFilterFromSelectedValueFragment.CTX_KEY to root,
                CreateFilterFromSelectedValueFragment.RELATION_KEY to relationKey
            )
        )

        val rvMatcher = TestUtils.withRecyclerView(R.id.rvViewerFilterRecycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvCheckbox)).performClick()

        onView(rvMatcher.atPositionOnView(0, R.id.iconChecked)).apply {
            checkIsDisplayed()
        }
        onView(rvMatcher.atPositionOnView(1, R.id.iconChecked)).apply {
            checkIsNotDisplayed()
        }

        onView(rvMatcher.atPositionOnView(1, R.id.tvCheckbox)).performClick()

        onView(rvMatcher.atPositionOnView(0, R.id.iconChecked)).apply {
            checkIsNotDisplayed()
        }
        onView(rvMatcher.atPositionOnView(1, R.id.iconChecked)).apply {
            checkIsDisplayed()
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestCreateSelectedFilterFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}