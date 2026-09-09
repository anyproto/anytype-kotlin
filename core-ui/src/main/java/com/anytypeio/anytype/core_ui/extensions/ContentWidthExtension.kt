package com.anytypeio.anytype.core_ui.extensions

import android.view.View
import android.widget.HorizontalScrollView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_utils.R

/**
 * Width of the single content column, in pixels.
 *
 * The activity layout caps the column at [R.dimen.max_content_width] and centers it. Therefore
 * `resources.displayMetrics.widthPixels` overstates the space on a tablet and on a phone in
 * landscape. A view that sizes itself from the display becomes wider than the column that holds
 * it. Call this on the container that owns the space instead.
 *
 * The receiver reports its own size when it has one. Before the first measure pass the value
 * falls back to the display width, capped by the same dimension.
 */
fun View.contentWidth(): Int =
    measuredWidth.takeIf { it > 0 }
        ?: width.takeIf { it > 0 }
        ?: minOf(
            resources.displayMetrics.widthPixels,
            resources.getDimensionPixelSize(R.dimen.max_content_width)
        )

/**
 * Width of the visible area for a view inside a horizontally scrolling container, in pixels.
 *
 * A [HorizontalScrollView] measures its child with `UNSPECIFIED`, so the child reports the width
 * of its whole content rather than the width the user sees. A view that must match the visible
 * area, such as a row header pinned with `translationX`, has to read the scroll container
 * instead. Without such an ancestor the receiver itself is the visible area.
 */
fun View.horizontalViewportWidth(): Int {
    var candidate: View? = this
    while (candidate != null && candidate !is HorizontalScrollView) {
        candidate = candidate.parent as? View
    }
    return (candidate ?: this).contentWidth()
}

/**
 * Width of the single content column, in density independent pixels.
 *
 * This is the Compose counterpart of [contentWidth]. Use it where a composable must size itself
 * against the column, for example a field title that takes at most half of its row.
 * `LocalConfiguration.current.screenWidthDp` reports the whole window, which is too wide once
 * the activity layout caps the column.
 */
@Composable
@ReadOnlyComposable
fun contentWidthDp(): Dp = minOf(
    LocalConfiguration.current.screenWidthDp.dp,
    dimensionResource(id = R.dimen.max_content_width)
)

/**
 * Half of the width of a field row, in pixels.
 *
 * A field title and a field value each take at most half of the row that holds them. [deductPx]
 * holds the two apart.
 *
 * @param availableWidthPx the space that the parent offers, in pixels.
 * @param deductPx the gap between the title and the value, in pixels.
 * @return half of [availableWidthPx], minus [deductPx], and never less than zero. An unbounded
 * row stays unbounded.
 */
internal fun halfRowWidthPx(availableWidthPx: Int, deductPx: Int): Int =
    if (availableWidthPx == Constraints.Infinity) {
        availableWidthPx
    } else {
        (availableWidthPx / 2 - deductPx).coerceAtLeast(0)
    }

/**
 * The gap between the title of a field and the value beside it.
 */
private val HALF_ROW_DEDUCT = 16.dp

/**
 * Limits the content to half of the row that holds it.
 *
 * The row is not always as wide as the window. The activity layout caps the content column on a
 * tablet, and the editor, the set, and the type screen fill the window on a phone in landscape.
 * The modifier reads the space that the parent offers, so it stays correct in both cases.
 * Neither [contentWidthDp] nor `LocalConfiguration.current.screenWidthDp` can report this.
 */
fun Modifier.halfRowWidth(): Modifier = layout { measurable, constraints ->
    val max = halfRowWidthPx(
        availableWidthPx = constraints.maxWidth,
        deductPx = HALF_ROW_DEDUCT.roundToPx()
    )
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = minOf(constraints.minWidth, max),
            maxWidth = max
        )
    )
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}
