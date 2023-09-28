package com.anytypeio.anytype.presentation.sets.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
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

class FilterViewModelInputFieldValueCreateTest {

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

    @Mock
    lateinit var spaceManager: SpaceManager

    private lateinit var viewModel: FilterViewModel
    private lateinit var urlBuilder: UrlBuilder
    private val root = MockDataFactory.randomUuid()
    private val dataViewId = MockDataFactory.randomString()
    private val session = ObjectSetSession()
    private val storeOfObjectTypes = DefaultStoreOfObjectTypes()

    val spaceId = MockDataFactory.randomString()


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
            spaceManager = spaceManager
        )
    }

    //region LONG TEXT
    @Test
    fun `should null string value, long text 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, long text 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, long text 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, long text 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    //endregion

    //region SHORT TEXT
    @Test
    fun `should null string value, short text 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, short text 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, short text 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, short text 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }
    //endregion

    //region NUMBER
    @Test
    fun `should null string value, number 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, number 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, number 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, number 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should send default number value, number 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = ""
        val filterIndex = null

        val value = Relations.NUMBER_DEFAULT_VALUE

        shouldSendFilterValueAsAny(
            relationKey = relation2.key,
            condition = condition,
            textInput = textInput,
            value = value,
            filterIndex = filterIndex
        )
    }

    @Test
    fun `should send default number value, number 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = " "
        val filterIndex = null

        val value = Relations.NUMBER_DEFAULT_VALUE

        shouldSendFilterValueAsAny(
            relationKey = relation2.key,
            condition = condition,
            textInput = textInput,
            value = value,
            filterIndex = filterIndex
        )
    }

    @Test
    fun `should send double, number 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = "1.01"
        val filterIndex = null

        val value = 1.01

        shouldSendFilterValueAsAny(
            relationKey = relation2.key,
            condition = condition,
            textInput = textInput,
            value = value,
            filterIndex = filterIndex
        )
    }

    @Test
    fun `should send negative double, number 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = "-3.0"
        val filterIndex = null

        val value = -3.0

        shouldSendFilterValueAsAny(
            relationKey = relation2.key,
            condition = condition,
            textInput = textInput,
            value = value,
            filterIndex = filterIndex
        )
    }

    @Test
    fun `should send negative double, number 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = "-4"
        val filterIndex = null

        val value = -4.0

        shouldSendFilterValueAsAny(
            relationKey = relation2.key,
            condition = condition,
            textInput = textInput,
            value = value,
            filterIndex = filterIndex
        )
    }

    @Test
    fun `should send default number value, number 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = "e1.0"
        val filterIndex = null

        val value = Relations.NUMBER_DEFAULT_VALUE

        shouldSendFilterValueAsAny(
            relationKey = relation2.key,
            condition = condition,
            textInput = textInput,
            value = value,
            filterIndex = filterIndex
        )
    }


    //endregion

    //region URL
    @Test
    fun `should null string value, url 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, url 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, url 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, url 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }
    //endregion

    //region EMAIL
    @Test
    fun `should null string value, email 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, email 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, email 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, email 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }
    //endregion

    //region PHONE
    @Test
    fun `should null string value, phone 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, phone 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, phone 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should null string value, phone 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNull(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 1`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 2`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 3`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 4`() = runTest {

        storeOfRelations.merge(relations)

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }
    //endregion

    private fun shouldSendFilterValueAsNull(
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

        viewModel.onCreateInputValueFilterClicked(
            ctx = root,
            relation = relationKey,
            input = textInput
        )

        val viewer = state.value.dataViewState()!!.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params.Filter.Add(
                    ctx = root,
                    dv = dataViewId,
                    view = viewer.id,
                    relationKey = relationKey,
                    operator = DEFAULT_OPERATOR,
                    condition = condition.toDomain(),
                    value = null
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

        viewModel.onCreateInputValueFilterClicked(
            ctx = root,
            relation = relationKey,
            input = textInput
        )

        val viewer = state.value.dataViewState()!!.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params.Filter.Add(
                    ctx = root,
                    dv = dataViewId,
                    view = viewer.id,
                    relationKey = relationKey,
                    operator = DEFAULT_OPERATOR,
                    condition = condition.toDomain(),
                    value = textInput
                )
            )
        }
    }

    private fun shouldSendFilterValueAsAny(
        relationKey: String,
        condition: Viewer.Filter.Condition,
        textInput: String,
        filterIndex: Int?,
        value: Any?
    ) {
        stubUpdateDataView()

        viewModel.onStart(
            relationKey = relationKey,
            filterIndex = filterIndex
        )

        viewModel.onConditionUpdate(condition)

        viewModel.onCreateInputValueFilterClicked(
            ctx = root,
            relation = relationKey,
            input = textInput
        )

        val viewer = state.value.dataViewState()!!.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params.Filter.Add(
                    ctx = root,
                    dv = dataViewId,
                    view = viewer.id,
                    relationKey = relationKey,
                    operator = DEFAULT_OPERATOR,
                    condition = condition.toDomain(),
                    value = value
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