package com.anytypeio.anytype.domain.widgets

interface OsWidgetDataCleaner {
    suspend fun clearAll()

    object NoOp : OsWidgetDataCleaner {
        override suspend fun clearAll() = Unit
    }
}
