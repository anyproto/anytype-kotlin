package com.anytypeio.anytype.feature_date.viewmodel

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Relations.LAST_MODIFIED_DATE
import com.anytypeio.anytype.core_models.Relations.RELATION_KEY
import com.anytypeio.anytype.core_models.Relations.TYPE
import com.anytypeio.anytype.core_models.TimeInSeconds

fun filtersAndSortsForSearch(
    dateId: Id,
    field: ActiveField,
    timestamp: TimeInSeconds,
    spaces: List<Id>
): Pair<List<DVFilter>, List<DVSort>> {
    val filters = buildList {
        addAll(buildDeletedFilter())
        add(buildSpaceIdFilter(spaces))
        add(buildTemplateFilter())
        addAll(
            buildFieldFilters(
                dateObjectId = dateId,
                field = field,
                timestamp = timestamp
            )
        )
        add(buildLayoutFilter())
    }
    return filters to buildSorts(field)
}

private fun buildSorts(
    field: ActiveField,
): List<DVSort> {
    return listOf(
        DVSort(
            relationKey = field.key.key,
            type = field.sort,
            relationFormat = RelationFormat.DATE
        )
    )
}

private fun buildFieldFilters(
    dateObjectId: Id,
    field: ActiveField,
    timestamp: TimeInSeconds
): List<DVFilter> {
    val fieldKey = field.key.key
    return when (field.format) {
        Relation.Format.DATE -> {
            buildList {
                add(
                    DVFilter(
                        relation = fieldKey,
                        condition = DVFilterCondition.EQUAL,
                        value = timestamp.toDouble(),
                        relationFormat = RelationFormat.DATE
                    )
                )
                if (fieldKey == LAST_MODIFIED_DATE) {
                    add(
                        DVFilter(
                            relation = Relations.CREATED_DATE,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = mapOf(
                                RELATION_KEY to fieldKey,
                                TYPE to VALUE_FROM_RELATION
                            )
                        )
                    )
                }
            }
        }
        else -> {
            listOf(
                DVFilter(
                    relation = fieldKey,
                    condition = DVFilterCondition.IN,
                    value = dateObjectId
                )
            )
        }
    }
}

const val VALUE_FROM_RELATION = "valueFromRelation"

private fun buildTemplateFilter(): DVFilter = DVFilter(
    relation = Relations.TYPE_UNIQUE_KEY,
    condition = DVFilterCondition.NOT_EQUAL,
    value = ObjectTypeUniqueKeys.TEMPLATE
)

private fun buildSpaceIdFilter(spaces: List<Id>): DVFilter = DVFilter(
    relation = Relations.SPACE_ID,
    condition = DVFilterCondition.IN,
    value = spaces
)

private fun buildLayoutFilter(): DVFilter = DVFilter(
    relation = Relations.LAYOUT,
    condition = DVFilterCondition.IN,
    value = SUPPORTED_DATE_OBJECT_LAYOUTS.map { it.code.toDouble() }
)

private fun buildDeletedFilter(): List<DVFilter> {
    return listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )
}

private val SUPPORTED_DATE_OBJECT_LAYOUTS = listOf(
    ObjectType.Layout.SET,
    ObjectType.Layout.COLLECTION,

    ObjectType.Layout.TODO,
    ObjectType.Layout.NOTE,
    ObjectType.Layout.BASIC,
    ObjectType.Layout.PROFILE,

    ObjectType.Layout.PARTICIPANT,
    ObjectType.Layout.BOOKMARK,
    ObjectType.Layout.DATE,

    ObjectType.Layout.FILE,
    ObjectType.Layout.IMAGE,
    ObjectType.Layout.VIDEO,
    ObjectType.Layout.AUDIO,
    ObjectType.Layout.PDF
)