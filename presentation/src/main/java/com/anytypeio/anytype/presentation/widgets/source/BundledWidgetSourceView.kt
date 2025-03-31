package com.anytypeio.anytype.presentation.widgets.source

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.widgets.BundledWidgetSourceIds

/**
 * Used for picking bundled widget source from list of objects.
 */
sealed class BundledWidgetSourceView : DefaultSearchItem {
    abstract val id: Id

    data object Favorites : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.FAVORITE
    }

    @Deprecated("To be deleted. Migrating to widget source by suggested types")
    data object Sets : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.SETS
    }

    @Deprecated("To be deleted. Migrating to widget source by suggested types")
    data object Collections : BundledWidgetSourceView() {
        override val id: Id get() = BundledWidgetSourceIds.COLLECTIONS
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
}