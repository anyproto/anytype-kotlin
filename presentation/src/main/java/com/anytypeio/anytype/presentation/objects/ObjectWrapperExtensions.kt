package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.relations.DateParser
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.presentation.sets.toObjectView
import timber.log.Timber

suspend fun ObjectWrapper.Basic.values(
    relations: List<ObjectWrapper.Relation>,
    settings: List<DVViewerRelation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder,
    storeOfObjects: ObjectStore
): List<DefaultObjectRelationValueView> {
    val values = mutableListOf<DefaultObjectRelationValueView>()
    relations.forEach { relation ->
        when (relation.format) {
            RelationFormat.SHORT_TEXT, RelationFormat.LONG_TEXT -> {
                val value = DefaultObjectRelationValueView.Text(
                    objectId = id,
                    relationKey = relation.key,
                    text = getValue<String>(relation.key)
                )
                values.add(value)
            }
            RelationFormat.NUMBER -> {
                val number = map.getOrDefault(key = relation.key, null)
                val value = DefaultObjectRelationValueView.Number(
                    objectId = id,
                    relationKey = relation.key,
                    number = NumberParser.parse(number)
                )
                values.add(value)
            }
            RelationFormat.URL -> {
                val value = DefaultObjectRelationValueView.Url(
                    objectId = id,
                    relationKey = relation.key,
                    url = getValue<String>(relation.key)
                )
                values.add(value)
            }
            RelationFormat.EMAIL -> {
                val value = DefaultObjectRelationValueView.Email(
                    objectId = id,
                    relationKey = relation.key,
                    email = getValue<String>(relation.key)
                )
                values.add(value)
            }
            RelationFormat.PHONE -> {
                val value = DefaultObjectRelationValueView.Phone(
                    objectId = id,
                    relationKey = relation.key,
                    phone = getValue<String>(relation.key)
                )
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
                val format: String
                val dateFormat: String

                if (setting != null) {
                    dateFormat = setting.dateFormat?.format ?: DateConst.DEFAULT_DATE_FORMAT
                    format = if (setting.isDateIncludeTime == true) {
                        if (setting.timeFormat == Block.Content.DataView.TimeFormat.H12) {
                            dateFormat + DateConst.DATE_FORMAT_SPACE + DateConst.TIME_H12
                        } else {
                            dateFormat + DateConst.DATE_FORMAT_SPACE + DateConst.TIME_H24
                        }
                    } else {
                        dateFormat
                    }
                } else {
                    format = DateConst.DEFAULT_DATE_FORMAT
                }

                val time = map.getOrDefault(key = relation.key, null)

                val value = DefaultObjectRelationValueView.Date(
                    objectId = id,
                    relationKey = relation.key,
                    timeInMillis = DateParser.parseInMillis(time),
                    dateFormat = format
                )
                values.add(value)
            }
            RelationFormat.STATUS -> {
                val value = DefaultObjectRelationValueView.Status(
                    objectId = id,
                    relationKey = relation.key,
                    status = statuses(
                        relation = relation.key,
                        storeOfObjects = storeOfObjects
                    )
                )
                values.add(value)
            }
            RelationFormat.TAG -> {
                val value = DefaultObjectRelationValueView.Tag(
                    objectId = id,
                    relationKey = relation.key,
                    tags = tags(
                        relation = relation.key,
                        storeOfObjects = storeOfObjects
                    )
                )
                values.add(value)
            }
            RelationFormat.FILE -> {
                val value = DefaultObjectRelationValueView.File(
                    objectId = id,
                    relationKey = relation.key,
                    files = files(relation = relation.key, details = details)
                )
                values.add(value)
            }
            RelationFormat.OBJECT -> {
                val value = DefaultObjectRelationValueView.Object(
                    objectId = id,
                    relationKey = relation.key,
                    objects = objects(
                        relation = relation.key,
                        details = details,
                        urlBuilder = urlBuilder
                    )
                )
                values.add(value)
            }
            else -> {
                Timber.w("Skipping relation format: ${relation.format}")
            }
        }
    }

    return values
}

suspend fun ObjectWrapper.Basic.valuesFilteredByHidden(
    relations: List<ObjectWrapper.Relation>,
    settings: List<DVViewerRelation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder,
    storeOfObjects: ObjectStore
): List<DefaultObjectRelationValueView> {
    return values(
        relations = relations.filter { it.isHidden != true },
        settings = settings,
        details = details,
        urlBuilder = urlBuilder,
        storeOfObjects = storeOfObjects
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
        if (option != null) {
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
        if (option != null) {
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

fun ObjectWrapper.Basic.files(
    relation: Id,
    details: Map<Id, Block.Fields>
) : List<FileView> {
    val result = mutableListOf<FileView>()
    val ids : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(id)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    ids.forEach { id ->
        val data = details[id]
        if (data != null) {
            val obj = ObjectWrapper.Basic(data.map)
            result.add(
                FileView(
                    id = obj.id,
                    name = obj.name.orEmpty(),
                    mime = obj.fileMimeType.orEmpty(),
                    ext = obj.fileExt.orEmpty()
                )
            )
        } else {
            Timber.e("Details missing for a file.")
        }
    }
    return result
}

fun ObjectWrapper.Basic.objects(
    relation: Id,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
) : List<ObjectView> {
    val result = mutableListOf<ObjectView>()

    val ids : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(value)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }

    ids.forEach { id ->
        val wrapper = ObjectWrapper.Basic(details[id]?.map ?: return@forEach)
        result.add(wrapper.toObjectView(urlBuilder))
    }
    return result
}