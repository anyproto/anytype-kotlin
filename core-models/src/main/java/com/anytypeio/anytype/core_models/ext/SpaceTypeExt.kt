package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Whether this space type should navigate directly to chat instead of home screen.
 * Only ONE_TO_ONE spaces open chat directly.
 */
val ObjectWrapper.SpaceView.shouldNavigateDirectlyToChat: Boolean
    get() = isOneToOneSpace

/**
 * Whether to show the member count for this space.
 * In ONE_TO_ONE spaces there can only be two people, so showing the count is redundant.
 */
val ObjectWrapper.SpaceView.shouldShowMemberCount: Boolean
    get() = !isOneToOneSpace

/**
 * Whether users can create additional chat objects in this space.
 * ONE_TO_ONE spaces already have their dedicated chat and cannot host extra ones.
 */
val ObjectWrapper.SpaceView.canCreateAdditionalChats: Boolean
    get() = !isOneToOneSpace
