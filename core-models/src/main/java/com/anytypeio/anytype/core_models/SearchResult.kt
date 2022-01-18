package com.anytypeio.anytype.core_models

data class SearchResult(
    val results: List<ObjectWrapper>,
    val dependencies: List<ObjectWrapper>
)