package com.anytypeio.anytype.domain.deeplink

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Store for keeping pending invite deeplinks in memory.
 * This is used to handle invite deeplinks that were received while user was not logged in.
 */
@Singleton
class PendingIntentStore @Inject constructor() {
    private var deepLinkInvite: String? = null

    fun setDeepLinkInvite(link: String?) {
        deepLinkInvite = link
    }

    fun getDeepLinkInvite(): String? = deepLinkInvite

    fun clearDeepLinkInvite() {
        deepLinkInvite = null
    }
} 