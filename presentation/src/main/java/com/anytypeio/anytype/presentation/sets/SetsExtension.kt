package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.*
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

fun List<ColumnView>.buildGridRow(
    record: Map<String, Any?>,
    relations: List<Relation>,
    details: Map<Id, Block.Fields>,
    builder: UrlBuilder
): Viewer.GridView.Row {

    val type = record[ObjectSetConfig.TYPE_KEY] as String
    val name = record[ObjectSetConfig.NAME_KEY] as String?
    val emoji = record[ObjectSetConfig.EMOJI_KEY] as String?
    val image = record[ObjectSetConfig.IMAGE_KEY] as String?

    val cells = mutableListOf<CellView>()
    this.map { column ->
        cells.add(
            when (column.format) {
                ColumnView.Format.SHORT_TEXT, ColumnView.Format.LONG_TEXT -> {
                    CellView.Description(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        text = if (record.containsKey(column.key)) {
                            record[column.key].toString()
                        } else {
                            EMPTY_VALUE
                        }
                    )
                }
                ColumnView.Format.NUMBER -> {
                    var number: Int? = null
                    val value = (record[column.key])
                    if (value is String)
                        number = value.toInt()
                    else if (value is Number)
                        number = value.toInt()
                    CellView.Number(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        number = number
                    )
                }
                ColumnView.Format.DATE -> {
                    val format = SimpleDateFormat(MONTH_DAY_AND_YEAR, Locale.US)
                    val time: Long? = (record[column.key] as Double?)?.toLong()
                    val timestamp = if (time != null) time * 1000L else null
                    CellView.Date(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        text = if (timestamp != null) {
                            val date = Date(timestamp)
                            format.format(date)
                        } else {
                            EMPTY_VALUE
                        },
                        timestamp = timestamp,
                        dateFormat = ""
                    )
                }
                ColumnView.Format.FILE -> {
                    val files = record.buildFileViews(
                        columnKey = column.key,
                        details = details
                    )
                    CellView.File(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        files = files
                    )
                }
                ColumnView.Format.CHECKBOX -> {
                    CellView.Description(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        text = if (record.containsKey(column.key)) {
                            record[column.key].toString()
                        } else {
                            EMPTY_VALUE
                        }
                    )
                }
                ColumnView.Format.URL -> {
                    CellView.Url(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        url = record[column.key]?.toString()
                    )
                }
                ColumnView.Format.EMAIL -> {
                    CellView.Email(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        email = record[column.key]?.toString()
                    )
                }
                ColumnView.Format.PHONE -> {
                    CellView.Phone(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        phone = if (record.containsKey(column.key)) {
                            record[column.key].toString()
                        } else {
                            EMPTY_VALUE
                        }
                    )
                }
                ColumnView.Format.OBJECT -> {
                    val objects = record.buildObjectViews(
                        columnKey = column.key,
                        details = details,
                        builder = builder
                    )
                    CellView.Object(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        objects = objects
                    )
                }
                ColumnView.Format.TAG -> {
                    val relation = relations.firstOrNull { it.key == column.key }
                    val tags = record.buildTagViews(
                        selOptions = relation?.selections,
                        columnKey = column.key
                    )
                    CellView.Tag(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        tags = tags
                    )
                }
                ColumnView.Format.STATUS -> {
                    val relation = relations.firstOrNull { it.key == column.key }
                    val status = record.buildStatusViews(
                        selOptions = relation?.selections,
                        columnKey = column.key
                    )
                    CellView.Status(
                        id = record[ObjectSetConfig.ID_KEY] as String,
                        key = column.key,
                        status = status
                    )
                }
                else -> TODO()
            }
        )
    }

    val objectId = record[ObjectSetConfig.ID_KEY] as String

    return Viewer.GridView.Row(
        id = objectId,
        type = type,
        name = name,
        emoji = emoji,
        image = image?.let { if (it.isEmpty()) null else builder.thumbnail(it) },
        cells = cells
    )
}

fun Map<String, Any?>.buildFileViews(
    columnKey: Id,
    details: Map<Id, Block.Fields>
): List<FileView> {
    val files = mutableListOf<FileView>()
    val value = this.getOrDefault(columnKey, null)
    if (value is Id) {
        files.add(
            FileView(
                id = value,
                name = details[value]?.name.orEmpty(),
                mime = details[value]?.fileMimeType.orEmpty(),
                ext = details[value]?.fileExt.orEmpty()
            )
        )
    } else if (value is List<*>) {
        value.typeOf<Id>().forEach { id ->
            files.add(
                FileView(
                    id = id,
                    name = details[id]?.name.orEmpty(),
                    mime = details[id]?.fileMimeType.orEmpty(),
                    ext = details[id]?.fileExt.orEmpty()
                )
            )
        }
    }
    return files
}

fun Map<String, Any?>.buildObjectViews(
    columnKey: Id,
    details: Map<Id, Block.Fields>,
    builder: UrlBuilder
): List<ObjectView> {
    val objects = mutableListOf<ObjectView>()
    val value = this.getOrDefault(columnKey, null)
    if (value is Id) {
        objects.add(
            ObjectView(
                id = value,
                name = details[value]?.name.orEmpty(),
                emoji = details[value]?.iconEmoji,
                image = details[value]?.iconImage?.let {
                    if (it.isEmpty()) null else builder.thumbnail(it)
                }
            )
        )
    } else if (value is List<*>) {
        value.typeOf<Id>().forEach { id ->
            objects.add(
                ObjectView(
                    id = id,
                    name = details[id]?.name.orEmpty(),
                    emoji = details[id]?.iconEmoji,
                    image = details[id]?.iconImage?.let {
                        if (it.isEmpty()) null else builder.thumbnail(it)
                    }
                )
            )
        }
    }
    return objects
}

fun Map<String, Any?>.buildTagViews(
    selOptions: List<Relation.Option>?,
    columnKey: String
): List<TagView> {
    val views = arrayListOf<TagView>()
    getColumnOptions(selOptions, this, columnKey).forEach {
        views.add(
            TagView(
                id = it.id,
                tag = it.text,
                color = it.color
            )
        )
    }
    return views
}

fun Map<String, Any?>.buildStatusViews(
    selOptions: List<Relation.Option>?,
    columnKey: String
): List<StatusView> {
    val views = arrayListOf<StatusView>()
    getColumnOptions(selOptions, this, columnKey).forEach {
        views.add(
            StatusView(
                id = it.id,
                status = it.text,
                color = it.color
            )
        )
    }
    return views
}

private fun getColumnOptions(
    options: List<Relation.Option>?,
    records: Map<String, Any?>,
    columnKey: String
): List<Relation.Option> {
    val columnOptions = arrayListOf<Relation.Option>()
    val record = records.getOrDefault(columnKey, null)
    if (record != null) {
        handleIds(record).forEach { id ->
            options
                ?.firstOrNull { it.id == id }
                ?.let { columnOptions.add(it) }
        }
    }
    return columnOptions
}

private fun handleIds(ids: Any?): List<String> = when (ids) {
    is Id -> listOf(ids)
    is List<*> -> ids.filterIsInstance<Id>()
    else -> emptyList()
}

fun Any?.filterIdsById(filter: Id): List<Id> {
    val remaining = mutableListOf<Id>()
    if (this is List<*>) {
        typeOf<Id>().forEach { id ->
            if (id != filter) remaining.add(id)
        }
    }
    return remaining.toList()
}

fun Any?.addIds(ids: List<Id>): List<Id> {
    val remaining = mutableListOf<Id>()
    if (this is List<*>) {
        remaining.addAll(typeOf())
        remaining.addAll(ids)
    }
    return remaining.toList()
}

const val MONTH_DAY_AND_YEAR = "MMM dd, yyyy"
const val EMPTY_VALUE = ""
val VALUE_MISSING = null