package com.anytypeio.anytype.feature_date.presentation

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.TimeInSeconds

fun filtersForSearch(
    relation: DateObjectViewModel.ActiveRelation,
    dateObjectId: Id,
    timestamp: TimeInSeconds,
    spaces: List<Id>
): List<DVFilter> {
    val filters = buildList {
        addAll(buildDeletedFilter())
        add(buildSpaceIdFilter(spaces))
        add(buildTemplateFilter())
        add(
            buildRelationKeyFilter(
                dateObjectId = dateObjectId,
                relation = relation,
                timestamp = timestamp
            )
        )
        add(buildLayoutFilter())
    }
    return filters
}

private fun buildRelationKeyFilter(
    dateObjectId: Id,
    relation: DateObjectViewModel.ActiveRelation,
    timestamp: TimeInSeconds
): DVFilter =
    when (relation.format) {
        Relation.Format.DATE -> {
            DVFilter(
                relation = relation.key.key,
                condition = DVFilterCondition.EQUAL,
                value = timestamp.toDouble(),
                relationFormat = RelationFormat.DATE
            )
        }
        else -> {
            DVFilter(
                relation = relation.key.key,
                condition = DVFilterCondition.IN,
                value = dateObjectId
            )
        }
    }

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
    value = SUPPORTED_LAYOUTS.map { it.code.toDouble() }
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

private val SUPPORTED_LAYOUTS = listOf(
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