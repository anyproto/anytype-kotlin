package com.anytypeio.anytype.core_models.chats

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId

sealed class Chat {

    /**
     * @property [id] message id
     * @property [read] whether this message is read
     * @property [mentionRead] whether mention contained in this message read
     */
    data class Message(
        val id: Id,
        val order: Id,
        val creator: Id,
        val createdAt: Long,
        val modifiedAt: Long,
        val content: Content?,
        val attachments: List<Attachment> = emptyList(),
        val reactions: Map<String, List<String>> = emptyMap(),
        val replyToMessageId: Id? = null,
        val read: Boolean = false,
        val mentionRead: Boolean = false,
        val synced: Boolean = false
    ) {
        data class Content(
            val text: String,
            val style: Block.Content.Text.Style,
            val marks: List<Block.Content.Text.Mark>
        )
        data class Attachment(
            val target: Id,
            val type: Type
        ) {
            sealed class Type {
                data object File: Type()
                data object Image: Type()
                data object Link: Type()
            }
        }

        companion object {
            /**
             * New message builder.
             */
            fun new(
                text: String,
                attachments: List<Attachment> = emptyList(),
                replyToMessageId: Id? = null,
                marks: List<Block.Content.Text.Mark>
            ) : Message = Message(
                id = "",
                createdAt = 0L,
                modifiedAt = 0L,
                attachments = attachments,
                reactions = emptyMap(),
                creator = "",
                replyToMessageId = replyToMessageId,
                content = Content(
                    text = text,
                    marks = marks,
                    style = Block.Content.Text.Style.P
                ),
                order = "",
                synced = false
            )

            /**
             * Updated message builder.
             */
            fun updated(
                id: Id,
                text: String,
                attachments: List<Attachment> = emptyList(),
                marks: List<Block.Content.Text.Mark>,
                synced: Boolean = false
            ) : Message = Message(
                id = id,
                createdAt = 0L,
                modifiedAt = 0L,
                attachments = attachments,
                reactions = emptyMap(),
                creator = "",
                replyToMessageId = "",
                content = Content(
                    text = text,
                    marks = marks,
                    style = Block.Content.Text.Style.P
                ),
                order = "",
                synced = synced
            )
        }
    }

    data class State(
        val unreadMessages: UnreadState? = null,
        val unreadMentions: UnreadState? = null,
        val lastStateId: Id? = null,
        val order: Long = -1L,
    ) {
        /**
         * @property olderOrderId oldest(in the lex sorting) unread message order id. Client should ALWAYS scroll through unread messages from the oldest to the newest
         */
        data class UnreadState(
            val olderOrderId: Id,
            val counter: Int
        )

        val hasUnReadMessages: Boolean get() {
            return unreadMessages?.counter != null && unreadMessages.counter > 0
        }

        val hasUnReadMentions: Boolean get() {
            return unreadMentions?.counter != null && unreadMentions.counter > 0
        }

        val oldestMessageOrderId: Id? = unreadMessages?.olderOrderId
        val oldestMentionMessageOrderId: Id? = unreadMentions?.olderOrderId
    }

    data class Preview(
        val space: SpaceId,
        val chat: Id,
        val message: Message? = null,
        val state: State? = null,
        val dependencies: List<ObjectWrapper.Basic> = emptyList()
    )
}