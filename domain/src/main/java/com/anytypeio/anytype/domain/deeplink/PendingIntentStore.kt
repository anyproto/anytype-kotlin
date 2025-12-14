package com.anytypeio.anytype.domain.deeplink

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Store for keeping pending deeplinks in memory.
 * This is used to handle deeplinks that were received while user was not logged in,
 * or when navigating between screens where data needs to be passed asynchronously.
 */
@Singleton
class PendingIntentStore @Inject constructor() {
    private var deepLinkInvite: String? = null
    private var oneToOneChatData: OneToOneChatData? = null

    data class OneToOneChatData(val identity: String, val metadataKey: String)

    fun setDeepLinkInvite(link: String?) {
        deepLinkInvite = link
    }

    fun getDeepLinkInvite(): String? = deepLinkInvite

    fun clearDeepLinkInvite() {
        deepLinkInvite = null
    }

    fun setOneToOneChatData(identity: String, metadataKey: String) {
        oneToOneChatData = OneToOneChatData(identity, metadataKey)
    }

    fun getOneToOneChatData(): OneToOneChatData? = oneToOneChatData

    fun clearOneToOneChatData() {
        oneToOneChatData = null
    }
} 