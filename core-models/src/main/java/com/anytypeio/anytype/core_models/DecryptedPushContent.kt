package com.anytypeio.anytype.core_models

data class DecryptedPushContent(
    val spaceId: Id,
    val type: Int,
    val senderId: Id,
    val newMessage: Message
) {
    data class Message(
        val chatId: Id,
        val msgId: Id,
        val text: String,
        val spaceName: String,
        val senderName: String
    )
}

data class DecryptedPushKeys(
    val decryptedMessage: String,
    val spaceId: Id,
    val chatId: Id
) 