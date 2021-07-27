package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.isWhole
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.Relations
import com.anytypeio.anytype.domain.relations.Relations.NUMBER_DEFAULT_VALUE
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.sets.*
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

fun List<Relation>.views(
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

fun Relation.view(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    isFeatured: Boolean = false
) : DocumentRelationView? {
    val relation = this
    return when {
        relation.isHidden -> null
        relation.format == Relation.Format.OBJECT -> {
            val objects = values.buildObjectViews(
                columnKey = relation.key,
                details = details.details,
                builder = urlBuilder
            )
            DocumentRelationView.Object(
                relationId = relation.key,
                name = relation.name,
                objects = objects,
                isFeatured = isFeatured
            )
        }
        relation.format == Relation.Format.FILE -> {
            val files = values.buildFileViews(
                columnKey = relation.key,
                details = details.details
            )
            DocumentRelationView.File(
                relationId = relation.key,
                name = relation.name,
                files = files,
                isFeatured = isFeatured
            )
        }
        relation.format == Relation.Format.DATE -> {
            //TODO In DataView Relation Date uses DateFormat and TimeFormat
            // so SimpleDateFormat can be different from what we have here
            // see {SetsExtension:buildGridRow()}
            val format = SimpleDateFormat(DateConst.DEFAULT_DATE_FORMAT, Locale.getDefault())
            val value = values[relation.key]
            val timeInMillis = DateParser.parse(value)
            val formattedDate = if (timeInMillis != null) {
                format.format(Date(timeInMillis))
            } else {
                null
            }
            DocumentRelationView.Default(
                relationId = relation.key,
                name = relation.name,
                value = formattedDate,
                isFeatured = isFeatured
            )
        }
        relation.format == Relation.Format.STATUS -> {
            val status = values.buildStatusViews(
                selOptions = relation.selections,
                columnKey = relation.key
            )
            DocumentRelationView.Status(
                relationId = relation.key,
                name = relation.name,
                status = status,
                isFeatured = isFeatured
            )
        }
        relation.format == Relation.Format.TAG -> {
            val tags = values.buildTagViews(
                selOptions = relation.selections,
                columnKey = relation.key
            )
            DocumentRelationView.Tags(
                relationId = relation.key,
                name = relation.name,
                tags = tags,
                isFeatured = isFeatured
            )
        }
        relation.format == Relation.Format.CHECKBOX -> {
            DocumentRelationView.Checkbox(
                relationId = relation.key,
                name = relation.name,
                isChecked = values[relation.key] as? Boolean ?: false,
                isFeatured = isFeatured
            )
        }
        relation.format == Relation.Format.NUMBER -> {
            val value = values[relation.key]
            DocumentRelationView.Default(
                relationId = relation.key,
                name = relation.name,
                value = NumberParser.parse(value),
                isFeatured = isFeatured
            )
        }
        else -> {
            DocumentRelationView.Default(
                relationId = relation.key,
                name = relation.name,
                value = values[relation.key] as? String,
                isFeatured = isFeatured
            )
        }
    }
}

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
        val timeInMillis: Long? = when (value) {
            is String -> value.toLongOrNull()
            is Double -> value.toLong()
            is Long -> value
            else -> null
        }
        return timeInMillis?.times(1000)
    }
}

/**
 * Converts relation {format NUMBER} value {Any?} to string representation or null
 */
object NumberParser {
    fun parse(value: Any?): String? = when (value) {
        is String -> {
            val num = value.toDoubleOrNull()
            num?.convertToString()
        }
        is Number -> {
            val num = value.toDouble()
            num.convertToString()
        }
        else -> null
    }
}

fun Double.convertToString(): String = if (isWhole())
    this.toLong().toString()
else
    this.toString()

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

fun List<Relation>.toNotHiddenRelationViews(): List<RelationView.Existing> {
    return filter { !it.isHidden }
        .map {
            RelationView.Existing(
                id = it.key,
                name = it.name,
                format = it.format
            )
        }
}