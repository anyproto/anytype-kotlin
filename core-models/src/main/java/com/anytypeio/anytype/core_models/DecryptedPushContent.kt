package com.anytypeio.anytype.core_models

import kotlinx.serialization.Serializable

@Serializable
data class DecryptedPushContent(
    val spaceId: String,
    val type: Int,
    val senderId: String,
    val newMessage: Message
) {
    @Serializable
    data class Message(
        val chatId: String,
        val msgId: String,
        val text: String,
        val spaceName: String,
        val senderName: String
    )
}