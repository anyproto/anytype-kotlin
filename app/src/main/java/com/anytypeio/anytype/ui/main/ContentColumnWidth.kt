package com.anytypeio.anytype.ui.main

import com.anytypeio.anytype.R

/**
 * The value that removes the maximum width of the content column. ConstraintLayout reads zero as
 * "no maximum", so the column then fills the window.
 */
const val NO_MAX_WIDTH = 0

/**
 * The destinations that fill the whole window. The rule holds on a phone and on a tablet.
 *
 * The set screen and the type screen show a data view: a grid, a gallery, or a Kanban board. The
 * reader compares the columns of a board side by side, so the board needs the whole window. The
 * editor keeps the capped column, because a full width line of text is hard to read.
 */
private val FULL_WIDTH_DESTINATIONS = setOf(
    R.id.objectSetScreen,
    R.id.objectTypeScreen
)

/**
 * The destinations that show the wallpaper of the space.
 *
 * The content of these screens is transparent, so the wallpaper fills the window behind it. Every
 * other screen paints an opaque background over the column. There the wallpaper reaches the eye
 * only in the strip beside a capped column, which reads as a defect.
 */
private val WALLPAPER_DESTINATIONS = setOf(
    R.id.homeScreen,
    R.id.homeScreenWidgets,
    R.id.vaultScreen
)

/**
 * The maximum width of the content column, in pixels, for one navigation destination.
 *
 * @param destinationId the identifier of the current navigation destination.
 * @param cappedWidthPx the value of `@dimen/max_content_width` for the current window.
 * @return [NO_MAX_WIDTH] when the destination fills the window, otherwise [cappedWidthPx].
 */
fun contentColumnMaxWidth(
    destinationId: Int,
    cappedWidthPx: Int
): Int = if (destinationId in FULL_WIDTH_DESTINATIONS) {
    NO_MAX_WIDTH
} else {
    cappedWidthPx
}

/**
 * True when the root paints the wallpaper of the space for this destination. The root paints a
 * plain backdrop for every other destination.
 *
 * @param destinationId the identifier of the current navigation destination.
 */
fun showsWallpaper(destinationId: Int): Boolean = destinationId in WALLPAPER_DESTINATIONS
