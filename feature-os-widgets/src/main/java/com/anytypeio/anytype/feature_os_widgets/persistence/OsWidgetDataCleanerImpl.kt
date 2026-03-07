package com.anytypeio.anytype.feature_os_widgets.persistence

import android.content.Context
import com.anytypeio.anytype.domain.widgets.OsWidgetDataCleaner

class OsWidgetDataCleanerImpl(
    private val context: Context
) : OsWidgetDataCleaner {
    override suspend fun clearAll() {
        OsWidgetsDataStore(context).clear()
        OsWidgetIconCache(context).clearAll()
    }
}
