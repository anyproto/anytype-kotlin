package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.Relations.NUMBER_DEFAULT_VALUE
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
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
): List<ObjectRelationView> = mapNotNull { relation ->
    relation.view(
        details = details,
        values = values,
        urlBuilder = urlBuilder,
        isFeatured = featured.contains(relation.key)
    )
}

fun Key.isSystemKey() : Boolean = Relations.systemRelationKeys.contains(this)

fun ObjectWrapper.Relation.view(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    isFeatured: Boolean = false
): ObjectRelationView? {
    val relation = this
    return when {
        relation.isHidden == true -> null
        relation.format == RelationFormat.OBJECT -> {
            val objects = values.buildRelationValueObjectViews(
                relationKey = relation.key,
                details = details.details,
                builder = urlBuilder
            )
            ObjectRelationView.Object(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                objects = objects,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
            )
        }
        relation.format == RelationFormat.FILE -> {
            val files = values.buildFileViews(
                relationKey = relation.key,
                details = details.details
            )
            ObjectRelationView.File(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                files = files,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
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
            ObjectRelationView.Default(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                value = formattedDate,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                format = relation.format,
                system = relation.key.isSystemKey()
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
            ObjectRelationView.Status(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                status = status,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
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
            ObjectRelationView.Tags(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                tags = tags,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
            )
        }
        relation.format == RelationFormat.CHECKBOX -> {
            ObjectRelationView.Checkbox(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                isChecked = values[relation.key] as? Boolean ?: false,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
            )
        }
        relation.format == RelationFormat.NUMBER -> {
            val value = values[relation.key]
            ObjectRelationView.Default(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                value = NumberParser.parse(value),
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                format = relation.format,
                system = relation.key.isSystemKey()
            )
        }
        else -> {
            val value = values[relation.key]
            ObjectRelationView.Default(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                value = value as? String,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                format = relation.format,
                system = relation.key.isSystemKey()
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
                relation = ObjectSetConfig.TYPE_KEY,
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
                relation = ObjectSetConfig.TYPE_KEY,
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
                relation = Relations.IS_HIDDEN,
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

/**
 * Retrieves a list of distinct [ObjectWrapper.Relation] relations of Object using the given relation links filtered by system relations.
 *
 * @param relationLinks The list of relation links (@see [RelationLink] class) used to identify the relations.
 * @param systemRelations The list of keys of the system relations. Final list will be filtered by this list.
 * @param storeOfRelations The store of relations to retrieve the relations from.
 *
 * @return A list of distinct [ObjectWrapper.Relation] object relations.
 */
suspend fun getObjectRelations(
    relationLinks: List<RelationLink>,
    systemRelations: List<Key>,
    storeOfRelations: StoreOfRelations
): List<ObjectWrapper.Relation> {
    val systemRelationKeys = systemRelations.toSet()
    val objectRelationKeys = relationLinks.map { it.key }.filterNot { it in systemRelationKeys }
    return storeOfRelations.getByKeys(objectRelationKeys).distinctBy { it.key }
}

/**
 * Retrieves a list of filtered [ObjectWrapper.Relation] recommended relations of Object.
 *
 * @param relationLinks The list of relation links (@see [RelationLink] class) used to identify the relations.
 * @param recommendedRelations The list of ids of the recommended relations.
 * @param storeOfRelations The store of relations to retrieve the relations from.
 *
 * @return A list of distinct [ObjectWrapper.Relation] object recommended relations.
 */
suspend fun getNotIncludedRecommendedRelations(
    relationLinks: List<RelationLink>,
    recommendedRelations: List<Id>,
    storeOfRelations: StoreOfRelations
): List<ObjectWrapper.Relation> {
    val relationLinkKeys = relationLinks.map { it.key }.toSet()
    return storeOfRelations.getById(recommendedRelations)
        .filterNot { recommended -> recommended.key in relationLinkKeys }
}