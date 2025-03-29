package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem

sealed class ObjectSearchSection : DefaultSearchItem {
    data object RecentlyOpened: ObjectSearchSection()
    sealed class SelectWidgetSource : ObjectSearchSection() {
        data object FromMyObjects: SelectWidgetSource()
        data object FromLibrary: SelectWidgetSource()
        data object System: SelectWidgetSource()
    }
}