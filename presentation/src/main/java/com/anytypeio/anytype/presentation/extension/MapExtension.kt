package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper

const val MAX_SNIPPET_SIZE = 30

fun Map<Id, Block.Fields>.updateFields(update: Map<Id, Block.Fields>): Map<Id, Block.Fields> {
    val result = this.toMutableMap()
    for ((key, value) in update) {
        result[key] = value
    }
    return result
}

fun Map<Id, Block.Fields>.getProperObjectName(id: Id?): String? {
    if (id == null) return null
    val layoutCode = this[id]?.layout?.toInt()
    return if (layoutCode == ObjectType.Layout.NOTE.code) {
        this[id]?.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        this[id]?.name
    }
}

fun ObjectWrapper.Basic.getProperObjectName(): String? {
    return if (layout == ObjectType.Layout.NOTE) {
        snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        name
    }
}
