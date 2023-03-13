package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem

sealed class ObjectSearchSection : DefaultSearchItem {
    object RecentlyOpened: ObjectSearchSection()
    sealed class SelectWidgetSource : ObjectSearchSection() {
        object FromMyObjects: SelectWidgetSource()
        object FromLibrary : SelectWidgetSource()
    }
}