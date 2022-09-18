package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.math.ceil

class ObjectSetPaginator {
    val total = MutableStateFlow(0)
    val offset = MutableStateFlow<Long>(0)
    val pagination = total.combine(offset) { t, o ->
        val idx = ceil(o.toDouble() / ObjectSetConfig.DEFAULT_LIMIT).toInt()
        val pages = ceil(t.toDouble() / ObjectSetConfig.DEFAULT_LIMIT).toInt()
        Pair(idx, pages)
    }
}