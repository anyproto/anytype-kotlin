package com.agileburo.anytype.core_ui.features.page

import org.junit.Assert.assertEquals
import org.junit.Test

class BlockViewTest {

    val ID = "123"

    @Test
    fun `should return video block with view type Empty`() {

        val block = BlockView.VideoEmpty(id = ID)

        assertEquals(BlockViewHolder.HOLDER_VIDEO_EMPTY, block.getViewType())
    }

    @Test
    fun `should return video block with view type Error`() {

        val block = BlockView.VideoError(id = ID)

        assertEquals(BlockViewHolder.HOLDER_VIDEO_ERROR, block.getViewType())
    }

    @Test
    fun `should return video block with view type Done`() {

        val block = BlockView.Video(id = ID, hash = "", url = "", size = 0L, mime = "", name = "")

        assertEquals(BlockViewHolder.HOLDER_VIDEO, block.getViewType())
    }

    @Test
    fun `should return video block with view type Uploading`() {

        val block = BlockView.VideoUpload(id = ID)

        assertEquals(BlockViewHolder.HOLDER_VIDEO_UPLOAD, block.getViewType())
    }
}