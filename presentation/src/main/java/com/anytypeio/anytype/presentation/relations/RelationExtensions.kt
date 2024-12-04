package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.Relations.NUMBER_DEFAULT_VALUE
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.sets.*
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import java.util.*

fun List<ObjectWrapper.Relation>.views(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    featured: List<Id> = emptyList(),
    fieldParser: FieldParser
): List<ObjectRelationView> = mapNotNull { relation ->
    relation.view(
        details = details.details,
        values = values,
        urlBuilder = urlBuilder,
        isFeatured = featured.contains(relation.key),
        fieldParser = fieldParser
    )
}

fun Key.isSystemKey() : Boolean = Relations.systemRelationKeys.contains(this)

fun ObjectWrapper.Relation.view(
    details: Map<Id, Block.Fields>,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    isFeatured: Boolean = false,
    fieldParser: FieldParser
): ObjectRelationView {
    val relation = this
    return when (relation.format) {
        RelationFormat.OBJECT -> {
            val objects = values.buildRelationValueObjectViews(
                relationKey = relation.key,
                details = details,
                builder = urlBuilder,
                fieldParser = fieldParser
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
        RelationFormat.FILE -> {
            val files = values.buildFileViews(
                relationKey = relation.key,
                details = details
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
        RelationFormat.DATE -> {
            val fieldDate = fieldParser.toDate(any = values[relation.key])
            ObjectRelationView.Date(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey(),
                relativeDate = fieldDate?.relativeDate
            )
        }
        RelationFormat.STATUS -> {
            val options = buildList {
                when(val value = values[relation.key]) {
                    is Id -> {
                        val status = details[value]
                        if (status != null && status.map.isNotEmpty()) {
                            add(
                                ObjectWrapper.Option(status.map)
                            )
                        }
                    }
                    is List<*> -> {
                        value.forEach { id ->
                            val status = details[id]
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
        RelationFormat.TAG -> {
            val options = buildList {
                when(val value = values[relation.key]) {
                    is Id -> {
                        val status = details[value]
                        if (status != null && status.map.isNotEmpty()) {
                            add(
                                ObjectWrapper.Option(status.map)
                            )
                        }
                    }
                    is List<*> -> {
                        value.forEach { id ->
                            val status = details[id]
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
        RelationFormat.CHECKBOX -> {
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
        RelationFormat.NUMBER -> {
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

fun ObjectRelationView.getRelationFormat(): RelationFormat = when (this) {
    is ObjectRelationView.Object -> RelationFormat.OBJECT
    is ObjectRelationView.File -> RelationFormat.FILE
    is ObjectRelationView.Default -> format
    is ObjectRelationView.Status -> RelationFormat.STATUS
    is ObjectRelationView.Tags -> RelationFormat.TAG
    is ObjectRelationView.Checkbox -> RelationFormat.CHECKBOX
    is ObjectRelationView.Links.Backlinks -> RelationFormat.OBJECT
    is ObjectRelationView.Links.From -> RelationFormat.OBJECT
    is ObjectRelationView.ObjectType.Base -> RelationFormat.OBJECT
    is ObjectRelationView.ObjectType.Deleted -> RelationFormat.OBJECT
    is ObjectRelationView.Source -> RelationFormat.OBJECT
    is ObjectRelationView.Date -> RelationFormat.DATE
}