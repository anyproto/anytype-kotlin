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
import com.anytypeio.anytype.core_models.StubRelationOptionObject
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.test_utils.utils.resources
import com.anytypeio.anytype.test_utils.utils.type
import com.anytypeio.anytype.ui.relations.add.BaseAddOptionsRelationFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddRelationTagValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var objectDetailProvider: ObjectDetailProvider

    private lateinit var createRelationOption: CreateRelationOption
    private lateinit var updateDetail: UpdateDetail
    private lateinit var getOptions: GetOptions
    private lateinit var urlBuilder: UrlBuilder

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val ctx = MockDataFactory.randomUuid()
    private val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val store : ObjectStore = DefaultObjectStore()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val db = ObjectSetDatabase(store = store)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        createRelationOption = CreateRelationOption(repo)
        getOptions = GetOptions(repo)
        updateDetail = UpdateDetail(repo)
        urlBuilder = UrlBuilder(gateway)
        TestRelationOptionValueDVAddFragment.testVmFactory = AddOptionsRelationDVViewModel.Factory(
            relations = DataViewObjectRelationProvider(
                objectState = state,
                storeOfRelations = storeOfRelations
            ),
            values = DataViewObjectValueProvider(db = db, objectState = state),
            createRelationOption = createRelationOption,
            dispatcher = dispatcher,
            optionsProvider = AddOptionsRelationProvider(),
            analytics = analytics,
            setObjectDetail = updateDetail,
            detailsProvider = objectDetailProvider,
            getOptions = getOptions
        )
    }

    @Test
    fun shouldStartCreatingDataViewOptionWhenTypeAndButtonClicked() {

        // SETUP

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = MockDataFactory.randomString(),
            source = Relation.Source.values().random(),
            format = Relation.Format.TAG,
            selections = emptyList()
        )

        val obj = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to obj,
            relation.key to emptyList<Id>()
        )

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
                viewers = listOf(viewer)
            )
        )

        state.value = ObjectState.DataView.Set(root = ctx,blocks = listOf(dv))

        stubCreateRelationOption()

        // TESTING

        launchFragment(
            bundleOf(
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to obj
            )
        )

        // Creating name for a new option

        R.id.filterInputField.type("Writer")

        val btn = R.id.recycler.rVMatcher().onItemView(0, R.id.tvCreateOptionValue)

        btn.checkHasText("Create option \"Writer\"")

        // Pressing button, in order to trigger request.

        Thread.sleep(100)

        btn.performClick()

        Thread.sleep(100)

        // Verifying that the request is made.

        verifyCreateRelationOptionCalled()
    }

    @Test
    fun shouldRenderOnlyTagsWhichRelationValueDoesNotContain() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = option1Color.code
        )

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

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to listOf(option2.id, option3.id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = "Roles",
            source = Relation.Source.values().random(),
            format = Relation.Format.TAG,
            selections = listOf(option1, option2, option3)
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                viewers = listOf(viewer)
            )
        )

        state.value = ObjectState.DataView.Set(root = ctx, blocks = listOf(dv))

        // TESTING

        launchFragment(
            bundleOf(
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to target
            )
        )

        R.id.recycler.rVMatcher().apply {
            checkIsRecyclerSize(1)
            onItemView(0, R.id.tvTagName).checkHasText(option1.text)
            onItemView(0, R.id.tvTagName).checkHasTextColor(resources.dark(option1Color))
        }
    }

    @Test
    fun tagsShouldBeInTheListWhenTypingToCreateNewOption() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = ThemeColor.values().random().code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = ThemeColor.values().random().code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ThemeColor.values().random().code
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = "Roles",
            source = Relation.Source.values().random(),
            format = Relation.Format.TAG,
            selections = listOf(option1, option2, option3)
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                viewers = listOf(viewer)
            )
        )

        state.value = ObjectState.DataView.Set(root = ctx, blocks = listOf(dv))

        // TESTING

        launchFragment(
            bundleOf(
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to target
            )
        )

        // Typing name for a new option

        val textToType = "a"

        R.id.filterInputField.type(textToType)

        // Checking that not only create-option view button, but also tags are visible

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.tvCreateOptionValue).checkHasText("Create option \"$textToType\"")
            onItemView(1, R.id.tvTagName).checkHasText(option1.text)
            onItemView(2, R.id.tvTagName).checkHasText(option2.text)
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldSelectFirstTwoTagsUpdateCounterAndRequestAddingThisTagToDataViewRecord() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = ThemeColor.values().random().code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = ThemeColor.values().random().code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ThemeColor.values().random().code
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = "Roles",
            source = Relation.Source.values().random(),
            format = Relation.Format.TAG,
            selections = listOf(option1, option2, option3)
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                viewers = listOf(viewer)
            )
        )

        state.value = ObjectState.DataView.Set(root = ctx, blocks = listOf(dv))

        // TESTING

        val scenario = launchFragment(
            bundleOf(
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to target
            )
        )


        // Selecting the first two tags

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.tvTagName).performClick()
            onItemView(1, R.id.tvTagName).performClick()
        }

        // Clicking twice on the last tag, in order to check select / unselect logic.

        R.id.recycler.rVMatcher().apply {
            onItemView(2, R.id.tvTagName).performClick()
            onItemView(2, R.id.tvTagName).performClick()
        }

        // Veryfying UI

        R.id.btnAdd.matchView().checkHasText("2")

        R.id.btnAdd.performClick()

        verifyBlocking(repo, times(1)) {
            setObjectDetail(
                ctx = target,
                key = relationKey,
                value = listOf(option1.id, option2.id)
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationOptionValueDVAddFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    private fun verifyCreateRelationOptionCalled() {
        verifyBlocking(repo, times(1)) {
            createRelationOption(
                relation = any(),
                color = any(),
                name = any()
            )
        }
    }

    private fun stubCreateRelationOption(
        name: String = MockDataFactory.randomString(),
        id: Id = MockDataFactory.randomUuid(),
        color: String = ThemeColor.values().random().code
    ) {
        repo.stub {
            onBlocking {
                createRelationOption(
                    relation = any(),
                    color = any(),
                    name = any()
                )
            } doReturn StubRelationOptionObject(
                id = id,
                text = name,
                color = color
            )
        }
    }
}