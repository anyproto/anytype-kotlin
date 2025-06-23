package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PushKeySpaceViewChannel @Inject constructor(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
) : PushKeyChannel {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<PushKeyUpdate> =
        spaceViewSubscriptionContainer.observe().flatMapLatest { spaceViews ->
            flow {
                spaceViews.forEach { spaceView ->
                    val key = spaceView.spacePushNotificationEncryptionKey
                    if (!key.isNullOrEmpty()) {
                        val id = key.computePushKeyId()
                        emit(PushKeyUpdate(encryptionKeyId = id, encryptionKey = key))
                    }
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