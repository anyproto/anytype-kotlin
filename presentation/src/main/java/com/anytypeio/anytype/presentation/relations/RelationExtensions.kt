package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.Relations.NUMBER_DEFAULT_VALUE
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.extension.getOptionObject
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.objects.buildRelationValueObjectViews
import com.anytypeio.anytype.presentation.sets.*
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer

suspend fun List<ObjectWrapper.Relation>.views(
    details: ObjectViewDetails,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    featured: List<Id> = emptyList(),
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<ObjectRelationView> = mapNotNull { relation ->
    relation.view(
        details = details,
        values = values,
        urlBuilder = urlBuilder,
        isFeatured = featured.contains(relation.key),
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes
    )
}

fun Key.isSystemKey() : Boolean = Relations.systemRelationKeys.contains(this)

suspend fun ObjectWrapper.Relation.view(
    details: ObjectViewDetails,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    isFeatured: Boolean = false,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): ObjectRelationView {
    val relation = this
    return when (relation.format) {
        RelationFormat.OBJECT -> {
            val objects = values.buildRelationValueObjectViews(
                relationKey = relation.key,
                details = details,
                builder = urlBuilder,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
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
                val statusValue = values.getSingleValue<String>(relation.key)
                if (statusValue != null) {
                    val status = details.getOptionObject(statusValue)
                    if (status != null) {
                        add(status)
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
                values[relation.key].values<String>().forEach { id ->
                    val status = details.getOptionObject(id)
                    if (status != null) add(status)
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
        format: RelationFormat,
        condition: Viewer.Filter.Condition
    ): Any? = when (format) {
        RelationFormat.NUMBER -> {
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
 * Retrieves a list of distinct [ObjectWrapper.Relation] relations of Object using the given relation keys filtered by system relations.
 *
 * @param relationKeys The list of object details keys
 * @param systemRelations The list of keys of the system relations. Final list will be filtered by this list.
 * @param storeOfRelations The store of relations to retrieve the relations from.
 *
 * @return A list of distinct [ObjectWrapper.Relation] object relations.
 */
suspend fun getObjectRelations(
    relationKeys: Set<Key>,
    systemRelations: List<Key>,
    storeOfRelations: StoreOfRelations
): List<ObjectWrapper.Relation> {
    val systemRelationKeys = systemRelations.toSet()
    val objectRelationKeys = relationKeys.filterNot { it in systemRelationKeys }
    return storeOfRelations.getByKeys(objectRelationKeys).distinctBy { it.key }
}

/**
 * Retrieves a list of filtered [ObjectWrapper.Relation] recommended relations of Object.
 *
 * @param relationKeys The list of object details keys
 * @param recommendedRelations The list of ids of the recommended relations.
 * @param storeOfRelations The store of relations to retrieve the relations from.
 *
 * @return A list of distinct [ObjectWrapper.Relation] object recommended relations.
 */
suspend fun getNotIncludedRecommendedRelations(
    relationKeys: Set<Key>,
    recommendedRelations: List<Id>,
    storeOfRelations: StoreOfRelations
): List<ObjectWrapper.Relation> {
    return storeOfRelations.getById(recommendedRelations)
        .filterNot { recommended -> recommended.key in relationKeys }
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