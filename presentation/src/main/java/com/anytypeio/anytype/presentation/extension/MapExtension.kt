package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType

fun Map<Id, Block.Fields>.updateFields(update: Map<Id, Block.Fields>): Map<Id, Block.Fields> {
    val result = this.toMutableMap()
    for ((key, value) in update) {
        result[key] = value
    }
    return result
}

fun Map<Id, Block.Fields>.getProperObjectName(id: Id): String? {
    val layoutCode = this[id]?.layout?.toInt()
    return if (layoutCode == ObjectType.Layout.NOTE.code) {
        this[id]?.snippet?.replace("\n", " ")?.take(30)
    } else {
        this[id]?.name
    }
}