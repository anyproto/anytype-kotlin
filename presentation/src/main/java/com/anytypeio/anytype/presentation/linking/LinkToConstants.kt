package com.anytypeio.anytype.presentation.linking

import com.anytypeio.anytype.core_models.*

object LinkToConstants {

    val filters = listOf(
        DVFilter(
            condition = DVFilterCondition.EQUAL,
            value = false,
            relationKey = Relations.IS_ARCHIVED,
            operator = DVFilterOperator.AND
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )

    val sorts = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
            type = DVSortType.DESC
        )
    )
}
