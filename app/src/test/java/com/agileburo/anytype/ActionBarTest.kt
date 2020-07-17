package com.agileburo.anytype

import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarHelper
import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarHelper.blockVisibilityState
import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarHelper.canShowBlockAtTheBottom
import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarHelper.getAnchorView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionBarTest {

    @Test
    fun `action bar should be anchor view, when top and bottom are outside`() {

        val screenTop = 0
        val blockTop = -200

        val screenBottom = 1000
        val blockBottom = 1200

        val blockHeight = 1400

        val barHeight = 250
        val barMarginTop = 49
        val barMarginBottom = 20

        val result = getAnchorView(
            screenBottom = screenBottom,
            screenTop = screenTop,
            blockBottom = blockBottom,
            blockTop = blockTop,
            barMarginBottom = barMarginBottom,
            barMarginTop = barMarginTop,
            barHeight = barHeight,
            blockHeight = blockHeight
        )

        val expected = BlockActionToolbarHelper.AnchorView.ACTION_BAR

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `action bar should be anchor view, when only bottom visible and not enough space`() {

        val screenTop = 0
        val blockTop = -200

        val screenBottom = 1000
        val blockBottom = 800

        val blockHeight = 1000

        val barHeight = 250
        val barMarginTop = 49
        val barMarginBottom = 20

        val result = getAnchorView(
            screenBottom = screenBottom,
            screenTop = screenTop,
            blockBottom = blockBottom,
            blockTop = blockTop,
            barMarginBottom = barMarginBottom,
            barMarginTop = barMarginTop,
            barHeight = barHeight,
            blockHeight = blockHeight
        )

        val expected = BlockActionToolbarHelper.AnchorView.ACTION_BAR

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `block top gravity should be anchor view, when only bottom visible and enough space`() {

        val screenTop = 0
        val blockTop = -200

        val screenBottom = 1000
        val blockBottom = 300

        val blockHeight = 500

        val barHeight = 250
        val barMarginTop = 49
        val barMarginBottom = 20

        val result = getAnchorView(
            screenBottom = screenBottom,
            screenTop = screenTop,
            blockBottom = blockBottom,
            blockTop = blockTop,
            barMarginBottom = barMarginBottom,
            barMarginTop = barMarginTop,
            barHeight = barHeight,
            blockHeight = blockHeight
        )

        val expected = BlockActionToolbarHelper.AnchorView.BLOCK_GRAVITY_TOP

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `action bar should be anchor view, when only top visible`() {

        val screenTop = 0
        val blockTop = 800

        val screenBottom = 1000
        val blockBottom = 1300

        val blockHeight = 500

        val barHeight = 250
        val barMarginTop = 49
        val barMarginBottom = 20

        val result = getAnchorView(
            screenBottom = screenBottom,
            screenTop = screenTop,
            blockBottom = blockBottom,
            blockTop = blockTop,
            barMarginBottom = barMarginBottom,
            barMarginTop = barMarginTop,
            barHeight = barHeight,
            blockHeight = blockHeight
        )

        val expected = BlockActionToolbarHelper.AnchorView.ACTION_BAR

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `block should be anchor view, when full visible, and enough space`() {

        val screenTop = 0
        val blockTop = 100

        val screenBottom = 1000
        val blockBottom = 300

        val blockHeight = 200

        val barHeight = 250
        val barMarginTop = 49
        val barMarginBottom = 20

        val result = getAnchorView(
            screenBottom = screenBottom,
            screenTop = screenTop,
            blockBottom = blockBottom,
            blockTop = blockTop,
            barMarginBottom = barMarginBottom,
            barMarginTop = barMarginTop,
            barHeight = barHeight,
            blockHeight = blockHeight
        )

        val expected = BlockActionToolbarHelper.AnchorView.BLOCK

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `action bar should be anchor view, when full visible, and not enough space`() {

        val screenTop = 0
        val blockTop = 100

        val screenBottom = 1000
        val blockBottom = 800

        val blockHeight = 700

        val barHeight = 250
        val barMarginTop = 49
        val barMarginBottom = 20

        val result = getAnchorView(
            screenBottom = screenBottom,
            screenTop = screenTop,
            blockBottom = blockBottom,
            blockTop = blockTop,
            barMarginBottom = barMarginBottom,
            barMarginTop = barMarginTop,
            barHeight = barHeight,
            blockHeight = blockHeight
        )

        val expected = BlockActionToolbarHelper.AnchorView.ACTION_BAR

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `block visibility is FULL`() {

        val screenTop = 0
        val blockTop = 100

        val screenBottom = 1000
        val blockBottom = 300

        val result = blockVisibilityState(screenTop, screenBottom, blockTop, blockBottom)

        val expected = BlockActionToolbarHelper.BlockVisibility.FULL
        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `block visibility is TOP`() {

        val screenTop = 0
        val blockTop = 100

        val screenBottom = 1000
        val blockBottom = 1001

        val result = blockVisibilityState(screenTop, screenBottom, blockTop, blockBottom)

        val expected = BlockActionToolbarHelper.BlockVisibility.BOTTOM_OUTSIDE
        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `block visibility is BOTTOM`() {

        val screenTop = 0
        val blockTop = -1

        val screenBottom = 1000
        val blockBottom = 999

        val result = blockVisibilityState(screenTop, screenBottom, blockTop, blockBottom)

        val expected = BlockActionToolbarHelper.BlockVisibility.TOP_OUTSIDE
        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `block visibility is TOP_BOTTOM_OUTSIDE`() {

        val screenTop = 0
        val blockTop = -1

        val screenBottom = 1000
        val blockBottom = 1001

        val result = blockVisibilityState(screenTop, screenBottom, blockTop, blockBottom)

        val expected = BlockActionToolbarHelper.BlockVisibility.TOP_BOTTOM_OUTSIDE
        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `can show action bar at the blocks bottom`() {

        val barHeight = 400
        val barMarginTop = 80
        val barMarginBottom = 20
        val blockBottom = 499
        val screenBottom = 1000

        val result = canShowBlockAtTheBottom(
            blockBottom = blockBottom,
            barHeight = barHeight,
            screenBottom = screenBottom,
            barMarginTop = barMarginTop,
            barMarginBottom = barMarginBottom
        )

        assertTrue(result)
    }

    @Test
    fun `can't show action bar at the blocks bottom`() {

        val barHeight = 400
        val barMarginTop = 80
        val barMarginBottom = 20
        val blockBottom = 501
        val screenBottom = 1000

        val result = canShowBlockAtTheBottom(
            blockBottom = blockBottom,
            barHeight = barHeight,
            screenBottom = screenBottom,
            barMarginTop = barMarginTop,
            barMarginBottom = barMarginBottom
        )

        assertFalse(result)
    }
}