package com.anytypeio.anytype.feature_chats.presentation

import com.anytypeio.anytype.core_models.Block
import org.junit.Test
import kotlin.test.assertEquals

class ContentToPlainTextTest {

    @Test
    fun `toPlainText should return text without emojis when no emoji marks present`() {
        val content = ChatView.Message.Content(
            msg = "Hello world",
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = "Hello world",
                    styles = emptyList()
                )
            )
        )
        
        assertEquals("Hello world", content.toPlainText())
    }

    @Test
    fun `toPlainText should return text with emojis when emoji marks are present`() {
        val content = ChatView.Message.Content(
            msg = "Hello world",
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = "Hello ",
                    styles = emptyList()
                ),
                ChatView.Message.Content.Part(
                    part = "",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 6..7,
                            type = Block.Content.Text.Mark.Type.EMOJI,
                            param = "😀"
                        )
                    )
                ),
                ChatView.Message.Content.Part(
                    part = " world",
                    styles = emptyList()
                )
            )
        )
        
        assertEquals("Hello 😀 world", content.toPlainText())
    }

    @Test
    fun `toPlainText should handle multiple emojis correctly`() {
        val content = ChatView.Message.Content(
            msg = "Hello world",
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = "Hello ",
                    styles = emptyList()
                ),
                ChatView.Message.Content.Part(
                    part = "",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 6..7,
                            type = Block.Content.Text.Mark.Type.EMOJI,
                            param = "😀"
                        )
                    )
                ),
                ChatView.Message.Content.Part(
                    part = " world ",
                    styles = emptyList()
                ),
                ChatView.Message.Content.Part(
                    part = "",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 14..15,
                            type = Block.Content.Text.Mark.Type.EMOJI,
                            param = "🎉"
                        )
                    )
                )
            )
        )
        
        assertEquals("Hello 😀 world 🎉", content.toPlainText())
    }

    @Test
    fun `toPlainText should handle text with bold and emoji marks`() {
        val content = ChatView.Message.Content(
            msg = "Hello world",
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = "Hello ",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 0..5,
                            type = Block.Content.Text.Mark.Type.BOLD
                        )
                    )
                ),
                ChatView.Message.Content.Part(
                    part = "",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 6..7,
                            type = Block.Content.Text.Mark.Type.EMOJI,
                            param = "😀"
                        )
                    )
                ),
                ChatView.Message.Content.Part(
                    part = " world",
                    styles = emptyList()
                )
            )
        )
        
        assertEquals("Hello 😀 world", content.toPlainText())
    }

    @Test
    fun `toPlainText should handle only emojis`() {
        val content = ChatView.Message.Content(
            msg = "",
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = "",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 0..1,
                            type = Block.Content.Text.Mark.Type.EMOJI,
                            param = "😀"
                        )
                    )
                ),
                ChatView.Message.Content.Part(
                    part = "",
                    styles = listOf(
                        Block.Content.Text.Mark(
                            range = 1..2,
                            type = Block.Content.Text.Mark.Type.EMOJI,
                            param = "🎉"
                        )
                    )
                )
            )
        )
        
        assertEquals("😀🎉", content.toPlainText())
    }

    @Test
    fun `toPlainText should return empty string for empty content`() {
        val content = ChatView.Message.Content(
            msg = "",
            parts = emptyList()
        )
        
        assertEquals("", content.toPlainText())
    }
}
