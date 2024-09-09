package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.GetChatMessages
import javax.inject.Inject

class ChatInteractor @Inject constructor(
    private val addChatMessage: AddChatMessage,
    private val getChatMessages: GetChatMessages,

) {
}