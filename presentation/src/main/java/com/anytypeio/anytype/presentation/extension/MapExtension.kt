package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MAX_SNIPPET_SIZE
import com.anytypeio.anytype.core_models.ObjectType

fun Map<Id, Block.Fields>.getProperObjectName(id: Id?): String? {
    if (id == null) return null
    val layoutCode = this[id]?.layout?.toInt()
    return if (layoutCode == ObjectType.Layout.NOTE.code) {
        this[id]?.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        this[id]?.name
    }
}
