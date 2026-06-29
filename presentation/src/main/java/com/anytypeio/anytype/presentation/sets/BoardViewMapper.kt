package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.GroupOrder
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
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
 * Builds the columns of a [Viewer.Board] (Kanban) view from the backend group subscription
 * [groups] — canonical group ids, empty option columns, Checkbox / Tag combination groups,
 * ordering and hidden state. Each column's cards come from its own paged record subscription
 * ([recordsByColumn] / [countsByColumn]). Until the groups load this returns no columns (the
 * board shows its loading/empty state); there is no client-side record-bucketing fallback.
 */
suspend fun DVViewer.buildBoardViews(
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
    groups: List<DataViewGroup> = emptyList(),
    // Per-column record ids + backend totals, one paged subscription per column (keyed by column id).
    recordsByColumn: Map<Id, List<Id>> = emptyMap(),
    countsByColumn: Map<Id, Int> = emptyMap()
): List<Viewer.Board.Column> {
    if (groups.isEmpty()) return emptyList()
    return buildColumnsFromGroups(
        groups = groups,
        recordsByColumn = recordsByColumn,
        countsByColumn = countsByColumn,
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

// region Backend-group-driven columns

private suspend fun DVViewer.buildColumnsFromGroups(
    groups: List<DataViewGroup>,
    recordsByColumn: Map<Id, List<Id>>,
    countsByColumn: Map<Id, Int>,
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
    val filteredRelations = filteredRelations(relations)
    val emptyGroupId = groups.firstOrNull { it.value is DataViewGroup.Value.Empty }?.id ?: BOARD_EMPTY_GROUP_ID
    // Checkbox boards are exhaustively split into true/false groups by the backend — there
    // is no "No value" state, so don't synthesize an empty column (it would render as a live
    // drop target that writes `checked = false`).
    val isCheckbox = groups.any { it.value is DataViewGroup.Value.Checkbox }
    val includeEmptyColumn = !isCheckbox

    val orderedIds = buildList {
        // Preserve the backend (ObjectGroupsSubscribe) order; only synthesize the
        // empty column if the backend didn't return one (and the board isn't checkbox).
        // Final column order is the saved GroupOrder index (see applyGroupOrder).
        if (includeEmptyColumn && groups.none { it.id == emptyGroupId }) add(emptyGroupId)
        groups.forEach { add(it.id) }
    }.distinct()

    val columns = orderedIds.map { gid ->
        val group = groups.firstOrNull { it.id == gid }
        // Each column's records come from its own paged subscription (keyed by column id).
        val recordIds = recordsByColumn[gid].orEmpty()
        val orderIds = objectOrders.find { it.group == gid }?.ids.orEmpty()
        val orderIndex = orderIds.withIndex().associate { (i, id) -> id to i }
        val cards = recordIds
            .mapNotNull { objectStore.get(it) }
            .filter { it.isValid }
            .sortedBy { orderIndex[it.id] ?: Int.MAX_VALUE }
            .map { obj ->
                obj.toCard(urlBuilder, viewerRelations, objectStore, filteredRelations, fieldParser, storeOfObjectTypes, hideIcon)
            }
        Viewer.Board.Column(
            id = gid,
            label = groupLabel(gid, group, emptyGroupId, groupOptions, objectStore, stringResourceProvider),
            color = groupColor(gid, group, groupOrder, groupOptions, objectStore),
            cards = cards,
            count = countsByColumn[gid] ?: cards.size
        )
    }

    val viewGroups = groupOrder?.viewGroups.orEmpty()
    return when {
        // Manual arrangement (drag / hide) always wins.
        viewGroups.isNotEmpty() -> applyGroupOrder(columns, groupOrder)
        // Tag (multi-select) boards: the backend returns groups in local-DB order,
        // which differs per device, so sort deterministically to match desktop.
        groups.any { it.value is DataViewGroup.Value.Tag } ->
            sortGroupsByDefault(columns, groups, emptyGroupId)
        // Other boards (status / checkbox): keep the backend order as-is.
        else -> columns
    }
}

/**
 * Default column order for a Tag (multi-select) board when the user hasn't manually
 * arranged columns (no saved [GroupOrder]). The backend returns groups in local-database
 * order, which differs per device in a local-first app; this produces a deterministic,
 * desktop-matching order instead: the empty ("No value") column first, then multi-tag
 * combination groups before single-tag groups (mirroring the backend's raw-id-length
 * ordering), and alphabetically by label within each tier. Status / checkbox boards keep
 * the backend order.
 */
private fun sortGroupsByDefault(
    columns: List<Viewer.Board.Column>,
    groups: List<DataViewGroup>,
    emptyGroupId: Id
): List<Viewer.Board.Column> {
    val tagCount = groups.associate { group ->
        group.id to ((group.value as? DataViewGroup.Value.Tag)?.ids?.size ?: 1)
    }
    return columns.sortedWith(
        compareBy<Viewer.Board.Column> { if (it.id == emptyGroupId) 0 else 1 }
            .thenByDescending { tagCount[it.id] ?: 1 }
            .thenBy { it.label.lowercase() }
    )
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
    groupOrder: GroupOrder?
): List<Viewer.Board.Column> {
    val viewGroups = groupOrder?.viewGroups.orEmpty()
    // No saved order: keep the backend (ObjectGroupsSubscribe) order as-is.
    if (viewGroups.isEmpty()) return columns
    // Order columns by the saved per-group index (drag-and-drop position); drop
    // hidden groups. Groups without a stored index keep their backend order.
    val hidden = viewGroups.filter { it.isHidden }.map { it.groupId }.toSet()
    val rank = viewGroups.associate { it.groupId to it.index }
    return columns
        .filter { it.id !in hidden }
        .sortedBy { rank[it.id] ?: Int.MAX_VALUE }
}

// endregion
