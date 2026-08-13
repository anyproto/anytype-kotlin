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
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
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

    // Named so that the query "7" matches tag7 only.
    private val tag2 = "tag2"
    private val tag3 = "tag3"
    private val tag7 = "tag7"

    private val tagD = "opt-d"

    // Named so that the query "7" matches both.
    private val tag7a = "tag7a"
    private val tag7b = "tag7b"

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

    private fun stubOptionOrderSuccess() {
        setRelationOptionOrder.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    private fun orderParams(orderedIds: List<Id>) = SetRelationOptionOrder.Params(
        spaceId = space,
        relationKey = RelationKey(relationKey),
        orderedIds = orderedIds
    )

    private fun buildViewModel(
        initialIds: List<Id>,
        isLocked: Boolean = false
    ): TagOrStatusValueViewModel {
        values = FakeObjectValueProvider(
            values = mapOf(objectId to mapOf(relationKey to initialIds))
        )
        return TagOrStatusValueViewModel(
            viewModelParams = TagOrStatusValueViewModel.ViewModelParams(
                ctx = ctx,
                space = space,
                objectId = objectId,
                relationKey = relationKey,
                isLocked = isLocked,
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

    private fun TagOrStatusValueViewModel.allItems(): List<RelationsListItem> =
        (viewState.value as TagStatusViewState.Content).items

    private fun TagOrStatusValueViewModel.items(): List<RelationsListItem.Item> =
        allItems().filterIsInstance<RelationsListItem.Item>()

    private fun List<RelationsListItem>.byId(id: Id) =
        filterIsInstance<RelationsListItem.Item>().first { it.optionId == id }

    /** Maps each row to its optionId, or to the Section object itself, for order assertions. */
    private fun TagOrStatusValueViewModel.rows(): List<Any> =
        allItems().map { if (it is RelationsListItem.Item) it.optionId else it }

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
    fun `selecting a tag under an active query keeps the selections hidden by the query`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tag2, "0"), option(tag3, "1"), option(tag7, "2"))
        )
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tag2, tag3))
        advanceUntilIdle()

        vm.onQueryChanged("7")
        advanceUntilIdle()

        // The query hides the two selected tags.
        assertEquals(listOf(tag7), vm.items().map { it.optionId })

        vm.onAction(TagStatusAction.Click(vm.items().byId(tag7)))
        advanceUntilIdle()

        verifyBlocking(setObjectDetails) { invoke(params(listOf(tag2, tag3, tag7))) }
    }

    @Test
    fun `deselecting a tag under an active query keeps the selections hidden by the query`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tag2, "0"), option(tag3, "1"), option(tag7, "2"))
        )
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tag2, tag3, tag7))
        advanceUntilIdle()

        vm.onQueryChanged("7")
        advanceUntilIdle()

        assertEquals(listOf(tag7), vm.items().map { it.optionId })
        assertTrue(vm.items().byId(tag7).isSelected)

        vm.onAction(TagStatusAction.Click(vm.items().byId(tag7)))
        advanceUntilIdle()

        verifyBlocking(setObjectDetails) { invoke(params(listOf(tag2, tag3))) }
    }

    @Test
    fun `a late failure does not revert a newer write that already succeeded`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2"))
        )

        // The first write fails, but only after the second write succeeds.
        setObjectDetails.stub {
            onBlocking { invoke(params(listOf(tagA, tagB))) } doSuspendableAnswer {
                delay(1000)
                Either.Left(RuntimeException("boom"))
            }
            onBlocking { invoke(params(listOf(tagA, tagB, tagC))) } doReturn
                    Either.Right(Payload(context = objectId, events = emptyList()))
        }

        val vm = buildViewModel(initialIds = listOf(tagA))
        advanceUntilIdle()

        // Two taps in flight together.
        vm.onAction(TagStatusAction.Click(vm.items().byId(tagB)))
        vm.onAction(TagStatusAction.Click(vm.items().byId(tagC)))
        advanceUntilIdle()

        // The late failure must not resurrect the snapshot that holds tagA alone.
        val updated = vm.items()
        assertTrue(updated.byId(tagA).isSelected)
        assertTrue(updated.byId(tagB).isSelected)
        assertTrue(updated.byId(tagC).isSelected)
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

    @Test
    fun `editable tag list shows the selected section then the all values section`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2")))

        val vm = buildViewModel(initialIds = listOf(tagC, tagB))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(
                RelationsListItem.Section.Selected,
                tagC,
                tagB,
                RelationsListItem.Section.AllValues,
                tagA
            ),
            vm.rows()
        )
        assertEquals(1, (vm.allItems().byId(tagC) as RelationsListItem.Item.Tag).number)
        assertEquals(2, (vm.allItems().byId(tagB) as RelationsListItem.Item.Tag).number)
    }

    @Test
    fun `no selection shows only the all values header`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(RelationsListItem.Section.AllValues, tagA, tagB),
            vm.rows()
        )
    }

    @Test
    fun `full selection shows only the selected header`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagA, tagB))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(RelationsListItem.Section.Selected, tagA, tagB),
            vm.rows()
        )
    }

    @Test
    fun `status list gets the same sections`() = runTest {
        storeOfRelations.merge(listOf(statusRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagB))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(
                RelationsListItem.Section.Selected,
                tagB,
                RelationsListItem.Section.AllValues,
                tagA
            ),
            vm.rows()
        )
    }

    @Test
    fun `read-only list has no section headers`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagB), isLocked = true)
        advanceUntilIdle()

        assertEquals(listOf<Any>(tagB), vm.rows())
    }

    @Test
    fun `drag inside the selected section reorders the object value`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tagA, tagB, tagC))
        advanceUntilIdle()

        // Flat list: [Section.Selected, A, B, C]. Move A after C.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 3))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(RelationsListItem.Section.Selected, tagB, tagC, tagA),
            vm.rows()
        )
        assertEquals(1, (vm.allItems().byId(tagB) as RelationsListItem.Item.Tag).number)
        assertEquals(3, (vm.allItems().byId(tagA) as RelationsListItem.Item.Tag).number)
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tagB, tagC, tagA))) }
        verifyNoInteractions(setRelationOptionOrder)
    }

    @Test
    fun `drag inside the selected section keeps the selections hidden by the query`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tag2, "0"), option(tag7a, "1"), option(tag7b, "2"))
        )
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tag2, tag7a, tag7b))
        advanceUntilIdle()

        vm.onQueryChanged("7")
        advanceUntilIdle()

        // Flat list under the query: [Section.Selected, tag7a, tag7b]. Swap them.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 2))
        advanceUntilIdle()

        // The hidden selection tag2 keeps its first position.
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tag2, tag7b, tag7a))) }
    }

    @Test
    fun `a failed value write after a selected drag reverts the order and toasts`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistFailure()

        val vm = buildViewModel(initialIds = listOf(tagA, tagB))
        advanceUntilIdle()
        val snapshot = vm.viewState.value

        vm.toasts.test {
            vm.onAction(TagStatusAction.OnMove(from = 1, to = 2))
            advanceUntilIdle()
            val messages = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<String>>()
                .map { it.value }
            assertTrue(messages.contains("Error while updating value"))
        }

        assertEquals(snapshot, vm.viewState.value)
    }

    @Test
    fun `drag inside all values saves the merged global option order`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2"), option(tagD, "3"))
        )
        stubOptionOrderSuccess()

        val vm = buildViewModel(initialIds = listOf(tagB))
        advanceUntilIdle()

        // Flat list: [Section.Selected, B, Section.AllValues, A, C, D]. Move D before A.
        vm.onAction(TagStatusAction.OnMove(from = 5, to = 3))
        advanceUntilIdle()

        // Full order was [A, B, C, D]; B is hidden from the section and keeps its slot.
        verifyBlocking(setRelationOptionOrder) {
            async(orderParams(listOf(tagD, tagB, tagA, tagC)))
        }
        verifyNoInteractions(setObjectDetails)
    }

    @Test
    fun `drag inside all values under a query keeps the hidden options in place`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tag7a, "0"), option(tag2, "1"), option(tag7b, "2"))
        )
        stubOptionOrderSuccess()

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        vm.onQueryChanged("7")
        advanceUntilIdle()

        // Flat list under the query: [Section.AllValues, tag7a, tag7b]. Swap them.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 2))
        advanceUntilIdle()

        // Full order was [tag7a, tag2, tag7b]; hidden tag2 keeps its slot.
        verifyBlocking(setRelationOptionOrder) {
            async(orderParams(listOf(tag7b, tag2, tag7a)))
        }
    }

    @Test
    fun `an option deleted from the store after the drag is dropped from the persisted order`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2"), option(tagD, "3"))
        )
        stubOptionOrderSuccess()

        val vm = buildViewModel(initialIds = listOf(tagB))
        advanceUntilIdle()

        // Flat list: [Section.Selected, B, Section.AllValues, A, C, D]. Move C before A.
        // onAction captures the displayed order [C, A, D] synchronously; the store fetch
        // inside moveOptionOrder only runs once advanceUntilIdle resumes the coroutine.
        vm.onAction(TagStatusAction.OnMove(from = 4, to = 3))

        // Simulate a concurrent deletion (another device or a fast second drag) that lands
        // before the suspend fetch resolves. This id was part of the captured displayed order.
        storeOfRelationOptions.remove(tagC)
        advanceUntilIdle()

        // Full order at fetch time is [A, B, D]; the dropped tagC is neither written into the
        // order nor does it displace tagD's slot. tagA and tagD swap as the drag intended.
        verifyBlocking(setRelationOptionOrder) {
            async(orderParams(listOf(tagA, tagB, tagD)))
        }
    }

    @Test
    fun `drag across sections is ignored`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagA))
        advanceUntilIdle()

        // Flat list: [Section.Selected, A, Section.AllValues, B].
        // from = selected item, to = unselected item.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 3))
        // from = item, to = section header.
        vm.onAction(TagStatusAction.OnMove(from = 3, to = 2))
        advanceUntilIdle()

        verifyNoInteractions(setObjectDetails)
        verifyNoInteractions(setRelationOptionOrder)
    }
}
