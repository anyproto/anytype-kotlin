package com.agileburo.anytype.core_ui.features.page

import com.agileburo.anytype.core_ui.MockDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockViewTest {

    @Test
    fun `should return video block with view type Empty`() {
        val block = BlockView.MediaPlaceholder.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(BlockViewHolder.HOLDER_VIDEO_PLACEHOLDER, block.getViewType())
    }

    @Test
    fun `should return video block with view type Error`() {
        val block = BlockView.Video.Error(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(BlockViewHolder.HOLDER_VIDEO_ERROR, block.getViewType())
    }

    @Test
    fun `should return video block with view type Done`() {
        val block = BlockView.Video.View(
            id = MockDataFactory.randomUuid(),
            hash = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            mime = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(BlockViewHolder.HOLDER_VIDEO, block.getViewType())
    }

    @Test
    fun `should return video block with view type Uploading`() {
        val block = BlockView.Upload.Video(
            id = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(BlockViewHolder.HOLDER_VIDEO_UPLOAD, block.getViewType())
    }
}