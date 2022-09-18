package com.anytypeio.anytype.core_models

data class SearchResult(
    val results: List<ObjectWrapper.Basic>,
    val dependencies: List<ObjectWrapper.Basic>,
    val counter: Counter? = null
) {
    /**
     * @property [total] total available
     * @property [prev] how many objects available before
     * @property [next] how many objects available after
     */
    data class Counter(
        val total: Int,
        val prev: Int,
        val next: Int
    )
}