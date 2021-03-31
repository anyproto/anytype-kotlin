package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id

fun Map<Id, Block.Fields>.updateFields(update: Map<Id, Block.Fields>): Map<Id, Block.Fields> {
    val result = this.toMutableMap()
    for ((key, value) in update) {
        result[key] = value
    }
    return result
}