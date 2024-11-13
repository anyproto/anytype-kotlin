package com.anytypeio.anytype.feature_date.presentation

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey

fun filtersForSearch(
    relationKey: RelationKey,
    objectId: Id,
    spaces: List<Id>
): List<DVFilter> {
    val filters = buildList {
        addAll(buildDeletedFilter())
        add(buildSpaceIdFilter(spaces))
        add(buildTemplateFilter())
        add(buildRelationKeyFilter(
            relationKey = relationKey,
            objectId = objectId
        ))
    }
    return filters
}

private fun buildRelationKeyFilter(relationKey: RelationKey, objectId: Id): DVFilter = DVFilter(
    relation = relationKey.key,
    condition = DVFilterCondition.IN,
    value = objectId
)

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