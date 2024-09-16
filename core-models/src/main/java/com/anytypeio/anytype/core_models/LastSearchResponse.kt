package com.anytypeio.anytype.core_models

data class GlobalSearchHistory(
    val query: String,
    val relatedObject: Id? = null
)
