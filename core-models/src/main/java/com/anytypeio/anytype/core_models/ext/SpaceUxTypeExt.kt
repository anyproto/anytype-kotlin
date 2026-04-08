package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

/**
 * Whether this space type should navigate directly to chat instead of home screen.
 * Only ONE_TO_ONE spaces open chat directly. Regular channels (including former CHAT spaces)
 * now use homepage-based navigation.
 */
val SpaceUxType?.shouldNavigateDirectlyToChat: Boolean
    get() = this == SpaceUxType.ONE_TO_ONE

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

// region ObjectWrapper.SpaceView extensions
//
// These mirror the SpaceUxType?-receiver helpers above but route through
// ObjectWrapper.SpaceView, so they automatically respect the new
// `spaceType` relation (with the legacy `spaceUxType` fallback baked into
// `isOneToOneSpace` / `isChatSpace`). Prefer these helpers in any call
// site that has the full SpaceView in scope.

/** @see SpaceUxType.shouldNavigateDirectlyToChat */
val ObjectWrapper.SpaceView.shouldNavigateDirectlyToChat: Boolean
    get() = isOneToOneSpace

/** @see SpaceUxType.shouldShowMessageAuthorInPreview */
val ObjectWrapper.SpaceView.shouldShowMessageAuthorInPreview: Boolean
    get() = !isOneToOneSpace

/** @see SpaceUxType.shouldShowMemberCount */
val ObjectWrapper.SpaceView.shouldShowMemberCount: Boolean
    get() = !isOneToOneSpace

/** @see SpaceUxType.canCreateAdditionalChats */
val ObjectWrapper.SpaceView.canCreateAdditionalChats: Boolean
    get() = !isChatSpace && !isOneToOneSpace

// endregion
