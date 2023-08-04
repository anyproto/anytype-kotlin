package com.anytypeio.anytype.presentation.widgets.source

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.widgets.BundledWidgetSourceIds

/**
 * Used for picking bundled widget source from list of objects.
 */
sealed class BundledWidgetSourceView : DefaultSearchItem {
    abstract val id: Id

    object Favorites : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.FAVORITE
    }

    object Sets : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.SETS
    }

    object Collections : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.COLLECTIONS
    }

    object Recent : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.RECENT
    }

    object RecentLocal : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.RECENT_LOCAL
    }
}