package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.relations.DateParser
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import timber.log.Timber

fun ObjectWrapper.Basic.values(
    relations: List<Relation>,
    settings: List<DVViewerRelation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
): List<DefaultObjectRelationValueView> {
    val values = mutableListOf<DefaultObjectRelationValueView>()
    relations.forEach { relation ->
        when (relation.format) {
            Relation.Format.SHORT_TEXT, Relation.Format.LONG_TEXT -> {
                val value = DefaultObjectRelationValueView.Text(
                    objectId = id,
                    relationKey = relation.key,
                    text = getValue<String>(relation.key)
                )
                values.add(value)
            }
            Relation.Format.NUMBER -> {
                val number = map.getOrDefault(key = relation.key, null)
                val value = DefaultObjectRelationValueView.Number(
                    objectId = id,
                    relationKey = relation.key,
                    number = NumberParser.parse(number)
                )
                values.add(value)
            }
            Relation.Format.URL -> {
                val value = DefaultObjectRelationValueView.Url(
                    objectId = id,
                    relationKey = relation.key,
                    url = getValue<String>(relation.key)
                )
                values.add(value)
            }
            Relation.Format.EMAIL -> {
                val value = DefaultObjectRelationValueView.Email(
                    objectId = id,
                    relationKey = relation.key,
                    email = getValue<String>(relation.key)
                )
                values.add(value)
            }
            Relation.Format.PHONE -> {
                val value = DefaultObjectRelationValueView.Phone(
                    objectId = id,
                    relationKey = relation.key,
                    phone = getValue<String>(relation.key)
                )
                values.add(value)
            }
            Relation.Format.CHECKBOX -> {
                val value = DefaultObjectRelationValueView.Checkbox(
                    objectId = id,
                    relationKey = relation.key,
                    isChecked = getValue<Boolean>(relation.key) ?: false
                )
                values.add(value)
            }
            Relation.Format.DATE -> {
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
                    timeInMillis = DateParser.parse(time),
                    dateFormat = format
                )
                values.add(value)
            }
            Relation.Format.STATUS -> {
                val value = DefaultObjectRelationValueView.Status(
                    objectId = id,
                    relationKey = relation.key,
                    status = statuses(relation = relation.key, options = relation.selections)
                )
                values.add(value)
            }
            Relation.Format.TAG -> {
                val value = DefaultObjectRelationValueView.Tag(
                    objectId = id,
                    relationKey = relation.key,
                    tags = tags(relation = relation.key, options = relation.selections)
                )
                values.add(value)
            }
            Relation.Format.FILE -> {
                val value = DefaultObjectRelationValueView.File(
                    objectId = id,
                    relationKey = relation.key,
                    files = files(relation = relation.key, details = details)
                )
                values.add(value)
            }
            Relation.Format.OBJECT -> {
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
            Relation.Format.EMOJI -> {
                // Ignoring this relation format.
            }
            Relation.Format.RELATIONS -> {
                // Ignoring this relation format.
            }
        }
    }

    return values
}

fun ObjectWrapper.Basic.valuesFilteredByHidden(
    relations: List<Relation>,
    settings: List<DVViewerRelation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
): List<DefaultObjectRelationValueView> {
    return values(
        relations = relations.filter { !it.isHidden },
        settings = settings,
        details = details,
        urlBuilder = urlBuilder
    )
}

fun ObjectWrapper.Basic.statuses(
    relation: Id,
    options: List<Relation.Option>
) : List<StatusView> {
    val result = mutableListOf<StatusView>()
    val keys : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(id)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    keys.forEach { key ->
        val option = options.find { it.id == key }
        if (option != null) {
            result.add(
                StatusView(
                    id = option.id,
                    status = option.text,
                    color = option.color
                )
            )
        }
    }
    return result
}

fun ObjectWrapper.Basic.tags(
    relation: Id,
    options: List<Relation.Option>
) : List<TagView> {
    val result = mutableListOf<TagView>()
    val keys : List<Id> = when(val value = map.getOrDefault(relation, null)) {
        is Id -> listOf(id)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
    keys.forEach { key ->
        val option = options.find { it.id == key }
        if (option != null) {
            result.add(
                TagView(
                    id = option.id,
                    tag = option.text,
                    color = option.color
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
        if (wrapper.isDeleted == true) {
            result.add(ObjectView.Deleted(id = id))
        } else {
            result.add(
                ObjectView.Default(
                    id = id,
                    name = wrapper.getProperName(),
                    icon = ObjectIcon.from(
                        obj = wrapper,
                        layout = wrapper.layout,
                        builder = urlBuilder
                    ),
                    types = wrapper.type
                )
            )
        }
    }
    return result
}