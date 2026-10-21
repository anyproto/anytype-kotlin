package com.anytypeio.anytype.presentation.relations.value.`object`

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

/**
 * Regression coverage for DROID-4554.
 *
 * The picker used to feed the raw `relationFormatObjectTypes` into the search filter, while every
 * other screen reading that field resolves it against the type store first and drops ids that no
 * longer exist. A property limited to a deleted type therefore produced `type IN [<dead ids>]`
 * — matching nothing — while Space settings reported the very same property as "Limit objects: All".
 *
 * These tests assert on the filters actually handed to [SearchObjects], since that is where the
 * two readers diverged.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectValueViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var objectSearch: SearchObjects

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var objectListIsArchived: SetObjectListIsArchived

    @Mock
    lateinit var duplicateObject: DuplicateObject

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    private lateinit var storeOfRelations: StoreOfRelations
    private lateinit var storeOfObjectTypes: StoreOfObjectTypes

    private val ctx: Id = "ctx-id"
    private val objectId: Id = "object-id"
    private val space = SpaceId("space-id")
    private val relationKey = "connexes"

    private val liveTypeId: Id = "type-live"
    private val staleTypeId: Id = "type-deleted"
    private val otherStaleTypeId: Id = "type-from-another-space"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        storeOfRelations = DefaultStoreOfRelations()
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        whenever(analyticSpaceHelperDelegate.provideParams(any()))
            .thenReturn(AnalyticSpaceHelperDelegate.Params.EMPTY)
        whenever(spaceViews.get(space)).thenReturn(null)
        objectSearch.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(emptyList())
        }
    }

    // region helpers

    private fun objectRelation(limitTypes: List<Id>) = ObjectWrapper.Relation(
        buildMap {
            put(Relations.ID, "rel-$relationKey")
            put(Relations.RELATION_KEY, relationKey)
            put(Relations.NAME, "Connexes")
            put(Relations.RELATION_FORMAT, RelationFormat.OBJECT.code.toDouble())
            if (limitTypes.isNotEmpty()) {
                put(Relations.RELATION_FORMAT_OBJECT_TYPES, limitTypes)
            }
        }
    )

    /** A type as the store holds it — [Relations.UNIQUE_KEY] is what makes it `isValid`. */
    private fun type(id: Id, name: String) = ObjectWrapper.Type(
        mapOf(
            Relations.ID to id,
            Relations.UNIQUE_KEY to "ot-$id",
            Relations.NAME to name
        )
    )

    /** A type row missing its uniqueKey, i.e. present but `isValid == false`. */
    private fun invalidType(id: Id) = ObjectWrapper.Type(mapOf(Relations.ID to id))

    private suspend fun buildViewModel(relation: ObjectWrapper.Relation): ObjectValueViewModel {
        storeOfRelations.merge(listOf(relation))
        return ObjectValueViewModel(
            viewModelParams = ObjectValueViewModel.ViewModelParams(
                ctx = ctx,
                space = space,
                objectId = objectId,
                relationKey = relationKey,
                isLocked = false,
                relationContext = RelationContext.OBJECT
            ),
            values = FakeObjectValueProvider(
                values = mapOf(objectId to mapOf(relationKey to emptyList<Id>()))
            ),
            dispatcher = dispatcher,
            setObjectDetails = setObjectDetails,
            analytics = analytics,
            spaceManager = spaceManager,
            objectSearch = objectSearch,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes,
            objectListIsArchived = objectListIsArchived,
            duplicateObject = duplicateObject,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            spaceViews = spaceViews
        )
    }

    private fun capturedFilters(): List<DVFilter> {
        val captor = argumentCaptor<SearchObjects.Params>()
        verifyBlocking(objectSearch) { invoke(captor.capture()) }
        return captor.lastValue.filters
    }

    private fun List<DVFilter>.typeFilter() = firstOrNull { it.relation == Relations.TYPE }
    private fun List<DVFilter>.layoutFilter() = firstOrNull { it.relation == Relations.LAYOUT }

    // endregion

    @Test
    fun `drops type ids that no longer resolve and falls back to the layout filter`() = runTest {
        // The reported state: the property still references types that were deleted, so the
        // store resolves none of them.
        storeOfObjectTypes.merge(listOf(type(liveTypeId, "Task")))

        buildViewModel(objectRelation(listOf(staleTypeId, otherStaleTypeId)))
        advanceUntilIdle()

        val filters = capturedFilters()
        assertNull(
            filters.typeFilter(),
            "A restriction made entirely of dead ids must not be sent — it can never match."
        )
        val layout = requireNotNull(filters.layoutFilter())
        assertEquals(DVFilterCondition.IN, layout.condition)
        assertTrue((layout.value as List<*>).isNotEmpty())
    }

    @Test
    fun `keeps only the resolvable ids when a restriction is partly stale`() = runTest {
        storeOfObjectTypes.merge(listOf(type(liveTypeId, "Task")))

        buildViewModel(objectRelation(listOf(staleTypeId, liveTypeId)))
        advanceUntilIdle()

        val filters = capturedFilters()
        val typeFilter = requireNotNull(filters.typeFilter())
        assertEquals(DVFilterCondition.IN, typeFilter.condition)
        assertEquals(listOf(liveTypeId), typeFilter.value)
        assertNull(filters.layoutFilter(), "A real restriction replaces the layout fallback.")
    }

    @Test
    fun `drops ids whose type row is present but invalid`() = runTest {
        storeOfObjectTypes.merge(listOf(invalidType(staleTypeId)))

        buildViewModel(objectRelation(listOf(staleTypeId)))
        advanceUntilIdle()

        val filters = capturedFilters()
        assertNull(filters.typeFilter())
        assertTrue(filters.layoutFilter() != null)
    }

    @Test
    fun `an unrestricted property still uses the layout filter`() = runTest {
        storeOfObjectTypes.merge(listOf(type(liveTypeId, "Task")))

        buildViewModel(objectRelation(emptyList()))
        advanceUntilIdle()

        val filters = capturedFilters()
        assertNull(filters.typeFilter())
        assertTrue(filters.layoutFilter() != null)
    }

    @Test
    fun `empty state names the types when the restriction is real`() = runTest {
        storeOfObjectTypes.merge(listOf(type(liveTypeId, "Task")))

        val vm = buildViewModel(objectRelation(listOf(liveTypeId)))
        advanceUntilIdle()

        val state = vm.viewState.value as ObjectValueViewState.Empty
        assertEquals("Task", state.limitedToTypeNames)
    }

    @Test
    fun `empty state claims no restriction when every id is stale`() = runTest {
        storeOfObjectTypes.merge(listOf(type(liveTypeId, "Task")))

        val vm = buildViewModel(objectRelation(listOf(staleTypeId)))
        advanceUntilIdle()

        val state = vm.viewState.value as ObjectValueViewState.Empty
        assertNull(
            state.limitedToTypeNames,
            "Must agree with Space settings, which reports this property as unrestricted."
        )
    }
}
