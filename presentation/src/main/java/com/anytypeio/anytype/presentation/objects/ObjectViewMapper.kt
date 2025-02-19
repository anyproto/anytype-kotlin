package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.toObjectView
import timber.log.Timber

/**
 * Mapper class for data class @see [com.anytypeio.anytype.presentation.sets.model.ObjectView]
 * that represents a view of an object in case of fields value.
 */
fun Struct.buildRelationValueObjectViews(
    relationKey: Key,
    details: ObjectViewDetails,
    builder: UrlBuilder,
    fieldParser: FieldParser
): List<ObjectView> {
    return this[relationKey]
        .asIdList()
        .mapNotNull { id ->
            details.getObject(id)
                ?.takeIf { it.isValid }
                ?.toObjectView(urlBuilder = builder, fieldParser = fieldParser)
        }
}

suspend fun Struct.buildObjectViews(
    columnKey: Id,
    store: ObjectStore,
    builder: UrlBuilder,
    withIcon: Boolean = true,
    fieldParser: FieldParser
): List<ObjectView> {
    return this.getOrDefault(columnKey, null)
        .asIdList()
        .mapNotNull { id ->
            val wrapper = store.get(id)
            if (wrapper == null) {
                Timber.w("Object was missing in object store: $id")
                null
            } else if (wrapper.isDeleted == true) {
                ObjectView.Deleted(id = id, name = fieldParser.getObjectName(wrapper))
            } else {
                val icon = if (withIcon) wrapper.objectIcon(builder) else ObjectIcon.None
                ObjectView.Default(
                    id = id,
                    name = fieldParser.getObjectName(wrapper),
                    icon = icon,
                    types = wrapper.type
                )
            }
        }
}

suspend fun ObjectWrapper.Basic.objects(
    relation: Id,
    urlBuilder: UrlBuilder,
    storeOfObjects: ObjectStore,
    fieldParser: FieldParser
): List<ObjectView> {
    return map.getOrDefault(relation, null)
        .asIdList()
        .mapNotNull { id ->
            storeOfObjects.get(id)
                ?.takeIf { it.isValid }
                ?.toObjectView(urlBuilder, fieldParser)
        }
}

suspend fun ObjectWrapper.Relation.toObjects(
    value: Any?,
    store: ObjectStore,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser
): List<ObjectView> {
    return value.asIdList().mapNotNull { id ->
        val raw = store.get(id)?.map
        if (raw.isNullOrEmpty()) return@mapNotNull null
        val wrapper = ObjectWrapper.Basic(raw)
        if (isDeleted == true) {
            ObjectView.Deleted(id = id, name = fieldParser.getObjectName(wrapper))
        } else {
            ObjectView.Default(
                id = id,
                name = fieldParser.getObjectName(wrapper),
                icon = wrapper.objectIcon(urlBuilder),
                types = type,
                isRelation = wrapper.layout == ObjectType.Layout.RELATION
            )
        }
    }
}

/**
 * Converts any value into a list of Ids.
 * Supports a single Id, a Collection (e.g. List) of Ids, or a Map whose values are Ids.
 */
private fun Any?.asIdList(): List<Id> = when (this) {
    is Id -> listOf(this)
    is Collection<*> -> this.filterIsInstance<Id>()
    is Map<*, *> -> this.values.filterIsInstance<Id>()
    else -> emptyList()
}