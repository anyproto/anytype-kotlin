package com.anytypeio.anytype.features.sets.sort

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.TestUtils.withRecyclerView
import com.anytypeio.anytype.ui.sets.modals.sort.ViewerSortFragment
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
class ViewerObjectSortTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var storeOfRelations: StoreOfRelations

    @Mock
    lateinit var dispatchers: AppCoroutineDispatchers

    private lateinit var updateDataViewViewer: UpdateDataViewViewer

    private val root = MockDataFactory.randomUuid()
    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val dispatcher = Dispatcher.Default<Payload>()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateDataViewViewer = UpdateDataViewViewer(repo, dispatchers)
        TestViewerSortFragment.testVmFactory = ViewerSortViewModel.Factory(
            state = state,
            updateDataViewViewer = updateDataViewViewer,
            dispatcher = dispatcher,
            analytics = analytics,
            storeOfRelations = storeOfRelations
        )
    }

    @Test
    fun shouldDisplayObjectRelationSortWithRelationNameAndSortingType() {

        // SETUP

        val name = "Author"

        val relationId = MockDataFactory.randomUuid()

        // Defining viewer containing one filter

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = listOf(
                DVSort(
                    relationKey = relationId,
                    type = Block.Content.DataView.Sort.Type.DESC
                )
            ),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = relationId,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.values().random()
        )

        val relation = Relation(
            key = relationId,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = name,
            source = Relation.Source.values().random(),
            format = Relation.Format.OBJECT,
            selections = emptyList()
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

        // Launching fragment

        launchFragment(bundleOf(ViewerSortFragment.CTX_KEY to root))

        // TESTING

        val rvMatcher = withRecyclerView(R.id.viewerSortRecycler)

        // Checking that the relation name is set

        onView(rvMatcher.atPositionOnView(0, R.id.tvTitle)).apply {
            check(matches(withText(name)))
        }

        // Checking that the sorting type is set

        onView(rvMatcher.atPositionOnView(0, R.id.tvSubtitle)).apply {
            check(matches(withText(R.string.sort_from_z_to_a)))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestViewerSortFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}