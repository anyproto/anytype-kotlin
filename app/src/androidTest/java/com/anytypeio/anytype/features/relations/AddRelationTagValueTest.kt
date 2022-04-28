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
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.RelationOptionValueDVAddViewModel
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.test_utils.utils.type
import com.anytypeio.anytype.ui.relations.RelationOptionValueBaseAddFragment
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

    private lateinit var addRelationOption: AddDataViewRelationOption
    private lateinit var addObjectRelationOption: AddObjectRelationOption
    private lateinit var removeTagFromDataViewRecord: RemoveTagFromDataViewRecord
    private lateinit var addTagToDataViewRecord: AddTagToDataViewRecord
    private lateinit var addStatusToDataViewRecord: AddStatusToDataViewRecord
    private lateinit var updateDetail: UpdateDetail
    private lateinit var urlBuilder: UrlBuilder

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val ctx = MockDataFactory.randomUuid()
    private val state = MutableStateFlow(ObjectSet.init())
    private val session = ObjectSetSession()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addRelationOption = AddDataViewRelationOption(repo)
        addTagToDataViewRecord = AddTagToDataViewRecord(repo)
        addObjectRelationOption = AddObjectRelationOption(repo)
        addStatusToDataViewRecord = AddStatusToDataViewRecord(repo)
        removeTagFromDataViewRecord = RemoveTagFromDataViewRecord(repo)
        updateDetail = UpdateDetail(repo)
        urlBuilder = UrlBuilder(gateway)
        TestRelationOptionValueDVAddFragment.testVmFactory = RelationOptionValueDVAddViewModel.Factory(
            relations = DataViewObjectRelationProvider(state),
            values = DataViewObjectValueProvider(state, session),
            details = object : ObjectDetailProvider {
                override fun provide(): Map<Id, Block.Fields> = state.value.details
            },
            types = object : ObjectTypesProvider {
                override fun set(objectTypes: List<ObjectType>) {}
                override fun get(): List<ObjectType> = state.value.objectTypes
            },
            addDataViewRelationOption = addRelationOption,
            addTagToDataViewRecord = addTagToDataViewRecord,
            addStatusToDataViewRecord = addStatusToDataViewRecord,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            analytics = analytics
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

        repo.stub {
            onBlocking {
                addDataViewRelationOption(
                    ctx = any(),
                    dataview = any(),
                    relation = any(),
                    color = any(),
                    name = any(),
                    record = any()
                )
            } doReturn Pair(
                Payload(
                    context = ctx,
                    events = emptyList()
                ),
                MockDataFactory.randomUuid()
            )
        }

        // TESTING

        launchFragment(
            bundleOf(
                RelationOptionValueBaseAddFragment.CTX_KEY to ctx,
                RelationOptionValueBaseAddFragment.RELATION_KEY to relation.key,
                RelationOptionValueBaseAddFragment.DATAVIEW_KEY to dv.id,
                RelationOptionValueBaseAddFragment.VIEWER_KEY to viewer.id,
                RelationOptionValueBaseAddFragment.TARGET_KEY to obj
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

        verifyBlocking(repo, times(1)) {
            addDataViewRelationOption(
                ctx = any(),
                dataview = any(),
                relation = any(),
                color = any(),
                name = any(),
                record = any()
            )
        }
    }

    @Test
    fun shouldRenderOnlyTagsWhichRelationValueDoesNotContain() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = option1Color.title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = option2Color.title
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

        // TESTING

        launchFragment(
            bundleOf(
                RelationOptionValueBaseAddFragment.CTX_KEY to ctx,
                RelationOptionValueBaseAddFragment.RELATION_KEY to relation.key,
                RelationOptionValueBaseAddFragment.DATAVIEW_KEY to dv.id,
                RelationOptionValueBaseAddFragment.VIEWER_KEY to viewer.id,
                RelationOptionValueBaseAddFragment.TARGET_KEY to target
            )
        )

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.tvTagName).checkHasText(option1.text)
            onItemView(0, R.id.tvTagName).checkHasTextColor(option1Color.text)
            checkIsRecyclerSize(1)
        }
    }

    @Test
    fun tagsShouldBeInTheListWhenTypingToCreateNewOption() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = ThemeColor.values().random().title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = ThemeColor.values().random().title
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ThemeColor.values().random().title
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

        // TESTING

        launchFragment(
            bundleOf(
                RelationOptionValueBaseAddFragment.CTX_KEY to ctx,
                RelationOptionValueBaseAddFragment.RELATION_KEY to relation.key,
                RelationOptionValueBaseAddFragment.DATAVIEW_KEY to dv.id,
                RelationOptionValueBaseAddFragment.VIEWER_KEY to viewer.id,
                RelationOptionValueBaseAddFragment.TARGET_KEY to target
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
            color = ThemeColor.values().random().title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = ThemeColor.values().random().title
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ThemeColor.values().random().title
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

        // TESTING

        val scenario = launchFragment(
            bundleOf(
                RelationOptionValueBaseAddFragment.CTX_KEY to ctx,
                RelationOptionValueBaseAddFragment.RELATION_KEY to relation.key,
                RelationOptionValueBaseAddFragment.DATAVIEW_KEY to dv.id,
                RelationOptionValueBaseAddFragment.VIEWER_KEY to viewer.id,
                RelationOptionValueBaseAddFragment.TARGET_KEY to target
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

        R.id.tvSelectionCounter.matchView().checkHasText("2")

        R.id.btnAdd.performClick()

        verifyBlocking(repo, times(1)) {
            updateDataViewRecord(
                context = ctx,
                target = dv.id,
                record = target,
                values = mapOf(
                    ObjectSetConfig.ID_KEY to target,
                    relationKey to listOf(option1.id, option2.id)
                )
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationOptionValueDVAddFragment> {
        return launchFragmentInContainer<TestRelationOptionValueDVAddFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}