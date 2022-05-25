package com.anytypeio.anytype.core_ui.features.page

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_UPLOAD
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockViewTest {

    @Test
    fun `should return video block with view type Empty`() {
        val block = BlockView.MediaPlaceholder.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt(),
            isPreviousBlockMedia = false
        )
        assertEquals(HOLDER_VIDEO_PLACEHOLDER, block.getViewType())
    }

    @Test
    fun `should return video block with view type Error`() {
        val block = BlockView.Error.Video(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(HOLDER_VIDEO_ERROR, block.getViewType())
    }

    @Test
    fun `should return video block with view type Done`() {
        val block = BlockView.Media.Video(
            id = MockDataFactory.randomUuid(),
            hash = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            mime = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(HOLDER_VIDEO, block.getViewType())
    }

    @Test
    fun `should return video block with view type Uploading`() {
        val block = BlockView.Upload.Video(
            id = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )
        assertEquals(HOLDER_VIDEO_UPLOAD, block.getViewType())
    }
}