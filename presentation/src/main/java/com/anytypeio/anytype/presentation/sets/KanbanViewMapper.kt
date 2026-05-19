package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ViewGroup
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelationOptions
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.objects.setTypeRelationIconsAsNone
import com.anytypeio.anytype.presentation.objects.values
import com.anytypeio.anytype.presentation.relations.values as anyValues
import com.anytypeio.anytype.presentation.sets.model.Viewer

const val KANBAN_UNGROUPED_COLUMN_ID = ""

/**
 * Builds a Kanban (Board) view. The caller MUST validate that the viewer's
 * `groupRelationKey` resolves to a STATUS or TAG relation and pass that pre-resolved
 * relation in. When the group relation is missing or has an unsupported format, the
 * caller should produce [Viewer.Unsupported] instead of calling this function.
 *
 * Column layout:
 * 1. One column per non-deleted, non-hidden option of the group relation
 * 2. Column order and visibility are driven by [viewGroups] (the view's `GroupOrder`
 *    from middleware). Options not referenced in [viewGroups] are appended after the
 *    ordered set (so new options created elsewhere stay visible).
 * 3. Ungrouped column is appended last (for objects whose group value is empty)
 *    unless its `groupId` is explicitly hidden in [viewGroups].
 */
suspend fun DVViewer.buildKanbanView(
    objectIds: List<Id>,
    dataViewRelations: List<ObjectWrapper.Relation>,
    objectOrders: List<ObjectOrder>,
    objectOrderIds: List<Id>,
    groupRelation: ObjectWrapper.Relation,
    store: ObjectStore,
    storeOfRelationOptions: StoreOfRelationOptions,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder,
    ungroupedColumnName: String,
    untitledPlaceholder: String,
    /**
     * View-scoped column ordering & visibility from middleware's `GroupOrder.viewGroups`.
     * Each entry's `groupId` matches a relation option id (or [KANBAN_UNGROUPED_COLUMN_ID]
     * for the ungrouped column), `index` defines column position, and `hidden = true`
     * removes the column from the board. When empty, every non-deleted option is shown
     * in store-insertion order plus a trailing ungrouped column.
     */
    viewGroups: List<ViewGroup> = emptyList()
): Viewer.KanbanView {

    val groupKey = groupRelation.key

    // NAME visibility (parity with GalleryViewMapper.kt)
    val nameRelationSetting = viewerRelations.find { it.key == Relations.NAME }
    val hideName = nameRelationSetting?.isVisible != true

    // Relations rendered on each card. NAME is excluded (own title slot). The grouping
    // relation IS included even though it duplicates the column header — its chip is the
    // tap target for changing an object's group (move-between-columns interaction).
    // Dedupe defensively in case viewerRelations has duplicate entries with the same key.
    val seenRelationKeys = HashSet<Key>()
    val filteredRelations = viewerRelations.mapNotNull { setting ->
        if (setting.isVisible && setting.key != Relations.NAME && seenRelationKeys.add(setting.key)) {
            dataViewRelations.find { it.key == setting.key }
        } else null
    }

    // Build the group-order index. Entries are de-duped by groupId (last write wins) and
    // sorted by `index`, so callers can pass viewGroups in any order. The map preserves
    // sorted order for downstream consumers.
    val sortedViewGroups: List<ViewGroup> = viewGroups
        .associateBy { it.groupId }
        .values
        .sortedBy { it.index }
    val viewGroupByGroupId: Map<Id, ViewGroup> = sortedViewGroups.associateBy { it.groupId }
    val hiddenOptionIds: Set<Id> = sortedViewGroups
        .asSequence()
        .filter { it.hidden && it.groupId.isNotEmpty() }
        .map { it.groupId }
        .toHashSet()
    val ungroupedHidden: Boolean = viewGroupByGroupId[KANBAN_UNGROUPED_COLUMN_ID]?.hidden == true

    // Enumerate every option for the group relation from the globally-subscribed options
    // store. `ObjectStore` only contains options that are referenced as dependencies by
    // current data view objects, so it misses options that no object has been tagged with
    // yet — that's why we cannot rely on it for column enumeration on Kanban.
    val visibleOptions = storeOfRelationOptions.getByRelationKey(groupKey)
        .filter { it.isDeleted != true && it.id !in hiddenOptionIds }

    // Sort by viewGroups.index. Options present in viewGroups are ordered by their index;
    // options not yet known to the GroupOrder (e.g. newly created on another device) fall
    // to the end keeping the store's iteration order (stable sort).
    val optionsForRelation = visibleOptions.sortedBy { option ->
        viewGroupByGroupId[option.id]?.index ?: Int.MAX_VALUE
    }
    val validOptionIds: Set<Id> = optionsForRelation.map { it.id }.toHashSet()

    // Index of ALL options across the whole space (every relation's options) so we can
    // resolve Status/Tag chips on cards regardless of which relation they belong to.
    // Without this fallback the chips render as "Select status" because the regular
    // ObjectStore typically doesn't carry option objects unless they came in as
    // dependencies of the current data view subscription.
    val optionsById: Map<Id, ObjectWrapper.Option> = storeOfRelationOptions.getAll()
        .associateBy { it.id }

    // Resolve each object's group assignment from its OWN field value. ObjectOrder is
    // only the manual within-column ordering — it does NOT define which column an object
    // belongs to. Default group assignment for a card is "whichever option id(s) are
    // stored in obj[groupKey]".
    val objectsToOptions = HashMap<Id, Set<Id>>(objectIds.size)
    val resolvedObjects = HashMap<Id, ObjectWrapper.Basic>(objectIds.size)
    for (objId in objectIds) {
        val obj = store.get(objId) ?: continue
        if (!obj.isValid) continue
        resolvedObjects[objId] = obj
        val groupValue: Any? = obj.map[groupKey]
        val groupIds: Set<Id> = groupValue.anyValues<Id>().toHashSet()
        objectsToOptions[objId] = groupIds
    }

    // ObjectOrder lookup is now only used to reorder cards *within* a column.
    val ordersByGroup: Map<Id, List<Id>> = objectOrders.asSequence()
        .filter { it.group.isNotEmpty() }
        .associate { it.group to it.ids }

    // Look up STATUS/TAG relations by key so the card chip patcher can know each card-
    // relation's format without re-querying StoreOfRelations per card.
    val statusTagRelationFormat: Map<Key, Relation.Format> = filteredRelations
        .filter { it.format == Relation.Format.STATUS || it.format == Relation.Format.TAG }
        .associate { it.key to it.format }

    val groupColumns = optionsForRelation.map { option ->
        val candidateIds = objectIds.filter { objId ->
            objectsToOptions[objId]?.contains(option.id) == true
        }
        val ordered = ordersByGroup[option.id]?.let { manual ->
            val orderMap = manual.withIndex().associate { (idx, id) -> id to idx }
            candidateIds.sortedBy { orderMap[it] ?: Int.MAX_VALUE }
        } ?: candidateIds
        val cards = toCards(
            ids = ordered,
            resolvedObjects = resolvedObjects,
            filteredRelations = filteredRelations,
            urlBuilder = urlBuilder,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            hideName = hideName,
            untitledPlaceholder = untitledPlaceholder,
            optionsById = optionsById,
            statusTagRelationFormat = statusTagRelationFormat
        )
        Viewer.KanbanView.Column(
            groupId = option.id,
            name = option.name.orEmpty().ifEmpty { untitledPlaceholder },
            color = option.color.takeIf { it.isNotEmpty() },
            cards = cards
        )
    }

    // Append the ungrouped column unless explicitly hidden by the view's GroupOrder.
    val columns = if (ungroupedHidden) {
        groupColumns
    } else {
        val ungroupedIds = objectIds
            .filter { objId ->
                val groupIds = objectsToOptions[objId] ?: return@filter false
                groupIds.isEmpty() || groupIds.none { it in validOptionIds }
            }
            .sortedByOrder(objectOrderIds)
        val ungroupedCards = toCards(
            ids = ungroupedIds,
            resolvedObjects = resolvedObjects,
            filteredRelations = filteredRelations,
            urlBuilder = urlBuilder,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            hideName = hideName,
            untitledPlaceholder = untitledPlaceholder,
            optionsById = optionsById,
            statusTagRelationFormat = statusTagRelationFormat
        )
        val ungroupedColumn = Viewer.KanbanView.Column(
            groupId = KANBAN_UNGROUPED_COLUMN_ID,
            name = ungroupedColumnName,
            color = null,
            cards = ungroupedCards
        )
        groupColumns + ungroupedColumn
    }

    return Viewer.KanbanView(
        id = id,
        title = name,
        groupRelationKey = groupKey,
        columns = columns
    )
}

private fun List<Id>.sortedByOrder(objectOrderIds: List<Id>): List<Id> {
    if (objectOrderIds.isEmpty()) return this
    val orderMap = objectOrderIds.withIndex().associate { (index, id) -> id to index }
    return sortedBy { orderMap[it] ?: Int.MAX_VALUE }
}

private suspend fun DVViewer.toCards(
    ids: List<Id>,
    resolvedObjects: Map<Id, ObjectWrapper.Basic>,
    filteredRelations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    hideName: Boolean,
    untitledPlaceholder: String,
    optionsById: Map<Id, ObjectWrapper.Option>,
    statusTagRelationFormat: Map<Key, Relation.Format>
): List<Viewer.KanbanView.Card> {
    if (ids.isEmpty()) return emptyList()
    val cards = ArrayList<Viewer.KanbanView.Card>(ids.size)
    // Per-column dedupe: an object id should appear at most once in a single column.
    // Across columns it may appear multiple times legitimately (TAG multi-value).
    val seenInThisColumn = HashSet<Id>(ids.size)
    for (objId in ids) {
        if (!seenInThisColumn.add(objId)) continue
        val obj = resolvedObjects[objId] ?: continue
        cards.add(
            buildCard(
                obj = obj,
                filteredRelations = filteredRelations,
                urlBuilder = urlBuilder,
                store = store,
                storeOfObjectTypes = storeOfObjectTypes,
                fieldParser = fieldParser,
                hideName = hideName,
                untitledPlaceholder = untitledPlaceholder,
                optionsById = optionsById,
                statusTagRelationFormat = statusTagRelationFormat
            )
        )
    }
    return cards
}

private suspend fun DVViewer.buildCard(
    obj: ObjectWrapper.Basic,
    filteredRelations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    hideName: Boolean,
    untitledPlaceholder: String,
    optionsById: Map<Id, ObjectWrapper.Option>,
    statusTagRelationFormat: Map<Key, Relation.Format>
): Viewer.KanbanView.Card {
    val resolvedName = fieldParser.getObjectName(obj).ifEmpty { untitledPlaceholder }
    val rawRelations = obj.values(
        relations = filteredRelations,
        settings = viewerRelations,
        urlBuilder = urlBuilder,
        storeOfObjects = store,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes
    ).setTypeRelationIconsAsNone()
    val patchedRelations = rawRelations.map { rel ->
        val format = statusTagRelationFormat[rel.relationKey] ?: return@map rel
        // `obj.values()` resolves option ids via the regular ObjectStore. Options that
        // weren't subscribed as dependencies aren't present there, so we patch every
        // STATUS/TAG relation by re-resolving against the dedicated options index.
        rel.patchWithOptions(obj = obj, format = format, optionsById = optionsById)
    }
    return Viewer.KanbanView.Card(
        objectId = obj.id,
        name = resolvedName,
        icon = obj.objectIcon(
            builder = urlBuilder,
            objType = storeOfObjectTypes.getTypeOfObject(obj)
        ),
        relations = patchedRelations,
        hideIcon = hideIcon,
        hideName = hideName
    )
}

private fun DefaultObjectRelationValueView.patchWithOptions(
    obj: ObjectWrapper.Basic,
    format: Relation.Format,
    optionsById: Map<Id, ObjectWrapper.Option>
): DefaultObjectRelationValueView {
    val key = relationKey
    val rawValue: Any? = obj.map[key]
    val ids = rawValue.anyValues<Id>()
    val resolved = ids.mapNotNull { id ->
        optionsById[id]?.takeIf { it.isDeleted != true }
    }
    if (resolved.isEmpty()) {
        return DefaultObjectRelationValueView.Empty(objectId = obj.id, relationKey = key)
    }
    return when (format) {
        Relation.Format.TAG -> DefaultObjectRelationValueView.Tag(
            objectId = obj.id,
            relationKey = key,
            tags = resolved.map { TagView(id = it.id, tag = it.name.orEmpty(), color = it.color) }
        )
        else -> DefaultObjectRelationValueView.Status(
            objectId = obj.id,
            relationKey = key,
            status = resolved.map { StatusView(id = it.id, status = it.name.orEmpty(), color = it.color) }
        )
    }
}
