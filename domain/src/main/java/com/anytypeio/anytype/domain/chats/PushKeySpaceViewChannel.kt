package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PushKeySpaceViewChannel @Inject constructor(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
) : PushKeyChannel {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<PushKeyUpdate> =
        spaceViewSubscriptionContainer.observe()
            // The space-view subscription amends many times on cold start, re-emitting the
            // full list each time. Collapse to the distinct SET of push keys (order-insensitive,
            // so reordering spaces is a no-op) and dedupe, so the expensive Base64 + SHA-256 id
            // computation below only runs when the set of keys actually changes.
            .map { spaceViews ->
                spaceViews
                    .mapNotNull { it.spacePushNotificationEncryptionKey?.takeIf(String::isNotEmpty) }
                    .toSet()
            }
            .distinctUntilChanged()
            .flatMapLatest { keys ->
                flow {
                    keys.forEach { key ->
                        emit(
                            PushKeyUpdate(
                                encryptionKeyId = key.computePushKeyId(),
                                encryptionKey = key
                            )
                        )
                    }
                }
            }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun String.computePushKeyId(): String {
    val decodedKey = Base64.getDecoder().decode(this)
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(decodedKey)
    return hash.toHexString()
}