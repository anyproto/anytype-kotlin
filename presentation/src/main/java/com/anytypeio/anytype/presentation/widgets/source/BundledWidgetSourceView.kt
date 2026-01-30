package com.anytypeio.anytype.presentation.widgets.source

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.core_models.ui.ObjectIcon

/**
 * Used for picking bundled widget source from list of objects.
 */
sealed class BundledWidgetSourceView : DefaultSearchItem {
    abstract val id: Id

    data object Favorites : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.FAVORITE
    }
    data object Recent : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.RECENT
    }

    data object RecentLocal : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.RECENT_LOCAL
    }

    data object Bin : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.BIN
    }

    data object AllObjects : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.ALL_OBJECTS
    }

    data object Chat : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.CHAT
    }
}

data class SuggestWidgetObjectType(
    val id: Id,
    val name: String,
    val objectIcon: ObjectIcon
) : DefaultSearchItem