package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldDateParser
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.sets.model.Viewer

/**
 * Builds a Calendar view. The caller MUST validate that [dateRelation] has DATE format and
 * that [DVViewer.groupRelationKey] is non-null before calling this function. When the
 * groupRelationKey is null or resolves to a non-DATE relation, the caller should produce
 * [Viewer.Unsupported] with TYPE_CALENDAR instead.
 *
 * Entries are silently excluded when:
 * - The store has no object for the given id, or `obj.isValid` is false.
 * - The DATE relation value is missing/null.
 * - The DATE relation value is `0` (treated as "no date" — protobuf default for unset DATE).
 */
suspend fun DVViewer.buildCalendarView(
    objectIds: List<Id>,
    objectOrderIds: List<Id>,
    dateRelation: ObjectWrapper.Relation,
    store: ObjectStore,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder,
    untitledPlaceholder: String
): Viewer.CalendarView {

    val dateKey = dateRelation.key

    val nameRelationSetting = viewerRelations.find { it.key == Relations.NAME }
    val hideName = nameRelationSetting?.isVisible != true

    val orderedIds = if (objectOrderIds.isNotEmpty()) {
        val orderMap = objectOrderIds.withIndex().associate { (idx, id) -> id to idx }
        objectIds.sortedBy { orderMap[it] ?: Int.MAX_VALUE }
    } else {
        objectIds
    }

    val entries = mutableListOf<Viewer.CalendarView.Entry>()
    val seenIds = HashSet<Id>()

    for (objId in orderedIds) {
        if (!seenIds.add(objId)) continue
        val obj = store.get(objId) ?: continue
        if (!obj.isValid) continue
        val rawDateValue: Any? = obj.map[dateKey]
        val timestampSeconds: Long = FieldDateParser.parse(rawDateValue)?.single ?: continue
        if (timestampSeconds == 0L) continue
        val resolvedName = fieldParser.getObjectName(obj).ifEmpty { untitledPlaceholder }
        entries.add(
            Viewer.CalendarView.Entry(
                objectId = obj.id,
                name = resolvedName,
                icon = obj.objectIcon(
                    builder = urlBuilder,
                    objType = storeOfObjectTypes.getTypeOfObject(obj)
                ),
                dateInSeconds = timestampSeconds,
                hideIcon = hideIcon,
                hideName = hideName
            )
        )
    }

    return Viewer.CalendarView(
        id = id,
        title = name,
        dateRelationKey = dateKey,
        entries = entries
    )
}
