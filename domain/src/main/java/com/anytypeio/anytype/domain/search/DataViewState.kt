package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Id

/**
 * Data view state for keeping track of results and its dependencies.
 * @property [objects] data for this subscription
 * @property [dependencies] its dependencies
 * @property [lastModified] timestamp for data modification
 */
sealed class DataViewState {
    data class Loaded(
        val objects: List<Id> = emptyList(),
        val dependencies: List<Id> = emptyList(),
        val lastModified: Long = 0L
    ) : DataViewState()

    object Init : DataViewState()
}