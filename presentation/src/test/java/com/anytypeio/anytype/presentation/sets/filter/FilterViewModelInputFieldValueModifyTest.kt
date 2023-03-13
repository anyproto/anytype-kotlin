package com.anytypeio.anytype.presentation.sets.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.sets.MockObjectSetFactory
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class FilterViewModelInputFieldValueModifyTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var updateDataViewViewer: UpdateDataViewViewer

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var getOptions: GetOptions

    @Mock
    lateinit var analytics: Analytics

    private lateinit var viewModel: FilterViewModel
    private lateinit var urlBuilder: UrlBuilder
    private val root = MockDataFactory.randomUuid()
    private val dataViewId = MockDataFactory.randomString()
    private val session = ObjectSetSession()
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    lateinit var workspaceManager: WorkspaceManager
    val workspaceId = MockDataFactory.randomString()

    //LONG TEXT
    private val relation1 = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.LONG_TEXT
    )

    private val viewerRelation1 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation1.key,
        isVisible = true
    )

    //NUMBER
    private val relation2 = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.NUMBER
    )

    private val viewerRelation2 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation2.key,
        isVisible = true
    )

    //SHORT TEXT
    private val relation3 = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.SHORT_TEXT
    )

    private val viewerRelation3 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation3.key,
        isVisible = true
    )

    //URL
    private val relation4 = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.URL
    )

    private val viewerRelation4 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation4.key,
        isVisible = true
    )

    //EMAIL
    private val relation5 = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.EMAIL
    )

    private val viewerRelation5 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation5.key,
        isVisible = true
    )

    //PHONE
    private val relation6 = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.PHONE
    )

    private val viewerRelation6 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation6.key,
        isVisible = true
    )

    private val relations = listOf(
        relation1,
        relation2,
        relation3,
        relation4,
        relation5,
        relation6
    )

    private val state = MutableStateFlow(
        MockObjectSetFactory.makeDefaultSetObjectState(
            dataViewId = dataViewId,
            relations = relations,
            viewerRelations = listOf(
                viewerRelation1,
                viewerRelation2,
                viewerRelation3,
                viewerRelation4,
                viewerRelation5,
                viewerRelation6
            ),
            filters = listOf(
                Block.Content.DataView.Filter(
                    relation = relation1.key,
                    operator = Block.Content.DataView.Filter.Operator.AND,
                    condition = Block.Content.DataView.Filter.Condition.LIKE,
                    value = MockDataFactory.randomString()
                )
            )
        )
    )
    private val dispatcher = Dispatcher.Default<Payload>()

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val objectStore: ObjectStore = DefaultObjectStore()
    private val db = ObjectSetDatabase(store = objectStore)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
        workspaceManager = WorkspaceManager.DefaultWorkspaceManager()
        runBlocking {
            workspaceManager.setCurrentWorkspace(workspaceId)
        }
        viewModel = FilterViewModel(
            objectState = state,
            session = session,
            dispatcher = dispatcher,
            urlBuilder = urlBuilder,
            updateDataViewViewer = updateDataViewViewer,
            searchObjects = searchObjects,
            analytics = analytics,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            objectSetDatabase = db,
            getOptions = getOptions,
            workspaceManager = workspaceManager
        )
    }

    //region LONG TEXT
    @Test
    fun `should empty string value, long text 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNullString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, long text 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNullString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, long text 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNullString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, long text 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNullString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = 0

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    //endregion

    private fun shouldSendFilterValueAsNullString(
        relationKey: String,
        condition: Viewer.Filter.Condition,
        textInput: String,
        filterIndex: Int?
    ) {
        stubUpdateDataView()

        viewModel.onStart(
            relationKey = relation1.key,
            filterIndex = filterIndex
        )

        viewModel.onConditionUpdate(condition)

        viewModel.onModifyApplyClicked(
            ctx = root,
            input = textInput
        )

        val viewer = state.value.dataViewState()!!.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params.Filter.Replace(
                    ctx = root,
                    dv = dataViewId,
                    view = viewer.id,
                    filter = DVFilter(
                        relation = relationKey,
                        operator = DEFAULT_OPERATOR,
                        condition = condition.toDomain(),
                        value = null
                    )
                )
            )
        }
    }

    private fun shouldSendFilterValueAsNotEmptyString(
        relationKey: String,
        condition: Viewer.Filter.Condition,
        textInput: String,
        filterIndex: Int?
    ) {

        stubUpdateDataView()

        viewModel.onStart(
            relationKey = relation1.key,
            filterIndex = filterIndex
        )

        viewModel.onConditionUpdate(condition)

        viewModel.onModifyApplyClicked(
            ctx = root,
            input = textInput
        )

        val viewer = state.value.dataViewState()!!.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params.Filter.Replace(
                    ctx = root,
                    dv = dataViewId,
                    view = viewer.id,
                    filter = DVFilter(
                        relation = relationKey,
                        operator = DEFAULT_OPERATOR,
                        condition = condition.toDomain(),
                        value = textInput
                    )
                )
            )
        }
    }

    private fun stubUpdateDataView() {
        updateDataViewViewer.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = "",
                    events = emptyList()
                )
            )
        }
    }

    companion object {
        const val EMPTY_STRING = ""
        const val NOT_EMPTY_STRING = "not empty"
        val DEFAULT_OPERATOR = Block.Content.DataView.Filter.Operator.AND
    }
}