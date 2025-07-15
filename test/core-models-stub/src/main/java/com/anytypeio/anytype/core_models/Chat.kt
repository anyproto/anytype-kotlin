package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubChatMessage(
    id: Id = MockDataFactory.randomUuid(),
    order: Id = MockDataFactory.randomUuid(),
    creator: Id = MockDataFactory.randomUuid(),
    timestamp: Long = MockDataFactory.randomLong(),
    modifiedAt: Long = MockDataFactory.randomLong(),
    reactions: Map<String, List<Id>> = emptyMap(),
    content: Chat.Message.Content? = null

): Chat.Message = Chat.Message(
    id = id,
    order = order,
    creator = creator,
    createdAt = timestamp,
    reactions = reactions,
    content = content,
    modifiedAt = modifiedAt
)

fun StubChatMessageContent(
    text: String,
    style: TextStyle = TextStyle.P,
    marks: List<Block.Content.Text.Mark> = emptyList()
): Chat.Message.Content = Chat.Message.Content(
    text = text,
    style = style,
    marks = marks
)

fun stubChatPreview(
    spaceId: String,
    chatId: String,
    lastMessageDate: Long
): Chat.Preview {
    return Chat.Preview(
        space = SpaceId(spaceId),
        chat = chatId,
        message = Chat.Message(
            id = "msg1",
            creator = "user1",
            content = Chat.Message.Content(
                text = "Hello, user1",
                style = TextStyle.P,
                marks = listOf()
            ),
            createdAt = lastMessageDate,
            attachments = emptyList(),
            order = "order1",
            modifiedAt = System.currentTimeMillis(),
        ),
        dependencies = emptyList(),
        state = null
    )
}