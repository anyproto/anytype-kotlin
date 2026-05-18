package com.anytypeio.anytype.domain.misc

/**
 * The space id a cold-start route points into, if any. Used to prime
 * AccountSelect.preferredSpaceId. Routes without an inherent space
 * (invites, one-to-one chat by identity) return null.
 */
fun DeepLinkResolver.Action.preferredSpaceId(): String? = when (this) {
    is DeepLinkResolver.Action.DeepLinkToObject -> space.id
    is DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToSpace -> space.id
    is DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToObject -> space.id
    else -> null
}
