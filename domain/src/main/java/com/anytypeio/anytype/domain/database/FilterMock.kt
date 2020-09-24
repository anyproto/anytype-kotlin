package com.anytypeio.anytype.domain.database

import com.anytypeio.anytype.domain.database.model.*

const val ALL_ID = "-2"
const val PLUS_ID = "-1"

object FilterMock {

    val FILTER_ALL = Filter(
        detailId = ALL_ID,
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
            detailId = "333",
            condition = FilterTypeCondition.NONE,
            equality = FilterTypeEquality.EQUAL,
            value = "Team"
        ),
        Filter(
            detailId = "777",
            condition = FilterTypeCondition.NONE,
            equality = FilterTypeEquality.EQUAL,
            value = "Family"
        ),
//        Filter(
//            propertyId = "333",
//            condition = FilterTypeCondition.NONE,
//            equality = FilterTypeEquality.EQUAL,
//            value = "Product"
//        ),
//        Filter(
//            propertyId = "555",
//            condition = FilterTypeCondition.NONE,
//            equality = FilterTypeEquality.EQUAL,
//            value = "Android"
//        ),
        Filter(
            detailId = "888",
            condition = FilterTypeCondition.NONE,
            equality = FilterTypeEquality.EQUAL,
            value = "New"
        )
    )

    val groups = mutableListOf(
        Group(
            details = listOf(
                Detail.File(id = "9", name = "File", show = true),
                Detail.Bool(id = "10", name = "Bool", show = true),
                Detail.Link(id = "11", name = "Link", show = true),
                Detail.Phone(id = "12", name = "Phone", show = true)
            )
        )
    )
}