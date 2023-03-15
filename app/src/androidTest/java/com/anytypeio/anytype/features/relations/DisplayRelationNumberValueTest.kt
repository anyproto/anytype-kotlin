package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
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
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.TestUtils
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment.Companion.FLOW_DATAVIEW
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
class DisplayRelationNumberValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var reloadObject: ReloadObject

    @Mock
    lateinit var analytics: Analytics

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val root = MockDataFactory.randomUuid()
    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val store: ObjectStore = DefaultObjectStore()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val db = ObjectSetDatabase(store = store)

    @Before
    fun setup() {
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
    fun shouldRenderDoubleNumberValue() {

        // SETUP

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectState.DataView.Set(
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

        // TESTING

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relationKey,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to FLOW_DATAVIEW,
                RelationTextValueFragment.LOCKED_KEY to DEFAULT_IS_LOCKED
            )
        )

        val rvMatcher = TestUtils.withRecyclerView(R.id.recycler)

        Espresso.onView(rvMatcher.atPositionOnView(0, R.id.textInputField)).apply {
            check(ViewAssertions.matches(withText("345.09")))
        }
    }

    @Test
    fun shouldRenderLongNumberValue() {

        // SETUP

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectState.DataView.Set(
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

        // TESTING

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relationKey,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to FLOW_DATAVIEW,
                RelationTextValueFragment.LOCKED_KEY to DEFAULT_IS_LOCKED
            )
        )

        val rvMatcher = TestUtils.withRecyclerView(R.id.recycler)

        Espresso.onView(rvMatcher.atPositionOnView(0, R.id.textInputField)).apply {
            check(ViewAssertions.matches(withText("345")))
        }
    }

    @Test
    fun shouldRenderNumberValue() {

        // SETUP

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectState.DataView.Set(
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

        // TESTING

        launchFragment(
            bundleOf(
                RelationTextValueFragment.CONTEXT_ID to root,
                RelationTextValueFragment.RELATION_KEY to relationKey,
                RelationTextValueFragment.OBJECT_ID to target,
                RelationTextValueFragment.FLOW_KEY to FLOW_DATAVIEW,
                RelationTextValueFragment.LOCKED_KEY to DEFAULT_IS_LOCKED
            )
        )

        val rvMatcher = TestUtils.withRecyclerView(R.id.recycler)

        Espresso.onView(rvMatcher.atPositionOnView(0, R.id.textInputField)).apply {
            check(ViewAssertions.matches(withText("")))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationTextValueFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    companion object {
        const val DEFAULT_IS_LOCKED = false
    }
}