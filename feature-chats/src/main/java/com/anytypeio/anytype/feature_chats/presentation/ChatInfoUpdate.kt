package com.anytypeio.anytype.feature_chats.presentation

// Holder for chat info update from EditChatInfoScreen

data class ChatInfoUpdate(
    val name: String,
    val chatIcon: ChatObjectIcon
)

sealed class ChatObjectIcon {
    data object None : ChatObjectIcon()
    data object Removed : ChatObjectIcon()
    data class Image(val uri: String) : ChatObjectIcon()
    data class Emoji(val unicode: String) : ChatObjectIcon()
}
