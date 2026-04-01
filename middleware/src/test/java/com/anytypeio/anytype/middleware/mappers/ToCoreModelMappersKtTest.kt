package com.anytypeio.anytype.middleware.mappers

import anytype.model.Range
import anytype.model.ChatMessage.MessageBlockText
import anytype.model.ChatMessage.MessageBlockLink
import anytype.model.ChatMessage.MessageBlockEmbed
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.chats.Chat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToCoreModelMappersKtTest {

    @Test
    fun `should set param as null when param_ is empty`() {

        val mark = MBMark(
            type = MBMarkType.Mention,
            range = Range(from = 0, to = 10),
            param_ = ""
        )

        val result = mark.toCoreModels()
        val expected = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should set param as string when param_ is empty`() {

        val param = "anytype.io"

        val mark = MBMark(
            type = MBMarkType.Mention,
            range = Range(from = 0, to = 10),
            param_ = param
        )

        val result = mark.toCoreModels()
        val expected = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        assertEquals(expected, result)
    }

    // region DROID-4327: Toggle Headers Display Support

    @Test
    fun `should map ToggleHeader1 to H1 for backward compatibility`() {
        val result = MBTextStyle.ToggleHeader1.toCoreModels()
        assertEquals(Block.Content.Text.Style.H1, result)
    }

    @Test
    fun `should map ToggleHeader2 to H2 for backward compatibility`() {
        val result = MBTextStyle.ToggleHeader2.toCoreModels()
        assertEquals(Block.Content.Text.Style.H2, result)
    }

    @Test
    fun `should map ToggleHeader3 to H3 for backward compatibility`() {
        val result = MBTextStyle.ToggleHeader3.toCoreModels()
        assertEquals(Block.Content.Text.Style.H3, result)
    }

    // endregion

    // region MessageBlock mapping

    @Test
    fun `should map text message block with marks`() {
        val block = MChatMessageBlock(
            text = MessageBlockText(
                text = "Hello world",
                style = MBTextStyle.Paragraph,
                marks = listOf(
                    MBMark(
                        type = MBMarkType.Bold,
                        range = Range(from = 0, to = 5)
                    )
                ),
                checked = false
            )
        )

        val result = block.toCoreMessageBlock()

        val expected = Chat.Message.MessageBlock.Text(
            text = "Hello world",
            style = Block.Content.Text.Style.P,
            marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    type = Block.Content.Text.Mark.Type.BOLD
                )
            ),
            checked = false
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should map link message block`() {
        val block = MChatMessageBlock(
            link = MessageBlockLink(
                targetObjectId = "obj-123",
                type = MessageBlockLink.LinkType.Object
            )
        )

        val result = block.toCoreMessageBlock()

        val expected = Chat.Message.MessageBlock.Link(
            targetObjectId = "obj-123",
            type = Chat.Message.MessageBlock.Link.LinkType.OBJECT
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should map file link type`() {
        val block = MChatMessageBlock(
            link = MessageBlockLink(
                targetObjectId = "file-1",
                type = MessageBlockLink.LinkType.File
            )
        )
        val result = block.toCoreMessageBlock() as Chat.Message.MessageBlock.Link
        assertEquals(Chat.Message.MessageBlock.Link.LinkType.FILE, result.type)
    }

    @Test
    fun `should map image link type`() {
        val block = MChatMessageBlock(
            link = MessageBlockLink(
                targetObjectId = "img-1",
                type = MessageBlockLink.LinkType.Image
            )
        )
        val result = block.toCoreMessageBlock() as Chat.Message.MessageBlock.Link
        assertEquals(Chat.Message.MessageBlock.Link.LinkType.IMAGE, result.type)
    }

    @Test
    fun `should map bookmark link type`() {
        val block = MChatMessageBlock(
            link = MessageBlockLink(
                targetObjectId = "bm-1",
                type = MessageBlockLink.LinkType.Bookmark
            )
        )
        val result = block.toCoreMessageBlock() as Chat.Message.MessageBlock.Link
        assertEquals(Chat.Message.MessageBlock.Link.LinkType.BOOKMARK, result.type)
    }

    @Test
    fun `should map embed message block`() {
        val block = MChatMessageBlock(
            embed = MessageBlockEmbed(text = "E = mc^2")
        )

        val result = block.toCoreMessageBlock()

        val expected = Chat.Message.MessageBlock.Embed(text = "E = mc^2")
        assertEquals(expected, result)
    }

    @Test
    fun `should return null for empty message block`() {
        val block = MChatMessageBlock()
        val result = block.toCoreMessageBlock()
        assertNull(result)
    }

    @Test
    fun `should map multiple blocks preserving order`() {
        val blocks = listOf(
            MChatMessageBlock(
                text = MessageBlockText(text = "First paragraph")
            ),
            MChatMessageBlock(
                text = MessageBlockText(text = "Second paragraph")
            ),
            MChatMessageBlock(
                link = MessageBlockLink(
                    targetObjectId = "obj-1",
                    type = MessageBlockLink.LinkType.Object
                )
            )
        )

        val result = blocks.mapNotNull { it.toCoreMessageBlock() }

        assertEquals(3, result.size)
        assertEquals("First paragraph", (result[0] as Chat.Message.MessageBlock.Text).text)
        assertEquals("Second paragraph", (result[1] as Chat.Message.MessageBlock.Text).text)
        assertEquals("obj-1", (result[2] as Chat.Message.MessageBlock.Link).targetObjectId)
    }

    // endregion
}
