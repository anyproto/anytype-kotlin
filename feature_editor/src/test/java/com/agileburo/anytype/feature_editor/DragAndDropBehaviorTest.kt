package com.agileburo.anytype.feature_editor

import com.agileburo.anytype.feature_editor.ui.getDropTargetCenter
import com.agileburo.anytype.feature_editor.ui.getConsumeBottomBorder
import com.agileburo.anytype.feature_editor.ui.getConsumeTopBorder
import com.agileburo.anytype.feature_editor.ui.getSelectedTargetCenter
import org.junit.Assert.assertEquals
import org.junit.Test

class DragAndDropBehaviorTest {

    val block = TestBlockView(top = 100, bottom = 300)

    val emptyBlock = TestBlockView(top = 0, bottom = 0)


    @Test
    fun `should return y coordinate center of view`() {
        val actualCenter = getDropTargetCenter(top = block.top, bottom = block.bottom)

        assertEquals(200, actualCenter)
    }

    @Test
    fun `should return 0 when get center`() {
        val actualCenter = getDropTargetCenter(top = emptyBlock.top, bottom = emptyBlock.bottom)

        assertEquals(0, actualCenter)
    }

    @Test
    fun `should return consume top border coordinate`() {
        val actualTopBorder = getConsumeTopBorder(top = block.top, bottom = block.bottom)

        assertEquals(172, actualTopBorder)
    }

    @Test
    fun `should return 0 consume top border`() {
        val actualTopBorder = getConsumeTopBorder(top = emptyBlock.top, bottom = emptyBlock.bottom)

        assertEquals(0, actualTopBorder)
    }

    @Test
    fun `should return consume bottom border coordinate`() {
        val actualTopBorder = getConsumeBottomBorder(top = block.top, bottom = block.bottom)

        assertEquals(228, actualTopBorder)
    }

    @Test
    fun `should return 0 consume bottom border`() {
        val actualTopBorder = getConsumeBottomBorder(top = emptyBlock.top, bottom = emptyBlock.bottom)

        assertEquals(0, actualTopBorder)
    }

    @Test
    fun `should return center of selected item`() {
        val actualCenter = getSelectedTargetCenter(curY = 400, height = 560)

        assertEquals(680, actualCenter)
    }
}

data class TestBlockView(val top: Int, val bottom: Int)