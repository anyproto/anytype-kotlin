package com.anytypeio.anytype.feature_chats.tools

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.chats.Chat.Message
import java.util.*
import kotlin.random.Random

typealias Id = String

object DummyMessageGenerator {
    private val sampleTexts = listOf(
        "Hey there!",
        "Just finished the task.",
        "Let's meet at 10am tomorrow.",
        "Check this out: https://anytype.io",
        "This looks awesome! 🎉",
        "Can you review this file?",
        "I'll push the update in an hour."
    )

    private val emojis = listOf("👍", "❤️", "😂", "😮", "🎉", "🔥")

    fun generateMessage(
        text: String? = null,
        replyTo: Id? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Message {
        val id = UUID.randomUUID().toString()

        val content = Message.Content(
            text = text ?: sampleTexts.random(),
            style = Block.Content.Text.Style.P, // paragraph only
            marks = emptyList()
        )

        val attachments = generateAttachments()

        val reactions = emojis.associateWith {
            listOf("user1", "user2").shuffled().take(Random.nextInt(0, 2))
        }.filterValues { it.isNotEmpty() }

        return Message(
            id = id,
            order = id,
            creator = "", // empty creator
            createdAt = timestamp,
            modifiedAt = timestamp,
            content = content,
            attachments = attachments,
            reactions = reactions,
            replyToMessageId = replyTo,
            synced = false
        )
    }

    fun generateChatHistory(count: Int): List<Message> {
        val baseTime = System.currentTimeMillis() - 100000
        return (0 until count).map {
            generateMessage(timestamp = baseTime + it * 1000L)
        }
    }

    private fun generateAttachments(): List<Message.Attachment> {
        return when (Random.nextInt(4)) {
            0 -> listOf(Message.Attachment(UUID.randomUUID().toString(), Message.Attachment.Type.File))
            1 -> listOf(Message.Attachment(UUID.randomUUID().toString(), Message.Attachment.Type.Image))
            2 -> listOf(Message.Attachment(UUID.randomUUID().toString(), Message.Attachment.Type.Link))
            else -> emptyList()
        }
    }
}