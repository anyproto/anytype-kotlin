package com.anytypeio.anytype.presentation.objects

enum class ObjectAction {
    DELETE,
    MOVE_TO_BIN,
    RESTORE,
    ADD_TO_FAVOURITE,
    REMOVE_FROM_FAVOURITE,
    /**
     * DROID-4397: per-user, per-space "My Favorites".
     * Distinct from the legacy global [ADD_TO_FAVOURITE] / [REMOVE_FROM_FAVOURITE]
     * which apply across spaces via SetObjectListIsFavorite. These go through
     * AddPersonalFavorite / RemovePersonalFavorite and the personal-widgets doc.
     */
    ADD_TO_MY_FAVORITES,
    REMOVE_FROM_MY_FAVORITES,
    MOVE_TO,
    SEARCH_ON_PAGE,
    USE_AS_TEMPLATE,
    UNDO_REDO,
    DUPLICATE,
    LOCK,
    UNLOCK,
    LINK_TO,
    COPY_LINK,
    DELETE_FILES,
    SET_AS_DEFAULT,
    PIN,
    UNPIN,
    DOWNLOAD_FILE
}