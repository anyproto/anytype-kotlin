package com.anytypeio.anytype.core_ui.extensions

import android.view.View
import android.widget.HorizontalScrollView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
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
