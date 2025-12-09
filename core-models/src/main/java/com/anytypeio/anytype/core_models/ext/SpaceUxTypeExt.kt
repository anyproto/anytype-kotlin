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

/**
 * Whether to show the member count for this space type.
 * In ONE_TO_ONE spaces, there can only be two people, so showing the count is redundant.
 */
val SpaceUxType?.shouldShowMemberCount: Boolean
    get() = this != SpaceUxType.ONE_TO_ONE

/**
 * Whether users can create additional chats in this space type.
 * In CHAT and ONE_TO_ONE spaces, users cannot create additional chat objects.
 */
val SpaceUxType?.canCreateAdditionalChats: Boolean
    get() = this != SpaceUxType.CHAT && this != SpaceUxType.ONE_TO_ONE
