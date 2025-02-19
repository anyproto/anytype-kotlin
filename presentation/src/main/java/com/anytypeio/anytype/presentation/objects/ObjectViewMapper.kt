package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.relations.values
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.toObjectView
import timber.log.Timber

/**
 * Mapper class for data class @see [com.anytypeio.anytype.presentation.sets.model.ObjectView]
 * that represents a view of an object in case of fields value
 */

fun Struct.buildRelationValueObjectViews(
    relationKey: Id,
    details: ObjectViewDetails,
    builder: UrlBuilder,
    fieldParser: FieldParser
): List<ObjectView> {

    val ids = when (val value = this[relationKey]) {
        is Id -> listOf(value)
        is List<*> -> value.typeOf<Id>()
        else -> emptyList()
    }

    return ids.mapNotNull { id ->
        details.getObject(id)?.takeIf { it.isValid }
            ?.toObjectView(
                urlBuilder = builder,
                fieldParser = fieldParser
            )
    }
}

suspend fun Struct.buildObjectViews(
    columnKey: Id,
    store: ObjectStore,
    builder: UrlBuilder,
    withIcon: Boolean = true,
    fieldParser: FieldParser
): List<ObjectView> {
    val objects = mutableListOf<ObjectView>()
    val value = this.getOrDefault(columnKey, null)
    if (value is Id) {
        val wrapper = store.get(value)
        if (wrapper != null) {
            if (wrapper.isDeleted == true) {
                objects.add(ObjectView.Deleted(id = value, name = fieldParser.getObjectName(wrapper)))
            } else {
                val icon = if (withIcon) {
                    wrapper.objectIcon(builder)
                } else {
                    ObjectIcon.None
                }
                objects.add(
                    ObjectView.Default(
                        id = value,
                        name = fieldParser.getObjectName(wrapper),
                        icon = icon,
                        types = wrapper.type
                    )
                )
            }
        } else {
            Timber.Forest.w("Object was missing in object store: $value")
        }
    } else if (value is List<*>) {
        value.typeOf<Id>().forEach { id ->
            val wrapper = store.get(id)
            if (wrapper != null) {
                if (wrapper.isDeleted == true) {
                    objects.add(ObjectView.Deleted(id = id, name = fieldParser.getObjectName(wrapper)))
                } else {
                    val icon = if (withIcon) {
                        wrapper.objectIcon(builder)
                    } else {
                        ObjectIcon.None
                    }
                    objects.add(
                        ObjectView.Default(
                            id = id,
                            name = fieldParser.getObjectName(wrapper),
                            icon = icon,
                            types = wrapper.type
                        )
                    )
                }
            } else {
                Timber.Forest.w("Object was missing in object store: $id")
            }
        }
    }
    return objects
}

suspend fun ObjectWrapper.Basic.objects(
    relation: Id,
    urlBuilder: UrlBuilder,
    storeOfObjects: ObjectStore,
    fieldParser: FieldParser
) : List<ObjectView> {
    val result = mutableListOf<ObjectView>()

    val ids : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(value)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    ids.forEach { id ->
        val wrapper = storeOfObjects.get(id) ?: return@forEach
        if (wrapper.isValid) {
            result.add(wrapper.toObjectView(urlBuilder, fieldParser))
        }
    }
    return result
}

suspend fun ObjectWrapper.Relation.toObjects(
    value: Any?,
    store: ObjectStore,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser
) : List<ObjectView> {
    val ids = value.values<Id>()
    return buildList {
        ids.forEach { id ->
            val raw = store.get(id)?.map
            if (!raw.isNullOrEmpty()) {
                val wrapper = ObjectWrapper.Basic(raw)
                val obj = when (isDeleted) {
                    true -> ObjectView.Deleted(
                        id = id,
                        name = fieldParser.getObjectName(wrapper),
                    )
                    else -> ObjectView.Default(
                        id = id,
                        name = fieldParser.getObjectName(wrapper),
                        icon = wrapper.objectIcon(urlBuilder),
                        types = type,
                        isRelation = wrapper.layout == ObjectType.Layout.RELATION
                    )
                }
                add(obj)
            }
        }
    }
}