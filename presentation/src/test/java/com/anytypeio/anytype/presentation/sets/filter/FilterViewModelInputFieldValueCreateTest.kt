package com.anytypeio.anytype.presentation.sets.filter

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.sets.MockObjectSetFactory
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

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

    private lateinit var viewModel: FilterViewModel
    private lateinit var urlBuilder: UrlBuilder
    private val root = MockDataFactory.randomUuid()
    private val dataViewId = MockDataFactory.randomString()
    private val session = ObjectSetSession()

    //LONG TEXT
    private val relation1 = Relation(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.LONG_TEXT,
        source = Relation.Source.DETAILS,
        defaultValue = null
    )

    private val viewerRelation1 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation1.key,
        isVisible = true
    )

    //NUMBER
    private val relation2 = Relation(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.NUMBER,
        source = Relation.Source.DETAILS,
        defaultValue = null
    )

    private val viewerRelation2 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation2.key,
        isVisible = true
    )

    //SHORT TEXT
    private val relation3 = Relation(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.SHORT_TEXT,
        source = Relation.Source.DETAILS,
        defaultValue = null
    )

    private val viewerRelation3 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation3.key,
        isVisible = true
    )

    //URL
    private val relation4 = Relation(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.URL,
        source = Relation.Source.DETAILS,
        defaultValue = null
    )

    private val viewerRelation4 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation4.key,
        isVisible = true
    )

    //EMAIL
    private val relation5 = Relation(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.EMAIL,
        source = Relation.Source.DETAILS,
        defaultValue = null
    )

    private val viewerRelation5 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation5.key,
        isVisible = true
    )

    //PHONE
    private val relation6 = Relation(
        key = MockDataFactory.randomString(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.PHONE,
        source = Relation.Source.DETAILS,
        defaultValue = null
    )

    private val viewerRelation6 = Block.Content.DataView.Viewer.ViewerRelation(
        key = relation6.key,
        isVisible = true
    )

    private val state = MutableStateFlow(
        MockObjectSetFactory.makeDefaultObjectSet(
            dataViewId = dataViewId,
            relations = listOf(relation1, relation2, relation3),
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

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        urlBuilder = UrlBuilder(gateway)
        viewModel = FilterViewModel(
            objectSetState = state,
            session = session,
            dispatcher = dispatcher,
            urlBuilder = urlBuilder,
            updateDataViewViewer = updateDataViewViewer,
            searchObjects = searchObjects
        )
    }

    //region LONG TEXT
    @Test
    fun `should empty string value, long text 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, long text 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, long text 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, long text 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, long text 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation1.key, condition, textInput, filterIndex)
    }

    //endregion

    //region SHORT TEXT
    @Test
    fun `should empty string value, short text 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, short text 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, short text 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, short text 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, short text 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation3.key, condition, textInput, filterIndex)
    }
    //endregion

    //region NUMBER
    @Test
    fun `should empty string value, number 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, number 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, number 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, number 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, number 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, number 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, number 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation2.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, number 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation2.key, condition, textInput, filterIndex)
    }
    //endregion

    //region URL
    @Test
    fun `should empty string value, url 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, url 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, url 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, url 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, url 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation4.key, condition, textInput, filterIndex)
    }
    //endregion

    //region EMAIL
    @Test
    fun `should empty string value, email 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, email 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, email 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, email 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, email 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation5.key, condition, textInput, filterIndex)
    }
    //endregion

    //region PHONE
    @Test
    fun `should empty string value, phone 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, phone 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Empty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, phone 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should empty string value, phone 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEmpty()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 1`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Equal()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 2`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotEqual()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 3`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.Like()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }

    @Test
    fun `should not empty string value, phone 4`() {

        //INIT
        val condition = Viewer.Filter.Condition.Text.NotLike()
        val textInput = NOT_EMPTY_STRING
        val filterIndex = null

        shouldSendFilterValueAsNotEmptyString(relation6.key, condition, textInput, filterIndex)
    }
    //endregion

    private fun shouldSendFilterValueAsEmptyString(
        relationKey: String,
        condition: Viewer.Filter.Condition,
        textInput: String,
        filterIndex: Int?
    ) {
        viewModel.onStart(
            relationId = relation1.key,
            filterIndex = filterIndex
        )

        viewModel.onConditionUpdate(condition)

        viewModel.onCreateInputValueFilterClicked(
            ctx = root,
            relation = relationKey,
            input = textInput
        )

        val viewer = state.value.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params(
                    context = root,
                    target = dataViewId,
                    viewer = viewer.copy(
                        filters = listOf(
                            DVFilter(
                                relationKey = relationKey,
                                operator = DEFAULT_OPERATOR,
                                condition = condition.toDomain(),
                                value = EMPTY_STRING
                            )
                        )
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
        viewModel.onStart(
            relationId = relation1.key,
            filterIndex = filterIndex
        )

        viewModel.onConditionUpdate(condition)

        viewModel.onCreateInputValueFilterClicked(
            ctx = root,
            relation = relationKey,
            input = textInput
        )

        val viewer = state.value.viewers[0]

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params(
                    context = root,
                    target = dataViewId,
                    viewer = viewer.copy(
                        filters = listOf(
                            DVFilter(
                                relationKey = relationKey,
                                operator = DEFAULT_OPERATOR,
                                condition = condition.toDomain(),
                                value = textInput
                            )
                        )
                    )
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