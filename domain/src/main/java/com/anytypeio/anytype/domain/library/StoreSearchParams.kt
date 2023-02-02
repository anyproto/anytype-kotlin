package com.anytypeio.anytype.domain.library

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id

class StoreSearchParams(
    val subscription: Id,
    val sorts: List<DVSort> = emptyList(),
    val filters: List<DVFilter> = emptyList(),
    val source: List<String> = emptyList(),
    val offset: Long = 0,
    val limit: Int = 0,
    val keys: List<String> = emptyList()
)