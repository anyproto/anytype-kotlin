package com.anytypeio.anytype.presentation.sets.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.sets.MockObjectSetFactory
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

/**
 * Verifies that FilterViewModel survives an index mismatch between the rendered
 * filter list (_views.value in ViewerFilterViewModel, produced via mapNotNull)
 * and the raw viewer.filters list.
 *
 * Reproduces the crash:
 *   java.lang.IllegalStateException: Incorrect filter state
 *   at FilterViewModel.getCondition(FilterViewModel.kt)
 *
 * Setup: viewer.filters contains two filters. The filter at index 0 references
 * a relation that is NOT in StoreOfRelations, so the rendered list drops it via
 * mapNotNull and all subsequent indices shift down. When the user taps the TAG
 * filter (which renders at index 0 in the view), filterIndex=0 is passed in —
 * but viewer.filters[0] is the missing-relation filter, not the TAG filter.
 *
 * The fix falls back to a relation-key search instead of throwing.
 */
class FilterViewModelIndexFallbackTest {

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

    @Mock
    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    private lateinit var viewModel: FilterViewModel
    private lateinit var urlBuilder: UrlBuilder

    private val root = MockDataFactory.randomUuid()
    private val dataViewId = MockDataFactory.randomString()
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()
    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val objectStore: ObjectStore = DefaultObjectStore()
    private val db = ObjectSetDatabase(store = objectStore)
    private val dispatcher = Dispatcher.Default<Payload>()

    private val tagRelation = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = "Repeat",
        format = Relation.Format.TAG
    )

    private val missingRelation = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = "Missing",
        format = Relation.Format.LONG_TEXT
    )

    private val tagViewerRelation = Block.Content.DataView.Viewer.ViewerRelation(
        key = tagRelation.key,
        isVisible = true
    )

    private val missingFilter = Block.Content.DataView.Filter(
        id = MockDataFactory.randomString(),
        relation = missingRelation.key,
        condition = Block.Content.DataView.Filter.Condition.EQUAL,
        value = MockDataFactory.randomString()
    )

    private val tagFilter = Block.Content.DataView.Filter(
        id = MockDataFactory.randomString(),
        relation = tagRelation.key,
        condition = Block.Content.DataView.Filter.Condition.IN,
        value = null
    )

    private val state = MutableStateFlow(
        MockObjectSetFactory.makeDefaultSetObjectState(
            dataViewId = dataViewId,
            relations = listOf(tagRelation),
            viewerRelations = listOf(tagViewerRelation),
            filters = listOf(missingFilter, tagFilter)
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilderImpl(gateway)
        val analyticSpaceHelperDelegate = mock<com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate>()
        analyticSpaceHelperDelegate.stub {
            on { provideParams(any()) } doReturn com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate.Params.EMPTY
        }
        viewModel = FilterViewModel(
            objectState = state,
            dispatcher = dispatcher,
            urlBuilder = urlBuilder,
            updateDataViewViewer = updateDataViewViewer,
            searchObjects = searchObjects,
            analytics = analytics,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            objectSetDatabase = db,
            getOptions = getOptions,
            spaceManager = spaceManager,
            fieldParser = fieldParser,
            spaceViews = spaceViews,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        )
    }

    @Test
    fun `should resolve condition via relation-key fallback when index points to wrong filter`() = runTest {
        // Only the TAG relation is in StoreOfRelations — the missingRelation is not.
        // This simulates the production scenario where mapNotNull drops the
        // missing-relation filter from the rendered list, shifting indices.
        storeOfRelations.merge(listOf(tagRelation))
        stubSpaceManager()

        val viewer = state.value.dataViewState()!!.viewers[0]

        // The user tapped the TAG filter at position 0 in the rendered view.
        // But viewer.filters[0] is missingFilter, not tagFilter.
        viewModel.onStart(
            relationKey = tagRelation.key,
            filterIndex = 0,
            viewerId = viewer.id
        )

        coroutineTestRule.advanceUntilIdle()

        // Pre-fix: getCondition() threw IllegalStateException, leaving conditionState null.
        // Post-fix: fallback to firstOrNull { it.relation == tagRelation.key } resolves tagFilter.
        val condition = viewModel.conditionState.value
        assertNotNull(
            condition,
            "conditionState must be populated via relation-key fallback when the index points to a different filter"
        )
        assertEquals(
            expected = Viewer.Filter.Condition.Selected.In(),
            actual = condition!!.condition,
            message = "Resolved condition should reflect the TAG filter's IN condition, not the unrelated filter"
        )
    }

    @Test
    fun `should clear value state when no filter matches the relation key`() = runTest {
        // TAG relation is in the store, but viewer.filters contains only the missing-relation filter
        // (no TAG filter at all). The fallback should find nothing and clear state instead of throwing.
        val stateWithoutTagFilter = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = listOf(tagRelation),
                viewerRelations = listOf(tagViewerRelation),
                filters = listOf(missingFilter)
            )
        )

        val analyticSpaceHelperDelegate = mock<com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate>()
        analyticSpaceHelperDelegate.stub {
            on { provideParams(any()) } doReturn com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate.Params.EMPTY
        }

        val vm = FilterViewModel(
            objectState = stateWithoutTagFilter,
            dispatcher = dispatcher,
            urlBuilder = urlBuilder,
            updateDataViewViewer = updateDataViewViewer,
            searchObjects = searchObjects,
            analytics = analytics,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            objectSetDatabase = db,
            getOptions = getOptions,
            spaceManager = spaceManager,
            fieldParser = fieldParser,
            spaceViews = spaceViews,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        )

        storeOfRelations.merge(listOf(tagRelation))
        stubSpaceManager()

        val viewer = stateWithoutTagFilter.value.dataViewState()!!.viewers[0]

        vm.onStart(
            relationKey = tagRelation.key,
            filterIndex = 0,
            viewerId = viewer.id
        )

        coroutineTestRule.advanceUntilIdle()

        // Pre-fix: viewer.filters[0] = missingFilter, check() threw IllegalStateException
        // and conditionState was never populated.
        // Post-fix: fallback finds no match, getCondition falls through to the default
        // condition for the TAG format — conditionState gets populated without throwing.
        assertNotNull(
            vm.conditionState.value,
            "conditionState must be populated even when no filter matches — getCondition must not throw"
        )
        assertEquals(
            expected = emptyList(),
            actual = vm.filterValueListState.value,
            message = "filterValueListState should be cleared when no filter matches the relation key"
        )
    }

    private fun stubSpaceManager() {
        spaceManager.stub {
            onBlocking { get() } doReturn MockDataFactory.randomString()
        }
        getOptions.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(emptyList())
        }
    }
}
