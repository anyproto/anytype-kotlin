package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

/**
 * Whether this space type should navigate directly to chat instead of home screen.
 * CHAT and ONE_TO_ONE spaces open chat directly.
 */
val SpaceUxType?.shouldNavigateDirectlyToChat: Boolean
    get() = this == SpaceUxType.CHAT || this == SpaceUxType.ONE_TO_ONE

/**
 * Whether to show message author name in chat preview.
 * In ONE_TO_ONE spaces, the author is always the other person, so it's redundant.
 */
val SpaceUxType?.shouldShowMessageAuthorInPreview: Boolean
    get() = this != SpaceUxType.ONE_TO_ONE
