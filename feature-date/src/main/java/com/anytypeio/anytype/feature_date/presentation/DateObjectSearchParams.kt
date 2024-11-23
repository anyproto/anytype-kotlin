package com.anytypeio.anytype.feature_date.presentation

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.primitives.RelationKey
import kotlin.text.toDouble

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