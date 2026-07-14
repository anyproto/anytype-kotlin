package com.anytypeio.anytype.presentation.relations.value.tagstatus

import app.cash.turbine.Event
import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelationOptions
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelationOptions
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationOptions
import com.anytypeio.anytype.domain.relations.SetRelationOptionOrder
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TagOrStatusValueViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var deleteRelationOptions: DeleteRelationOptions

    @Mock
    lateinit var setRelationOptionOrder: SetRelationOptionOrder

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    private lateinit var storeOfRelations: StoreOfRelations
    private lateinit var storeOfRelationOptions: StoreOfRelationOptions
    private lateinit var values: FakeObjectValueProvider

    private val ctx: Id = "ctx-id"
    private val objectId: Id = "object-id"
    private val space = SpaceId("space-id")
    private val relationKey = "custom-tag"

    private val tagA = "opt-a"
    private val tagB = "opt-b"
    private val tagC = "opt-c"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        storeOfRelations = DefaultStoreOfRelations()
        storeOfRelationOptions = DefaultStoreOfRelationOptions()
        whenever(analyticSpaceHelperDelegate.provideParams(any()))
            .thenReturn(AnalyticSpaceHelperDelegate.Params.EMPTY)
    }

    // region helpers

    private fun tagRelation() = ObjectWrapper.Relation(
        mapOf(
            Relations.ID to "rel-$relationKey",
            Relations.RELATION_KEY to relationKey,
            Relations.NAME to "Tags",
            Relations.RELATION_FORMAT to RelationFormat.TAG.code.toDouble()
        )
    )

    private fun statusRelation() = ObjectWrapper.Relation(
        mapOf(
            Relations.ID to "rel-$relationKey",
            Relations.RELATION_KEY to relationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to RelationFormat.STATUS.code.toDouble()
        )
    )

    private fun option(id: Id, order: String, key: String = relationKey) = ObjectWrapper.Option(
        mapOf(
            Relations.ID to id,
            Relations.RELATION_KEY to key,
            Relations.NAME to id,
            Relations.ORDER_ID to order,
            Relations.RELATION_OPTION_COLOR to ThemeColor.RED.code
        )
    )

    private fun stubPersistSuccess() {
        setObjectDetails.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Payload(context = objectId, events = emptyList()))
        }
    }

    private fun stubPersistFailure() {
        setObjectDetails.stub {
            onBlocking { invoke(any()) } doReturn Either.Left(RuntimeException("boom"))
        }
    }

    private fun buildViewModel(initialIds: List<Id>): TagOrStatusValueViewModel {
        values = FakeObjectValueProvider(
            values = mapOf(objectId to mapOf(relationKey to initialIds))
        )
        return TagOrStatusValueViewModel(
            viewModelParams = TagOrStatusValueViewModel.ViewModelParams(
                ctx = ctx,
                space = space,
                objectId = objectId,
                relationKey = relationKey,
                isLocked = false,
                relationContext = RelationContext.DATA_VIEW
            ),
            values = values,
            dispatcher = dispatcher,
            setObjectDetails = setObjectDetails,
            analytics = analytics,
            deleteRelationOptions = deleteRelationOptions,
            setRelationOptionOrder = setRelationOptionOrder,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            storeOfRelations = storeOfRelations,
            storeOfRelationOptions = storeOfRelationOptions
        )
    }

    private fun TagOrStatusValueViewModel.items(): List<RelationsListItem.Item> =
        (viewState.value as TagStatusViewState.Content).items

    private fun List<RelationsListItem.Item>.byId(id: Id) = first { it.optionId == id }

    private fun params(value: Any?) = UpdateDetail.Params(target = objectId, key = relationKey, value = value)

    // endregion

    @Test
    fun `clicking an unselected tag selects it in viewState without any subscribe re-emit`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        // Initial: nothing selected.
        assertTrue(vm.items().none { it.isSelected })

        val tagBItem = vm.items().byId(tagB)
        vm.onAction(TagStatusAction.Click(tagBItem))
        advanceUntilIdle()

        val updated = vm.items()
        val b = updated.byId(tagB) as RelationsListItem.Item.Tag
        assertTrue(b.isSelected)
        assertEquals(1, b.number)
        assertFalse(updated.byId(tagA).isSelected)
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tagB))) }
    }

    @Test
    fun `clicking a selected tag removes it and recomputes numbers`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tagA, tagB))
        advanceUntilIdle()

        val tagAItem = vm.items().byId(tagA)
        assertTrue(tagAItem.isSelected)
        vm.onAction(TagStatusAction.Click(tagAItem))
        advanceUntilIdle()

        val updated = vm.items()
        assertFalse(updated.byId(tagA).isSelected)
        val b = updated.byId(tagB) as RelationsListItem.Item.Tag
        assertTrue(b.isSelected)
        assertEquals(1, b.number)
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tagB))) }
    }

    @Test
    fun `tag selection order drives number, independent of display order`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tagA))
        advanceUntilIdle()

        // Click C (unselected), then B (unselected). Selection order: A, C, B.
        vm.onAction(TagStatusAction.Click(vm.items().byId(tagC)))
        advanceUntilIdle()
        vm.onAction(TagStatusAction.Click(vm.items().byId(tagB)))
        advanceUntilIdle()

        val updated = vm.items()
        assertEquals(1, (updated.byId(tagA) as RelationsListItem.Item.Tag).number)
        assertEquals(2, (updated.byId(tagC) as RelationsListItem.Item.Tag).number)
        assertEquals(3, (updated.byId(tagB) as RelationsListItem.Item.Tag).number)
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tagA, tagC, tagB))) }
    }

    @Test
    fun `status single-select selects then clears and dismisses on select`() = runTest {
        storeOfRelations.merge(listOf(statusRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        // Select statusA and confirm the sheet dismisses.
        vm.commands.test {
            vm.onAction(TagStatusAction.Click(vm.items().byId(tagA)))
            advanceUntilIdle()
            val emitted = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<Command>>()
                .map { it.value }
            assertTrue(emitted.contains(Command.Dismiss))
        }

        assertTrue(vm.items().byId(tagA).isSelected)
        assertFalse(vm.items().byId(tagB).isSelected)
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tagA))) }

        // Click the selected status again → clears.
        vm.onAction(TagStatusAction.Click(vm.items().byId(tagA)))
        advanceUntilIdle()

        assertTrue(vm.items().none { it.isSelected })
        verifyBlocking(setObjectDetails) { invoke(params(null)) }
    }

    @Test
    fun `clear action empties the selection`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tagA, tagB))
        advanceUntilIdle()

        vm.onAction(TagStatusAction.Clear)
        advanceUntilIdle()

        assertTrue(vm.items().none { it.isSelected })
        verifyBlocking(setObjectDetails) { invoke(params(null)) }
    }

    @Test
    fun `failure reverts the optimistic update and toasts`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistFailure()

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()
        val snapshot = vm.viewState.value

        vm.toasts.test {
            vm.onAction(TagStatusAction.Click(vm.items().byId(tagB)))
            advanceUntilIdle()
            val messages = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<String>>()
                .map { it.value }
            assertTrue(messages.contains("Error while updating value"))
        }

        // Reverted to the pre-click state.
        assertEquals(snapshot, vm.viewState.value)
    }

    @Test
    fun `stale subscription emission does not clobber the optimistic state`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistSuccess()

        // Provider streams an empty value once and never re-emits (orphaned-store condition).
        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        vm.onAction(TagStatusAction.Click(vm.items().byId(tagB)))
        advanceUntilIdle()
        assertTrue(vm.items().byId(tagB).isSelected)

        // Force the combine to re-run (via a store change) while the option-event lock is active.
        // The cached record value is still empty; without the lock this would deselect tagB.
        storeOfRelationOptions.merge(listOf(option("unrelated", "0", key = "other-key")))
        advanceUntilIdle()

        assertTrue(vm.items().byId(tagB).isSelected)
    }
}
