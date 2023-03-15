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
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.TestUtils.withRecyclerView
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.espresso.WithTextColorRes
import com.anytypeio.anytype.test_utils.utils.resources
import com.anytypeio.anytype.ui.relations.RelationValueBaseFragment
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
class DisplayRelationStatusValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    private lateinit var updateDetail: UpdateDetail
    private lateinit var addFileToObject: AddFileToObject
    private lateinit var urlBuilder: UrlBuilder

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val root = MockDataFactory.randomUuid()
    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val store: ObjectStore = DefaultObjectStore()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()
    private val db = ObjectSetDatabase(store)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateDetail = UpdateDetail(repo)
        addFileToObject = AddFileToObject(repo)
        urlBuilder = UrlBuilder(gateway)
        TestRelationValueDVFragment.testVmFactory = RelationValueDVViewModel.Factory(
            relations = DataViewObjectRelationProvider(
                objectState = state,
                storeOfRelations = storeOfRelations
            ),
            values = DataViewObjectValueProvider(
                db = db,
                objectState = state
            ),
            details = object : ObjectDetailProvider {
                override fun provide(): Map<Id, Block.Fields> =
                    state.value.dataViewState()?.details.orEmpty()
            },
            urlBuilder = urlBuilder,
            copyFileToCache = copyFileToCacheDirectory,
            dispatcher = dispatcher,
            analytics = analytics,
            setObjectDetails = updateDetail,
            addFileToObject = addFileToObject,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    @Test
    fun shouldDisplayEditButtonAndPlusButton() {
        // SETUP

        val relation = MockDataFactory.randomUuid()
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
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relation,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        // Checking that the buttons are invisible

        onView(withId(R.id.btnEditOrDone)).apply {
            check(matches(isDisplayed()))
        }

        onView(withId(R.id.btnAddValue)).apply {
            check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldSetRelationName() {

        // SETUP

        val name = "Status"

        val relation = MockDataFactory.randomUuid()
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
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relation,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        // Checking that the name is set

        onView(withId(R.id.tvRelationHeader)).apply {
            check(matches(withText(name)))
        }
    }

    @Test
    fun shouldRenderStatusFromDV() {

        // SETUP

        val option2Color = ThemeColor.values().random()

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = option2Color.code
        )

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
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvStatusName)).apply {
            checkHasText(option2.text)
            checkHasTextColor(resources.dark(option2Color))
        }
    }

    @Test
    fun shouldRenderStatusWithDefaultColor() {

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
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvStatusName)).apply {
            check(matches(WithTextColorRes(com.anytypeio.anytype.core_ui.R.color.text_primary)))
        }
    }

    @Test
    fun shouldRenderEmptyState() {

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
                RelationValueBaseFragment.CTX_KEY to root,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        val rvMatcher = withRecyclerView(R.id.recycler)

        onView(rvMatcher.atPositionOnView(0, R.id.tvEmptyMessage)).apply {
            check(matches(isDisplayed()))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationValueDVFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}