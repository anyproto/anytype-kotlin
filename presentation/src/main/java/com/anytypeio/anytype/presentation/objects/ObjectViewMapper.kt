package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.isValidObject
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import timber.log.Timber

/**
 * Mapper class for data class @see [com.anytypeio.anytype.presentation.sets.model.ObjectView]
 * that represents a view of an object in case of fields value.
 */
suspend fun Struct.buildRelationValueObjectViews(
    relationKey: Key,
    details: ObjectViewDetails,
    builder: UrlBuilder,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<ObjectView> {
    return this[relationKey]
        .asIdList()
        .mapNotNull { id ->
            details.getObject(id)
                ?.takeIf { it.isValid }
                ?.toObjectView(urlBuilder = builder, fieldParser = fieldParser, storeOfObjectTypes = storeOfObjectTypes)
        }
}

suspend fun Struct.buildObjectViews(
    columnKey: Id,
    store: ObjectStore,
    builder: UrlBuilder,
    withIcon: Boolean = true,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<ObjectView> {
    return this.getOrDefault(columnKey, null)
        .asIdList()
        .mapNotNull { id ->
            val wrapper = store.get(id)
            if (wrapper == null || !wrapper.isValid) {
                Timber.w("Object was missing in object store: $id or was invalid")
                null
            } else if (wrapper.isDeleted == true) {
                ObjectView.Deleted(id = id, name = fieldParser.getObjectName(wrapper))
            } else {
                val icon = if (withIcon) {
                    wrapper.objectIcon(
                        builder = builder,
                        objType = storeOfObjectTypes.getTypeOfObject(wrapper)
                    )
                } else {
                    ObjectIcon.None
                }
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
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<ObjectView> {
    return map.getOrDefault(relation, null)
        .asIdList()
        .mapNotNull { id ->
            storeOfObjects.get(id)
                ?.takeIf { it.isValid }
                ?.toObjectView(urlBuilder, fieldParser, storeOfObjectTypes)
        }
}

suspend fun ObjectWrapper.Relation.toObjects(
    value: Any?,
    store: ObjectStore,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<ObjectView> {
    return value.asIdList().mapNotNull { id ->
        val raw = store.get(id)?.map
        if (raw.isNullOrEmpty() || !raw.isValidObject()) null
        else {
            ObjectWrapper.Basic(raw).toObjectView(urlBuilder, fieldParser, storeOfObjectTypes)
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

/**
 * Converts a Basic wrapper into an ObjectView.
 * isValid check performed already in the caller function.
 */
suspend fun ObjectWrapper.Basic.toObjectView(
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): ObjectView = if (isDeleted == true)
    ObjectView.Deleted(id = id, name = fieldParser.getObjectName(this))
else toObjectViewDefault(urlBuilder, fieldParser, storeOfObjectTypes)

/**
 * Converts a non-deleted Basic wrapper into a Default ObjectView.
 * isValid check performed already in the caller function.
 */
suspend fun ObjectWrapper.Basic.toObjectViewDefault(
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): ObjectView.Default = ObjectView.Default(
    id = id,
    name = fieldParser.getObjectName(this),
    icon = objectIcon(
        builder = urlBuilder,
        objType = storeOfObjectTypes.getTypeOfObject(this)
    ),
    types = type,
    isRelation = layout == ObjectType.Layout.RELATION
)