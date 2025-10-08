package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

/**
 * Checks if this space has chat functionality enabled.
 *
 * A space has chat functionality if:
 * 1. It's a dedicated chat space (spaceUxType == CHAT), OR
 * 2. It has a non-empty chatId (DATA/STREAM spaces can have chat)
 *
 * This is used to determine whether to show chat-specific UI and behavior.
 */
fun ObjectWrapper.SpaceView.hasChatFunctionality(): Boolean {
    return spaceUxType == SpaceUxType.CHAT || !chatId.isNullOrEmpty()
}
