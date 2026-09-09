package com.anytypeio.anytype.ui.main

import com.anytypeio.anytype.R

/**
 * The value that removes the maximum width of the content column. ConstraintLayout reads zero as
 * "no maximum", so the column then fills the window.
 */
const val NO_MAX_WIDTH = 0

/**
 * The destinations that fill the whole window on a phone.
 *
 * The editor, the set, and the type screen show a wide body: a text column with an inline
 * toolbar, a data view grid, and a property list. A phone in landscape gives these screens too
 * little height for the capped column to help the reader. A tablet keeps the capped column,
 * because a full width line of text is hard to read there.
 */
private val FULL_WIDTH_DESTINATIONS = setOf(
    R.id.pageScreen,
    R.id.objectSetScreen,
    R.id.objectTypeScreen
)

/**
 * The maximum width of the content column, in pixels, for one navigation destination.
 *
 * @param destinationId the identifier of the current navigation destination.
 * @param isTablet true when the shortest side of the window is at least 600dp.
 * @param cappedWidthPx the value of `@dimen/max_content_width` for the current window.
 * @return [NO_MAX_WIDTH] when the destination fills the window, otherwise [cappedWidthPx].
 */
fun contentColumnMaxWidth(
    destinationId: Int,
    isTablet: Boolean,
    cappedWidthPx: Int
): Int = if (!isTablet && destinationId in FULL_WIDTH_DESTINATIONS) {
    NO_MAX_WIDTH
} else {
    cappedWidthPx
}
