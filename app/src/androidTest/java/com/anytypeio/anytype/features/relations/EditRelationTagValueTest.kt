package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
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
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@RunWith(AndroidJUnit4::class)
@LargeTest
class EditRelationTagValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    private lateinit var updateDetail: UpdateDetail
    private lateinit var urlBuilder: UrlBuilder
    private lateinit var addFileToObject: AddFileToObject

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val ctx = MockDataFactory.randomUuid()
    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val store: ObjectStore = DefaultObjectStore()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val db = ObjectSetDatabase(store = store)
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateDetail = UpdateDetail(repo)
        urlBuilder = UrlBuilder(gateway)
        addFileToObject = AddFileToObject(repo)
        TestRelationValueDVFragment.testVmFactory = RelationValueDVViewModel.Factory(
            relations = DataViewObjectRelationProvider(
                objectState = state,
                storeOfRelations = storeOfRelations
            ),
            values = DataViewObjectValueProvider(db = db, objectState = state),
            details = object : ObjectDetailProvider {
                override fun provide(): Map<Id, Block.Fields> =
                    state.value.dataViewState()?.details.orEmpty()
            },
            urlBuilder = urlBuilder,
            copyFileToCache = copyFileToCacheDirectory,
            addFileToObject = addFileToObject,
            dispatcher = dispatcher,
            analytics = analytics,
            setObjectDetails = updateDetail,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    @Test
    fun shouldRenderTwoTagsInReadThenInEditMode() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = option2Color.code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ""
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
                RelationValueBaseFragment.CTX_KEY to ctx,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target,
                RelationValueBaseFragment.IS_LOCKED_KEY to false,
            )
        )

        val editOrDoneBtn = R.id.btnEditOrDone.matchView()

        editOrDoneBtn.checkHasText(R.string.edit)

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvTagName).checkHasText(option2.text)
            onItemView(0, R.id.tvTagName).checkHasTextColor(resources.dark(option2Color))
            onItemView(0, R.id.btnRemoveTag).checkIsNotDisplayed()
            onItemView(0, R.id.btnDragAndDropTag).checkIsNotDisplayed()
            onItemView(1, R.id.tvTagName).checkHasText(option3.text)
            onItemView(1, R.id.btnRemoveTag).checkIsNotDisplayed()
            onItemView(1, R.id.btnDragAndDropTag).checkIsNotDisplayed()
            checkIsRecyclerSize(2)
        }

        editOrDoneBtn.performClick()

        editOrDoneBtn.checkHasText(R.string.done)

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvTagName).checkHasText(option2.text)
            onItemView(0, R.id.tvTagName).checkHasTextColor(resources.dark(option2Color))
            onItemView(0, R.id.btnRemoveTag).checkIsDisplayed()
            onItemView(0, R.id.btnDragAndDropTag).checkIsDisplayed()
            onItemView(1, R.id.tvTagName).checkHasText(option3.text)
            onItemView(1, R.id.btnRemoveTag).checkIsDisplayed()
            onItemView(1, R.id.btnDragAndDropTag).checkIsDisplayed()
            checkIsRecyclerSize(2)
        }
    }

    @Test
    fun shouldRequestRemovingLastTagWhenRemoveButtonPressed() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
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

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                viewers = listOf(viewer),

                )
        )

        state.value = ObjectState.DataView.Set(
            blocks = listOf(dv)
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to ctx,
                RelationValueBaseFragment.DV_KEY to dv.id,
                RelationValueBaseFragment.VIEWER_KEY to viewer.id,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target,
                RelationValueBaseFragment.IS_LOCKED_KEY to false,
            )
        )

        R.id.btnEditOrDone.performClick()

        val rvMatcher = R.id.recycler.rVMatcher()

        rvMatcher.onItemView(1, R.id.btnRemoveTag).performClick()

        verifyBlocking(repo, times(1)) {
            setObjectDetail(
                ctx = target,
                key = relationKey,
                value = listOf(option2.id)
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationValueDVFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}