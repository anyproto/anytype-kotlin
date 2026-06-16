package com.anytypeio.anytype.presentation.navigation.backstack

/**
 * Read-only access to the object-screen entries currently present in the app's
 * navigation back stack. Implemented in the app module on top of the NavController.
 */
interface NavigationBackStackInspector {
    /**
     * @return object-screen entries in back-stack order, bottom -> top. The currently visible
     * screen is identified separately via [currentEntryId] rather than assumed to be the last
     * entry, since the back stack can hold trailing entries that are not yet on screen.
     */
    fun objectScreenEntries(): List<BackStackObjectEntry>

    /**
     * @return the back-stack id of the topmost space home (widgets) screen present in the
     * navigation history, or null if there is none.
     */
    fun homeScreenEntryId(): String?

    /**
     * @return the back-stack id of the currently visible destination, as reported by the
     * NavController, or null. Used to exclude the current screen from the history regardless
     * of its position in the back-stack list.
     */
    fun currentEntryId(): String?
}
