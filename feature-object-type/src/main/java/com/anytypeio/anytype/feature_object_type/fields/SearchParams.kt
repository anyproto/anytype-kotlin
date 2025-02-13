package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations

fun filtersForFieldsListSearch(
    resolvedLayout: ObjectType.Layout,
    excludedRelationKeys: List<Id>,
    excludedRelationIds: List<Id>
): List<DVFilter> {
    val filters = buildList {
        addAll(buildDeletedFilter())
        add(buildTemplateFilter())
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = listOf(
                    ObjectType.Layout.RELATION.code.toDouble()
                )
            )
        )
        add(
            DVFilter(
                relation = Relations.RELATION_KEY,
                condition = DVFilterCondition.NOT_IN,
                value = excludedRelationKeys
            )
        )
        add(
            DVFilter(
                relation = Relations.ID,
                condition = DVFilterCondition.NOT_IN,
                value = excludedRelationIds
            )
        )
    }
    return filters
}

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

fun sortsForFieldsListSearch() = DVSort(
    relationKey = Relations.NAME,
    type = DVSortType.ASC,
    relationFormat = RelationFormat.LONG_TEXT
)

private fun buildTemplateFilter(): DVFilter = DVFilter(
    relation = Relations.TYPE_UNIQUE_KEY,
    condition = DVFilterCondition.NOT_EQUAL,
    value = ObjectTypeUniqueKeys.TEMPLATE
)