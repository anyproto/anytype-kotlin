package com.agileburo.anytype.ui.page.modals.actions

object BlockActionToolbarHelper {


    enum class BlockVisibility {
        FULL, // block is fully visible
        BOTTOM_OUTSIDE,  // we can see only top of the block
        TOP_OUTSIDE,  // we can see only bottom of the block
        TOP_BOTTOM_OUTSIDE // block top and bottom are off the screen
    }


    /**
     * Positioning in action toolbar
     * @property ACTION_BAR, positioning action bar at the bottom, block at the top
     * @property BLOCK_GRAVITY_TOP, draw block at the most top position, then action bar at the bottom
     * @property BLOCK, draw block at exact start coordinates, then action bar at the bottom
     */
    enum class AnchorView {
        ACTION_BAR,
        BLOCK_GRAVITY_TOP,
        BLOCK
    }

    fun blockVisibilityState(
        screenTop: Int,
        screenBottom: Int,
        blockTop: Int,
        blockBottom: Int
    ): BlockVisibility =
        when (blockTop > screenTop) {
            true -> {
                when (blockBottom < screenBottom) {
                    true -> BlockVisibility.FULL
                    false -> BlockVisibility.BOTTOM_OUTSIDE
                }
            }
            false -> {
                when (blockBottom < screenBottom) {
                    true -> BlockVisibility.TOP_OUTSIDE
                    false -> BlockVisibility.TOP_BOTTOM_OUTSIDE
                }
            }
        }

    fun canShowBlockAtTheBottom(
        blockBottom: Int,
        barHeight: Int,
        screenBottom: Int,
        barMarginTop: Int,
        barMarginBottom: Int
    ): Boolean = blockBottom + barHeight + barMarginTop + barMarginBottom < screenBottom

    fun getAnchorView(
        screenTop: Int,
        screenBottom: Int,
        blockTop: Int,
        blockBottom: Int,
        blockHeight: Int,
        barHeight: Int,
        barMarginTop: Int,
        barMarginBottom: Int
    ): AnchorView = when (blockVisibilityState(screenTop, screenBottom, blockTop, blockBottom)) {
        BlockVisibility.FULL -> {
            when (canShowBlockAtTheBottom(
                blockBottom = blockBottom,
                barHeight = barHeight,
                screenBottom = screenBottom,
                barMarginTop = barMarginTop,
                barMarginBottom = barMarginBottom
            )) {
                true -> AnchorView.BLOCK
                false -> AnchorView.ACTION_BAR
            }
        }
        BlockVisibility.BOTTOM_OUTSIDE -> AnchorView.ACTION_BAR
        BlockVisibility.TOP_OUTSIDE -> {
            when (screenTop + blockHeight + barHeight + barMarginTop + barMarginBottom < screenBottom) {
                true -> AnchorView.BLOCK_GRAVITY_TOP
                false -> AnchorView.ACTION_BAR
            }
        }
        BlockVisibility.TOP_BOTTOM_OUTSIDE -> AnchorView.ACTION_BAR
    }
}