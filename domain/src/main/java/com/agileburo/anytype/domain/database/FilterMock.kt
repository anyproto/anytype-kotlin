package com.agileburo.anytype.domain.database

import com.agileburo.anytype.domain.database.model.Filter
import com.agileburo.anytype.domain.database.model.FilterTypeCondition
import com.agileburo.anytype.domain.database.model.FilterTypeEquality

const val ALL_ID = "-2"
const val PLUS_ID = "-1"

object FilterMock {

    val FILTER_ALL = Filter(
        propertyId = ALL_ID,
        condition = FilterTypeCondition.NONE,
        equality = FilterTypeEquality.EQUAL,
        value = "All"
    )

//    val FILTER_PLUS = Filter(
//        propertyId = PLUS_ID,
//        condition = FilterTypeCondition.NONE,
//        equality = FilterTypeEquality.EQUAL,
//        value = "Plus"
//    )

    var filters = mutableListOf(
        Filter(
            propertyId = "122345",
            condition = FilterTypeCondition.NONE,
            equality = FilterTypeEquality.EQUAL,
            value = "Team"
        ),
        Filter(
            propertyId = "987655",
            condition = FilterTypeCondition.NONE,
            equality = FilterTypeEquality.EQUAL,
            value = "Friends"
        )
    )
}