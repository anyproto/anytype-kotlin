package com.anytypeio.anytype.core_models

data class SearchResult(
    val results: List<ObjectWrapper.Basic>,
    val dependencies: List<ObjectWrapper.Basic>
)