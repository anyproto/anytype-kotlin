package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DataViewGroup
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
 * Builds the columns of a [Viewer.Board] (Kanban) view.
 *
 * When [groups] (from the backend group subscription) are present, columns are
 * built from them — canonical group ids, empty option columns, Checkbox / Tag
 * combination groups, ordering and hidden state. Otherwise (groups not loaded
 * yet) falls back to deriving columns client-side from the loaded records.
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
    groupOrder: GroupOrder? = null,
    groups: List<DataViewGroup> = emptyList()
): List<Viewer.Board.Column> {
    return if (groups.isNotEmpty()) {
        buildColumnsFromGroups(
            groups = groups,
            objectIds = objectIds,
            relations = relations,
            urlBuilder = urlBuilder,
            objectStore = objectStore,
            objectOrders = objectOrders,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = groupOptions,
            groupOrder = groupOrder
        )
    } else {
        buildColumnsFromRecords(
            objectIds = objectIds,
            relations = relations,
            urlBuilder = urlBuilder,
            objectStore = objectStore,
            objectOrders = objectOrders,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = groupOptions,
            groupOrder = groupOrder
        )
    }
}

// region Backend-group-driven columns

private suspend fun DVViewer.buildColumnsFromGroups(
    groups: List<DataViewGroup>,
    objectIds: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder,
    objectStore: ObjectStore,
    objectOrders: List<ObjectOrder>,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    stringResourceProvider: StringResourceProvider,
    groupOptions: Map<Id, ObjectWrapper.Option>,
    groupOrder: GroupOrder?
): List<Viewer.Board.Column> {
    val key = groupRelationKey
    val filteredRelations = filteredRelations(relations)
    val emptyGroupId = groups.firstOrNull { it.value is DataViewGroup.Value.Empty }?.id ?: BOARD_EMPTY_GROUP_ID

    val records = objectIds.mapNotNull { objectStore.get(it) }.filter { it.isValid }

    val byGroup = LinkedHashMap<Id, MutableList<ObjectWrapper.Basic>>()
    byGroup.getOrPut(emptyGroupId) { mutableListOf() }
    groups.forEach { byGroup.getOrPut(it.id) { mutableListOf() } }
    records.forEach { obj ->
        val gid = matchGroupId(obj, key, groups, emptyGroupId)
        byGroup.getOrPut(gid) { mutableListOf() }.add(obj)
    }

    val orderedIds = buildList {
        add(emptyGroupId)
        groups.forEach { if (it.id != emptyGroupId) add(it.id) }
        byGroup.keys.forEach { id -> if (id != emptyGroupId && groups.none { it.id == id }) add(id) }
    }.distinct()

    val columns = orderedIds.map { gid ->
        val group = groups.firstOrNull { it.id == gid }
        val orderIds = objectOrders.find { it.group == gid }?.ids.orEmpty()
        val orderIndex = orderIds.withIndex().associate { (i, id) -> id to i }
        val cards = (byGroup[gid] ?: emptyList())
            .sortedBy { orderIndex[it.id] ?: Int.MAX_VALUE }
            .map { obj ->
                obj.toCard(urlBuilder, viewerRelations, objectStore, filteredRelations, fieldParser, storeOfObjectTypes, hideIcon)
            }
        Viewer.Board.Column(
            id = gid,
            label = groupLabel(gid, group, emptyGroupId, groupOptions, objectStore, stringResourceProvider),
            color = groupColor(gid, group, groupOrder, groupOptions, objectStore),
            cards = cards
        )
    }

    return applyGroupOrder(columns, groupOrder, emptyGroupId)
}

private fun matchGroupId(
    obj: ObjectWrapper.Basic,
    key: String?,
    groups: List<DataViewGroup>,
    emptyGroupId: Id
): Id {
    if (key.isNullOrEmpty()) return emptyGroupId
    val raw = obj.map[key]
    val ids: List<Id> = when (raw) {
        is Id -> if (raw.isNotEmpty()) listOf(raw) else emptyList()
        is List<*> -> raw.typeOf()
        else -> emptyList()
    }
    val bool = raw as? Boolean
    val match = groups.firstOrNull { group ->
        when (val v = group.value) {
            is DataViewGroup.Value.Status -> ids.contains(v.id)
            is DataViewGroup.Value.Tag -> ids.isNotEmpty() && ids.toSet() == v.ids.toSet()
            is DataViewGroup.Value.Checkbox -> (bool ?: false) == v.checked
            is DataViewGroup.Value.Empty -> ids.isEmpty() && bool == null
            is DataViewGroup.Value.Date -> false
        }
    }
    return match?.id ?: emptyGroupId
}

private suspend fun groupLabel(
    gid: Id,
    group: DataViewGroup?,
    emptyGroupId: Id,
    groupOptions: Map<Id, ObjectWrapper.Option>,
    store: ObjectStore,
    stringResourceProvider: StringResourceProvider
): String {
    if (gid == emptyGroupId) return stringResourceProvider.getKanbanEmptyColumnTitle()
    return when (val v = group?.value) {
        is DataViewGroup.Value.Status ->
            optionName(v.id, groupOptions, store) ?: stringResourceProvider.getKanbanEmptyColumnTitle()
        is DataViewGroup.Value.Tag ->
            v.ids.mapNotNull { optionName(it, groupOptions, store) }
                .joinToString(", ")
                .ifBlank { stringResourceProvider.getKanbanEmptyColumnTitle() }
        is DataViewGroup.Value.Checkbox -> stringResourceProvider.getKanbanCheckboxGroupTitle(v.checked)
        is DataViewGroup.Value.Date -> gid
        is DataViewGroup.Value.Empty, null -> stringResourceProvider.getKanbanEmptyColumnTitle()
    }
}

private suspend fun groupColor(
    gid: Id,
    group: DataViewGroup?,
    groupOrder: GroupOrder?,
    groupOptions: Map<Id, ObjectWrapper.Option>,
    store: ObjectStore
): String? {
    val background = groupOrder?.viewGroups
        ?.firstOrNull { it.groupId == gid }
        ?.backgroundColor
        ?.takeIf { it.isNotBlank() }
    if (background != null) return background
    return when (val v = group?.value) {
        is DataViewGroup.Value.Status -> optionColor(v.id, groupOptions, store)
        is DataViewGroup.Value.Tag -> v.ids.firstNotNullOfOrNull { optionColor(it, groupOptions, store) }
        else -> null
    }
}

private suspend fun optionName(
    id: Id,
    groupOptions: Map<Id, ObjectWrapper.Option>,
    store: ObjectStore
): String? = groupOptions[id]?.name?.takeIf { it.isNotBlank() }
    ?: store.get(id)?.name?.takeIf { it.isNotBlank() }

private suspend fun optionColor(
    id: Id,
    groupOptions: Map<Id, ObjectWrapper.Option>,
    store: ObjectStore
): String? = groupOptions[id]?.color?.takeIf { it.isNotBlank() }
    ?: store.get(id)?.relationOptionColor?.takeIf { it.isNotBlank() }

// endregion

// region Client-side fallback (groups not yet loaded)

private suspend fun DVViewer.buildColumnsFromRecords(
    objectIds: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder,
    objectStore: ObjectStore,
    objectOrders: List<ObjectOrder>,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    stringResourceProvider: StringResourceProvider,
    groupOptions: Map<Id, ObjectWrapper.Option>,
    groupOrder: GroupOrder?
): List<Viewer.Board.Column> {
    val groupRelationKey = groupRelationKey
    val filteredRelations = filteredRelations(relations)

    val grouped = LinkedHashMap<Id, MutableList<ObjectWrapper.Basic>>()
    objectIds
        .mapNotNull { objectStore.get(it) }
        .filter { it.isValid }
        .forEach { obj ->
            grouped.getOrPut(obj.resolveGroupId(groupRelationKey)) { mutableListOf() }.add(obj)
        }

    grouped.getOrPut(BOARD_EMPTY_GROUP_ID) { mutableListOf() }
    groupOptions.keys.forEach { optionId -> grouped.getOrPut(optionId) { mutableListOf() } }

    val columns = grouped.map { (groupId, objects) ->
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

        val orderIds = objectOrders.find { it.group == groupId }?.ids.orEmpty()
        val orderIndex = orderIds.withIndex().associate { (index, id) -> id to index }
        val cards = objects
            .sortedBy { orderIndex[it.id] ?: Int.MAX_VALUE }
            .map { obj ->
                obj.toCard(urlBuilder, viewerRelations, objectStore, filteredRelations, fieldParser, storeOfObjectTypes, hideIcon)
            }

        Viewer.Board.Column(id = groupId, label = label, color = color, cards = cards)
    }

    val viewGroups = groupOrder?.viewGroups.orEmpty()
    if (viewGroups.isNotEmpty()) {
        return applyGroupOrder(columns, groupOrder, BOARD_EMPTY_GROUP_ID)
    }
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

// endregion

// region Shared helpers

private fun DVViewer.filteredRelations(
    relations: List<ObjectWrapper.Relation>
): List<ObjectWrapper.Relation> = viewerRelations.mapNotNull { setting ->
    if (setting.isVisible && setting.key != Relations.NAME) {
        relations.find { it.key == setting.key }
    } else {
        null
    }
}

private suspend fun ObjectWrapper.Basic.toCard(
    urlBuilder: UrlBuilder,
    viewerRelations: List<com.anytypeio.anytype.core_models.DVViewerRelation>,
    store: ObjectStore,
    filteredRelations: List<ObjectWrapper.Relation>,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    hideIcon: Boolean
): Viewer.Board.Card {
    val content = buildCardContent(
        urlBuilder = urlBuilder,
        viewerRelations = viewerRelations,
        store = store,
        filteredRelations = filteredRelations,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes
    )
    return Viewer.Board.Card(
        objectId = id,
        name = content.name,
        icon = content.icon,
        relations = content.relations,
        hideIcon = hideIcon
    )
}

private fun applyGroupOrder(
    columns: List<Viewer.Board.Column>,
    groupOrder: GroupOrder?,
    emptyGroupId: Id
): List<Viewer.Board.Column> {
    val viewGroups = groupOrder?.viewGroups.orEmpty()
    if (viewGroups.isNotEmpty()) {
        val hidden = viewGroups.filter { it.isHidden }.map { it.groupId }.toSet()
        val rank = viewGroups.withIndex().associate { (index, group) -> group.groupId to index }
        return columns
            .filter { it.id !in hidden }
            .sortedBy { rank[it.id] ?: Int.MAX_VALUE }
    }
    // No saved order: keep the "no value" column first, preserve the rest.
    return columns.sortedBy { if (it.id == emptyGroupId) 0 else 1 }
}

// endregion
