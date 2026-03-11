package com.anytypeio.anytype.domain.widgets

/**
 * Interface for syncing data view widget items.
 * Reads all configured data view widgets from DataStore,
 * re-fetches items for each, and persists the updated data.
 */
interface OsWidgetDataViewSync {
    suspend fun sync()
}
