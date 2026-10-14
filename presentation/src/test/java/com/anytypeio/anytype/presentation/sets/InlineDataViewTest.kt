package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.multiplayer.GetCurrentParticipantId
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * DROID-4606: an inline query of a page holds its own views. The set screen shows the block of
 * the page, takes the records from the target of the block, and replaces the placeholders of a
 * filter value.
 */
class InlineDataViewTest {

    private val page = MockDataFactory.randomUuid()
    private val target = MockDataFactory.randomUuid()
    private val belongsTo = MockDataFactory.randomUuid()
    private val title = StubTitle()

    private val thisObjectFilter = DVFilter(
        relation = belongsTo,
        relationFormat = RelationFormat.OBJECT,
        condition = DVFilterCondition.ALL_IN,
        value = listOf(FilterValueTemplates.THIS_OBJECT)
    )

    private val otherInline = StubDataView(
        views = listOf(StubDataViewView(id = "other")),
        targetObjectId = MockDataFactory.randomUuid()
    )

    private val inline = StubDataView(
        views = listOf(StubDataViewView(id = "default", filters = listOf(thisObjectFilter))),
        targetObjectId = target
    )

    private fun showPage(targetLayout: ObjectType.Layout, setOf: List<String> = emptyList()) =
        Event.Command.ShowObject(
            context = page,
            root = page,
            blocks = listOf(title, otherInline, inline),
            details = mapOf(
                page to mapOf(
                    Relations.ID to page,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                ),
                target to mapOf(
                    Relations.ID to target,
                    Relations.LAYOUT to targetLayout.code.toDouble(),
                    Relations.SET_OF to setOf
                )
            )
        )

    private fun reduce(blockId: String?, vararg events: Event): ObjectState {
        val reducer = DefaultObjectStateReducer(inlineBlockId = blockId)
        return reducer.reduce(state = ObjectState.Init, events = events.toList()).state
    }

    //region state

    @Test
    fun `should pick inline block by id when page has more than one query`() {
        val state = reduce(inline.id, showPage(ObjectType.Layout.OBJECT_TYPE))

        assertIs<ObjectState.DataView.Set>(state)
        assertEquals(expected = inline.id, actual = state.dataViewBlock.id)
        assertEquals(expected = listOf(thisObjectFilter), actual = state.viewers.single().filters)
        assertEquals(expected = target, actual = state.sourceObjectId)
        assertEquals(expected = page, actual = state.root)
    }

    @Test
    fun `should show page as error layout without inline block id`() {
        val state = reduce(null, showPage(ObjectType.Layout.OBJECT_TYPE))

        assertSame(expected = ObjectState.ErrorLayout, actual = state)
    }

    @Test
    fun `should show error layout when inline block is missing`() {
        val state = reduce(MockDataFactory.randomUuid(), showPage(ObjectType.Layout.SET))

        assertSame(expected = ObjectState.ErrorLayout, actual = state)
    }

    @Test
    fun `should give collection state when inline block is a collection`() {
        val collection = inline.copy(
            content = (inline.content as DV).copy(isCollection = true)
        )
        val event = showPage(ObjectType.Layout.COLLECTION).copy(
            blocks = listOf(title, otherInline, collection)
        )

        val state = reduce(collection.id, event)

        assertIs<ObjectState.DataView.Collection>(state)
        assertEquals(expected = target, actual = state.sourceObjectId)
    }

    @Test
    fun `should ignore collection change of another inline block`() {
        val state = reduce(
            inline.id,
            showPage(ObjectType.Layout.SET),
            Event.Command.DataView.SetIsCollection(
                context = page,
                dv = otherInline.id,
                isCollection = true
            )
        )

        assertIs<ObjectState.DataView.Set>(state)
        assertEquals(expected = inline.id, actual = state.dataViewBlock.id)
    }

    @Test
    fun `should keep inline block id when inline block becomes a collection`() {
        val state = reduce(
            inline.id,
            showPage(ObjectType.Layout.SET),
            Event.Command.DataView.SetIsCollection(
                context = page,
                dv = inline.id,
                isCollection = true
            )
        )

        assertIs<ObjectState.DataView.Collection>(state)
        assertEquals(expected = inline.id, actual = state.inlineBlockId)
    }

    //endregion

    //region source

    @Test
    fun `should use type as source when inline query targets a type`() {
        val state = reduce(inline.id, showPage(ObjectType.Layout.OBJECT_TYPE))

        assertIs<ObjectState.DataView.Set>(state)
        assertEquals(expected = listOf(target), actual = state.getSetOfValue(ctx = page))
    }

    @Test
    fun `should use set-of of target when inline query targets a set`() {
        val type = MockDataFactory.randomUuid()
        val state = reduce(inline.id, showPage(ObjectType.Layout.SET, setOf = listOf(type)))

        assertIs<ObjectState.DataView.Set>(state)
        assertEquals(expected = listOf(type), actual = state.getSetOfValue(ctx = page))
    }

    @Test
    fun `should keep source without details for inline query`() {
        val type = MockDataFactory.randomUuid()
        val state = reduce(inline.id, showPage(ObjectType.Layout.SET, setOf = listOf(type)))

        assertIs<ObjectState.DataView.Set>(state)
        assertEquals(
            expected = listOf(type),
            actual = state.filterOutDeletedAndMissingObjects(state.getSetOfValue(ctx = page))
        )
    }

    //endregion

    //region placeholders

    private val templates = FilterValueTemplates(objectId = page, participantId = "participant")

    @Test
    fun `should replace this object placeholder in list value`() {
        val result = thisObjectFilter.resolveValueTemplates(templates)

        assertEquals(expected = listOf(page), actual = result.value)
    }

    @Test
    fun `should replace current user placeholder in string value`() {
        val filter = thisObjectFilter.copy(value = FilterValueTemplates.CURRENT_USER)

        val result = filter.resolveValueTemplates(templates)

        assertEquals(expected = "participant", actual = result.value)
    }

    @Test
    fun `should keep placeholder when value is unknown`() {
        val result = thisObjectFilter.resolveValueTemplates(
            FilterValueTemplates(objectId = null, participantId = "participant")
        )

        assertEquals(expected = listOf(FilterValueTemplates.THIS_OBJECT), actual = result.value)
    }

    @Test
    fun `should keep other values`() {
        val filter = thisObjectFilter.copy(value = listOf("a", "b"))

        assertSame(expected = filter, actual = filter.resolveValueTemplates(templates))
    }

    @Test
    fun `should replace placeholder in nested filter for subscription`() = runTest {
        val group = DVFilter(
            relation = "",
            operator = DVFilterOperator.AND,
            condition = DVFilterCondition.NONE,
            nestedFilters = listOf(thisObjectFilter)
        )

        val result = listOf(group).updateFormatForSubscription(
            storeOfRelations = DefaultStoreOfRelations(),
            templates = templates
        )

        assertEquals(expected = listOf(page), actual = result.single().nestedFilters.single().value)
    }

    @Test
    fun `should prefill this object for new record`() = runTest {
        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(StubRelationObject(key = belongsTo, format = Relation.Format.OBJECT)))
        }
        val viewer = StubDataViewView(filters = listOf(thisObjectFilter))

        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = mock<DateProvider>(),
            templates = templates
        )

        assertEquals(expected = mapOf(belongsTo to listOf(page)), actual = prefilled)
    }

    @Test
    fun `should build participant id like middleware`() {
        assertEquals(
            expected = "_participant_bafy_space_account",
            actual = GetCurrentParticipantId.participantId(
                space = SpaceId("bafy.space"),
                account = "account"
            )
        )
    }

    //endregion
}
