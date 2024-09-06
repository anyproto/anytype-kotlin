package com.anytypeio.anytype.core_models

data class GlobalSearchCache(
    val query: String,
    val relatedObject: Id? = null
)
