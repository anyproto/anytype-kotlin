package com.agileburo.anytype.core_ui.features.page

import org.junit.Assert.assertEquals
import org.junit.Test

class BlockViewTest {

    val ID = "123"

    @Test
    fun `should return video block with view type Empty`() {

        val block = BlockView.Video.Placeholder(id = ID)

        assertEquals(BlockViewHolder.HOLDER_VIDEO_PLACEHOLDER, block.getViewType())
    }

    @Test
    fun `should return video block with view type Error`() {

        val block = BlockView.Video.Error(id = ID)

        assertEquals(BlockViewHolder.HOLDER_VIDEO_ERROR, block.getViewType())
    }

    @Test
    fun `should return video block with view type Done`() {

        val block =
            BlockView.Video.View(id = ID, hash = "", url = "", size = 0L, mime = "", name = "")

        assertEquals(BlockViewHolder.HOLDER_VIDEO, block.getViewType())
    }

    @Test
    fun `should return video block with view type Uploading`() {

        val block = BlockView.Video.Upload(id = ID)

        assertEquals(BlockViewHolder.HOLDER_VIDEO_UPLOAD, block.getViewType())
    }
}