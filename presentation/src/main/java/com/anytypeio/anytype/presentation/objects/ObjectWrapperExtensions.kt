package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MAX_SNIPPET_SIZE
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import timber.log.Timber

suspend fun ObjectWrapper.Basic.values(
    relations: List<ObjectWrapper.Relation>,
    settings: List<DVViewerRelation>,
    urlBuilder: UrlBuilder,
    storeOfObjects: ObjectStore,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<DefaultObjectRelationValueView> {
    val values = mutableListOf<DefaultObjectRelationValueView>()
    relations.forEach { relation ->
        when (relation.format) {
            RelationFormat.SHORT_TEXT, RelationFormat.LONG_TEXT -> {
                val relationValue = getValue<String>(relation.key)
                val value = if (relationValue.isNullOrBlank()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Text(
                        objectId = id,
                        relationKey = relation.key,
                        text = relationValue
                    )
                }
                values.add(value)
            }
            RelationFormat.NUMBER -> {
                val number = map.getOrDefault(key = relation.key, null)
                val value = if (number == null) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Number(
                        objectId = id,
                        relationKey = relation.key,
                        number = NumberParser.parse(number)
                    )
                }
                values.add(value)
            }
            RelationFormat.URL -> {
                val relationValue = getValue<String>(relation.key)
                val value = if (relationValue.isNullOrBlank()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Url(
                        objectId = id,
                        relationKey = relation.key,
                        url = relationValue
                    )
                }
                values.add(value)
            }
            RelationFormat.EMAIL -> {
                val relationValue = getValue<String>(relation.key)
                val value = if (relationValue.isNullOrBlank()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Email(
                        objectId = id,
                        relationKey = relation.key,
                        email = relationValue
                    )
                }
                values.add(value)
            }
            RelationFormat.PHONE -> {
                val relationValue = getValue<String>(relation.key)
                val value = if (relationValue.isNullOrBlank()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Phone(
                        objectId = id,
                        relationKey = relation.key,
                        phone = relationValue
                    )
                }
                values.add(value)
            }
            RelationFormat.CHECKBOX -> {
                val value = DefaultObjectRelationValueView.Checkbox(
                    objectId = id,
                    relationKey = relation.key,
                    isChecked = getValue<Boolean>(relation.key) ?: false
                )
                values.add(value)
            }
            RelationFormat.DATE -> {
                val setting = settings.find { it.key == relation.key }
                val fieldDate = fieldParser.toDate(any = map.getOrDefault(relation.key, null))
                val value = if (fieldDate == null) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Date(
                        objectId = id,
                        relationKey = relation.key,
                        timeInMillis = fieldDate.timestamp.inMillis,
                        isTimeIncluded = setting?.isDateIncludeTime == true,
                        relativeDate = fieldDate.relativeDate
                    )
                }
                values.add(value)
            }
            RelationFormat.STATUS -> {
                val statuses = statuses(
                    relation = relation.key,
                    storeOfObjects = storeOfObjects
                )
                val value = if (statuses.isEmpty()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Status(
                        objectId = id,
                        relationKey = relation.key,
                        status = statuses
                    )
                }
                values.add(value)
            }
            RelationFormat.TAG -> {
                val tags = tags(
                    relation = relation.key,
                    storeOfObjects = storeOfObjects
                )
                val value = if (tags.isEmpty()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Tag(
                        objectId = id,
                        relationKey = relation.key,
                        tags = tags
                    )
                }
                values.add(value)
            }
            RelationFormat.FILE -> {
                val files = files(
                    relation = relation.key,
                    storeOfObjects = storeOfObjects
                )
                val value = if (files.isEmpty()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.File(
                        objectId = id,
                        relationKey = relation.key,
                        files = files
                    )

                }
                values.add(value)
            }
            RelationFormat.OBJECT -> {
                val objects = objects(
                    relation = relation.key,
                    urlBuilder = urlBuilder,
                    storeOfObjects = storeOfObjects,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes
                )
                val value = if (objects.isEmpty()) {
                    DefaultObjectRelationValueView.Empty(
                        objectId = id,
                        relationKey = relation.key,
                    )
                } else {
                    DefaultObjectRelationValueView.Object(
                        objectId = id,
                        relationKey = relation.key,
                        objects = objects
                    )
                }
                values.add(value)
            }
            else -> {
                Timber.w("Skipping relation format: ${relation.format}")
            }
        }
    }

    return values
}

suspend fun ObjectWrapper.Basic.relationsFilteredByHiddenAndDescription(
    relations: List<ObjectWrapper.Relation>,
    settings: List<DVViewerRelation>,
    urlBuilder: UrlBuilder,
    storeOfObjects: ObjectStore,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<DefaultObjectRelationValueView> {
    return values(
        relations = relations.filter { it.isHidden != true && it.key != Relations.DESCRIPTION },
        settings = settings,
        urlBuilder = urlBuilder,
        storeOfObjects = storeOfObjects,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes
    )
}

suspend fun ObjectWrapper.Basic.statuses(
    relation: Id,
    storeOfObjects: ObjectStore
) : List<StatusView> {
    val result = mutableListOf<StatusView>()
    val keys : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(id)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    keys.forEach { key ->
        val option = storeOfObjects.get(key)
        if (option != null && option.isDeleted != true) {
            result.add(
                StatusView(
                    id = option.id,
                    status = option.name.orEmpty(),
                    color = option.relationOptionColor.orEmpty()
                )
            )
        }
    }
    return result
}

suspend fun ObjectWrapper.Basic.tags(
    relation: Id,
    storeOfObjects: ObjectStore
) : List<TagView> {
    val result = mutableListOf<TagView>()
    val keys : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(id)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    keys.forEach { key ->
        val option = storeOfObjects.get(key)
        if (option != null && option.isDeleted != true) {
            result.add(
                TagView(
                    id = option.id,
                    tag = option.name.orEmpty(),
                    color = option.relationOptionColor.orEmpty()
                )
            )
        }
    }
    return result
}

suspend fun ObjectWrapper.Basic.files(
    relation: Id,
    storeOfObjects: ObjectStore
) : List<FileView> {
    val result = mutableListOf<FileView>()
    val ids : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(id)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    ids.forEach { id ->
        val obj = storeOfObjects.get(id)
        if (obj != null) {
            result.add(
                FileView(
                    id = obj.id,
                    name = obj.name.orEmpty(),
                    mime = obj.fileMimeType.orEmpty(),
                    ext = obj.fileExt.orEmpty(),
                    icon = ObjectIcon.File(
                        mime = obj.fileMimeType.orEmpty(),
                        extensions = obj.fileExt.orEmpty()
                    )
                )
            )
        } else {
            Timber.e("Details missing for a file.")
        }
    }
    return result
}

fun ObjectWrapper.Basic.getDescriptionOrSnippet(): String? {
    return when (layout) {
        ObjectType.Layout.NOTE -> description
        else -> {
            if (!description.isNullOrBlank()) {
                description
            } else {
                snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
            }
        }
    }
}

fun List<DefaultObjectRelationValueView>.setTypeRelationIconsAsNone(): List<DefaultObjectRelationValueView> {
    return this.map { view ->
        if (view.relationKey == Relations.TYPE) {
            handleTypeRelation(view)
        } else {
            view
        }
    }
}

private fun handleTypeRelation(view: DefaultObjectRelationValueView): DefaultObjectRelationValueView {
    return when (view) {
        is DefaultObjectRelationValueView.Object -> {
            view.copy(
                objects = view.objects.map { obj -> updateObjectIcon(obj) }
            )
        }
        else -> view
    }
}

private fun updateObjectIcon(obj: ObjectView): ObjectView {
    return when (obj) {
        is ObjectView.Default -> obj.copy(icon = ObjectIcon.None)
        is ObjectView.Deleted -> obj
    }
}