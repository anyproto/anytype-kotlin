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
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
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
class AddRelationStatusValueTest {

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
        TestRelationOptionValueDVAddFragment.testVmFactory = AddOptionsRelationDVViewModel.Factory(
            relations = DataViewObjectRelationProvider(state),
            values = DataViewObjectValueProvider(state, session),
            addDataViewRelationOption = addRelationOption,
            addTagToDataViewRecord = addTagToDataViewRecord,
            addStatusToDataViewRecord = addStatusToDataViewRecord,
            dispatcher = dispatcher,
            optionsProvider = AddOptionsRelationProvider()
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
            format = Relation.Format.STATUS,
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
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to obj
            )
        )

        // Creating name for a new option

        R.id.filterInputField.type("In progress")

        Thread.sleep(1000)

        val btn = R.id.recycler.rVMatcher().onItemView(0, R.id.tvCreateOptionValue)

        btn.checkHasText("Create option \"In progress\"")

        // Pressing button, in order to trigger request.

        btn.performClick()

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
    fun addButtonShouldNotBeVisible() {

        // SETUP

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = MockDataFactory.randomString(),
            source = Relation.Source.values().random(),
            format = Relation.Format.STATUS,
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
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to obj
            )
        )

        R.id.btnAdd.matchView().checkIsNotDisplayed()
    }

    @Test
    fun shouldRenderOnlyStatusesWhichRelationValueDoesNotContain() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In progress",
            color = option1Color.code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = option2Color.code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Development",
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
            format = Relation.Format.STATUS,
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
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to target
            )
        )

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.tvStatusName).checkHasText(option1.text)
            onItemView(0, R.id.tvStatusName).checkHasTextColor(option1Color.text)
            checkIsRecyclerSize(1)
        }
    }

    @Test
    fun matchedStatusesShouldBeInTheListWhenTypingToCreateNewOption() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In Testing",
            color = ThemeColor.values().random().code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = ThemeColor.values().random().code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In Development",
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
            format = Relation.Format.STATUS,
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
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to target
            )
        )

        // Typing name for a new option

        val textToType = "In"

        R.id.filterInputField.type(textToType)

        // Checking that not only create-option view button, but also tags are visible

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.tvCreateOptionValue).checkHasText("Create option \"$textToType\"")
            onItemView(1, R.id.tvStatusName).checkHasText(option1.text)
            onItemView(2, R.id.tvStatusName).checkHasText(option3.text)
            checkIsRecyclerSize(3)
        }
    }

    @Test
    fun shouldRequestAddingStatusToDataViewRecordWhenStatusClicked() {

        // SETUP

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In progress",
            color = ThemeColor.values().random().code
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Testing",
            color = ThemeColor.values().random().code
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Development",
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
            format = Relation.Format.STATUS,
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
                BaseAddOptionsRelationFragment.CTX_KEY to ctx,
                BaseAddOptionsRelationFragment.RELATION_KEY to relation.key,
                BaseAddOptionsRelationFragment.DATAVIEW_KEY to dv.id,
                BaseAddOptionsRelationFragment.VIEWER_KEY to viewer.id,
                BaseAddOptionsRelationFragment.TARGET_KEY to target
            )
        )


        // Selecting the first two tags

        R.id.recycler.rVMatcher().onItemView(1, R.id.tvStatusName).performClick()

        // Veryfying UI

        verifyBlocking(repo, times(1)) {
            updateDataViewRecord(
                context = ctx,
                target = dv.id,
                record = target,
                values = mapOf(
                    ObjectSetConfig.ID_KEY to target,
                    relationKey to listOf(option2.id)
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