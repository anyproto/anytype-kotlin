package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.Relations.NUMBER_DEFAULT_VALUE
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.sets.*
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun List<ObjectWrapper.Relation>.views(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    featured: List<Id> = emptyList()
): List<DocumentRelationView> = mapNotNull { relation ->
    relation.view(
        details = details,
        values = values,
        urlBuilder = urlBuilder,
        isFeatured = featured.contains(relation.key)
    )
}

fun ObjectWrapper.Relation.view(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    isFeatured: Boolean = false
): DocumentRelationView? {
    val relation = this
    return when {
        relation.isHidden == true -> null
        relation.format == RelationFormat.OBJECT -> {
            val objects = values.buildRelationValueObjectViews(
                relationKey = relation.key,
                details = details.details,
                builder = urlBuilder
            )
            DocumentRelationView.Object(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                objects = objects,
                isFeatured = isFeatured
            )
        }
        relation.format == RelationFormat.FILE -> {
            val files = values.buildFileViews(
                relationKey = relation.key,
                details = details.details
            )
            DocumentRelationView.File(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                files = files,
                isFeatured = isFeatured
            )
        }
        relation.format == RelationFormat.DATE -> {
            //TODO In DataView Relation Date uses DateFormat and TimeFormat
            // so SimpleDateFormat can be different from what we have here
            // see {SetsExtension:buildGridRow()}
            val format = SimpleDateFormat(DateConst.DEFAULT_DATE_FORMAT, Locale.getDefault())
            val value = values[relation.key]

            val timeInSec = DateParser.parse(value)
            val formattedDate = if (timeInSec != null) {
                format.format(Date(TimeUnit.SECONDS.toMillis(timeInSec)))
            } else {
                null
            }
            DocumentRelationView.Default(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                value = formattedDate,
                isFeatured = isFeatured,
                format = relation.format
            )
        }
        relation.format == RelationFormat.STATUS -> {
            val options = buildList {
                when(val value = values[relation.key]) {
                    is Id -> {
                        val status = details.details[value]
                        if (status != null && status.map.isNotEmpty()) {
                            add(
                                ObjectWrapper.Option(status.map)
                            )
                        }
                    }
                    is List<*> -> {
                        value.forEach { id ->
                            val status = details.details[id]
                            if (status != null && status.map.isNotEmpty()) {
                                add(
                                    ObjectWrapper.Option(status.map)
                                )
                            }
                        }
                    }
                }
            }
            val status = values.buildStatusViews(
                options = options,
                relationKey = relation.key
            )
            DocumentRelationView.Status(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                status = status,
                isFeatured = isFeatured
            )
        }
        relation.format == RelationFormat.TAG -> {
            val options = buildList {
                when(val value = values[relation.key]) {
                    is Id -> {
                        val status = details.details[value]
                        if (status != null && status.map.isNotEmpty()) {
                            add(
                                ObjectWrapper.Option(status.map)
                            )
                        }
                    }
                    is List<*> -> {
                        value.forEach { id ->
                            val status = details.details[id]
                            if (status != null && status.map.isNotEmpty()) {
                                add(
                                    ObjectWrapper.Option(status.map)
                                )
                            }
                        }
                    }
                }
            }
            val tags = values.buildTagViews(
                options = options,
                relationKey = relation.key
            )
            DocumentRelationView.Tags(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                tags = tags,
                isFeatured = isFeatured
            )
        }
        relation.format == RelationFormat.CHECKBOX -> {
            DocumentRelationView.Checkbox(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                isChecked = values[relation.key] as? Boolean ?: false,
                isFeatured = isFeatured
            )
        }
        relation.format == RelationFormat.NUMBER -> {
            val value = values[relation.key]
            DocumentRelationView.Default(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                value = NumberParser.parse(value),
                isFeatured = isFeatured,
                format = relation.format
            )
        }
        else -> {
            val value = values[relation.key]
            DocumentRelationView.Default(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                value = value as? String,
                isFeatured = isFeatured,
                format = relation.format
            )
        }
    }
}

@Deprecated("To be deleted")
fun Relation.searchObjectsFilter(): List<DVFilter> {
    val filter = arrayListOf<DVFilter>()
    if (objectTypes.isNotEmpty()) {
        filter.add(
            DVFilter(
                relationKey = ObjectSetConfig.TYPE_KEY,
                operator = DVFilterOperator.AND,
                condition = DVFilterCondition.IN,
                value = objectTypes
            )
        )
    }
    return filter.toList()
}

fun ObjectWrapper.Relation.searchObjectsFilter(): List<DVFilter> {
    val filter = arrayListOf<DVFilter>()
    if (relationFormatObjectTypes.isNotEmpty()) {
        filter.add(
            DVFilter(
                relationKey = ObjectSetConfig.TYPE_KEY,
                operator = DVFilterOperator.AND,
                condition = DVFilterCondition.IN,
                value = relationFormatObjectTypes
            )
        )
    }
    return filter.toList()
}

fun List<DVFilter>.addIsHiddenFilter(): List<DVFilter> =
    this.toMutableList().apply {
        add(
            DVFilter(
                relationKey = Relations.IS_HIDDEN,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
    }

object FilterInputValueParser {
    fun parse(
        value: String?,
        format: ColumnView.Format,
        condition: Viewer.Filter.Condition
    ): Any? = when (format) {
        ColumnView.Format.NUMBER -> {
            if (value.isNullOrBlank()) {
                NUMBER_DEFAULT_VALUE
            } else {
                value.toDoubleOrNull() ?: NUMBER_DEFAULT_VALUE
            }
        }
        else -> {
            if (condition.hasValue()) value else null
        }
    }
}

/**
 * Converts relation {format DATE} value {Any?} to time in millis {Long} or null
 * @tests [RelationValueExtensionTest]
 */
object DateParser {
    fun parse(value: Any?): Long? {
        val result: Long? = when (value) {
            is String -> value.toLongOrNull()
            is Double -> value.toLong()
            is Long -> value
            else -> null
        }
        return result
    }

    fun parseInMillis(value: Any?) : Long? {
        val result: Long? = when (value) {
            is String -> value.toLongOrNull()
            is Double -> value.toLong()
            is Long -> value
            else -> null
        }
        return if (result!= null)
            result * 1000
        else
            null
    }
}

/**
 *  Get date format for ColumnView type DATE
 *  @tests [SetsExtensionTests]
 */
fun ColumnView.getDateRelationFormat(): String {
    val format = dateFormat?.format ?: return DateConst.DEFAULT_DATE_FORMAT
    return if (isDateIncludeTime == true) {
        if (timeFormat == Block.Content.DataView.TimeFormat.H12) {
            format + DateConst.DATE_FORMAT_SPACE + DateConst.TIME_H12
        } else {
            format + DateConst.DATE_FORMAT_SPACE + DateConst.TIME_H24
        }
    } else {
        format
    }
}