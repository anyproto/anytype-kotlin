package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.GroupOrder
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.sets.model.Viewer

/**
 * Group id for objects that have no value for the board's group relation.
 */
const val BOARD_EMPTY_GROUP_ID = "empty"

/**
 * Builds the columns of a [Viewer.Board] (Kanban) view by grouping the loaded
 * records on the viewer's [DVViewer.groupRelationKey].
 *
 * Read-only MVP: columns are derived only from option ids actually present in
 * the records (options with no records are not shown), and a record with a
 * multi-value (Tag) group relation is placed in the first option's column only.
 */
suspend fun DVViewer.buildBoardViews(
    objectIds: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder,
    objectStore: ObjectStore,
    objectOrders: List<ObjectOrder>,
    storeOfRelations: StoreOfRelations,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    stringResourceProvider: StringResourceProvider,
    groupOptions: Map<Id, ObjectWrapper.Option> = emptyMap(),
    groupOrder: GroupOrder? = null
): List<Viewer.Board.Column> {

    val groupRelationKey = groupRelationKey

    val filteredRelations = viewerRelations.mapNotNull { setting ->
        if (setting.isVisible && setting.key != Relations.NAME) {
            relations.find { it.key == setting.key }
        } else {
            null
        }
    }

    // Group valid objects by their value of the group relation, preserving
    // first-seen order of the groups.
    val grouped = LinkedHashMap<Id, MutableList<ObjectWrapper.Basic>>()
    objectIds
        .mapNotNull { objectStore.get(it) }
        .filter { it.isValid }
        .forEach { obj ->
            val groupId = obj.resolveGroupId(groupRelationKey)
            grouped.getOrPut(groupId) { mutableListOf() }.add(obj)
        }

    // Always include a "no value" column so items without a value have a home,
    // and cards can be dragged there to clear their value.
    grouped.getOrPut(BOARD_EMPTY_GROUP_ID) { mutableListOf() }

    // Show every option of the group relation as a column, including options
    // that currently have no records.
    groupOptions.keys.forEach { optionId ->
        grouped.getOrPut(optionId) { mutableListOf() }
    }

    val columns = grouped.map { (groupId, objects) ->
        // Resolve the option's label/color from the loaded relation options first,
        // falling back to the object store, then to the raw group id.
        val mapped = if (groupId != BOARD_EMPTY_GROUP_ID) groupOptions[groupId] else null
        val stored = if (groupId != BOARD_EMPTY_GROUP_ID && mapped == null) objectStore.get(groupId) else null
        val label = if (groupId == BOARD_EMPTY_GROUP_ID) {
            stringResourceProvider.getKanbanEmptyColumnTitle()
        } else {
            mapped?.name?.takeIf { it.isNotBlank() }
                ?: stored?.name?.takeIf { it.isNotBlank() }
                ?: groupId
        }
        val color = mapped?.color?.takeIf { it.isNotBlank() }
            ?: stored?.relationOptionColor?.takeIf { it.isNotBlank() }

        // Order cards within the column by this group's saved object order;
        // objects not present in the order keep their natural (stable) order.
        val groupOrder = objectOrders.find { it.group == groupId }?.ids.orEmpty()
        val orderIndex = groupOrder.withIndex().associate { (index, id) -> id to index }
        val cards = objects
            .sortedBy { orderIndex[it.id] ?: Int.MAX_VALUE }
            .map { obj ->
                val content = obj.buildCardContent(
                    urlBuilder = urlBuilder,
                    viewerRelations = viewerRelations,
                    store = objectStore,
                    filteredRelations = filteredRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes
                )
                Viewer.Board.Card(
                    objectId = obj.id,
                    name = content.name,
                    icon = content.icon,
                    relations = content.relations,
                    hideIcon = hideIcon
                )
            }

        Viewer.Board.Column(
            id = groupId,
            label = label,
            color = color,
            cards = cards
        )
    }

    // If the view has a saved group order, use it: drop hidden groups, sort by
    // index, and append any groups not present in the saved order.
    val viewGroups = groupOrder?.viewGroups.orEmpty()
    if (viewGroups.isNotEmpty()) {
        val hidden = viewGroups.filter { it.isHidden }.map { it.groupId }.toSet()
        val savedRank = viewGroups.withIndex().associate { (index, group) -> group.groupId to index }
        return columns
            .filter { it.id !in hidden }
            .sortedBy { savedRank[it.id] ?: Int.MAX_VALUE }
    }

    // Fallback order: "no value" first, then options in their loaded order, then
    // any remaining (e.g. orphaned) groups in first-seen order.
    val optionOrder = groupOptions.keys.withIndex().associate { (index, id) -> id to index }
    return columns.sortedBy { column ->
        when (column.id) {
            BOARD_EMPTY_GROUP_ID -> -1
            else -> optionOrder[column.id] ?: Int.MAX_VALUE
        }
    }
}

private fun ObjectWrapper.Basic.resolveGroupId(groupRelationKey: String?): Id {
    if (groupRelationKey.isNullOrEmpty()) return BOARD_EMPTY_GROUP_ID
    val groupValue: Id? = when (val value = map[groupRelationKey]) {
        is Id -> value
        is List<*> -> value.typeOf<Id>().firstOrNull()
        else -> null
    }
    return groupValue?.takeIf { it.isNotEmpty() } ?: BOARD_EMPTY_GROUP_ID
}
