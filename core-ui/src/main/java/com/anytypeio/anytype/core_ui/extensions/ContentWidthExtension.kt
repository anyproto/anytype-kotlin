package com.anytypeio.anytype.core_ui.extensions

import android.view.View
import android.widget.HorizontalScrollView
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_utils.R

/**
 * Width of the content column that holds the receiver, in pixels.
 *
 * The receiver reports its own size when it has one. Before the first measure pass the nearest
 * ancestor with a size stands in, because the ancestor already holds the column. A view without
 * such an ancestor, for example an item that a holder binds before the list adds it, falls back
 * to the display width.
 *
 * The fallback does not cap the width at [R.dimen.max_content_width]. Every caller lives in the
 * editor, on the set screen, or on the type screen, and these screens fill the window (see
 * `FULL_WIDTH_DESTINATIONS` in the app module). A cap under-reported the width there, and a
 * relation value then ellipsized at half of 600dp on a tablet. A caller in a capped column must
 * read an ancestor that has a size, because the display is wider than the column there.
 */
fun View.contentWidth(): Int =
    measuredWidth.takeIf { it > 0 }
        ?: width.takeIf { it > 0 }
        ?: sizedAncestorWidth()
        ?: resources.displayMetrics.widthPixels

/**
 * The width of the nearest ancestor that has a size, or null when no ancestor has one.
 */
private fun View.sizedAncestorWidth(): Int? {
    var candidate = parent as? View
    while (candidate != null) {
        val width = candidate.measuredWidth.takeIf { it > 0 } ?: candidate.width.takeIf { it > 0 }
        if (width != null) return width
        candidate = candidate.parent as? View
    }
    return null
}

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
 * The gap between the title of a field and the value beside it.
 */
private val HALF_ROW_DEDUCT = 16.dp

/**
 * The width of the row that holds a field, in density independent pixels.
 *
 * [FieldRow] provides the value, and [halfRowWidth] reads it. The value stays [Dp.Unspecified]
 * outside a [FieldRow], and [halfRowWidth] then sets no maximum.
 */
val LocalFieldRowWidth = compositionLocalOf { Dp.Unspecified }

/**
 * Half of the width of a field row, minus the gap that holds the two halves apart.
 *
 * @param rowWidth the width of the whole row.
 * @return half of [rowWidth], minus the gap, and never less than zero.
 */
internal fun halfRowWidth(rowWidth: Dp): Dp =
    (rowWidth / 2 - HALF_ROW_DEDUCT).coerceAtLeast(0.dp)

/**
 * The row that holds the title of a field and the value of that field.
 *
 * A `Row` measures one child after the other, and it offers a child only the space that the
 * children before it leave. A child therefore reads a maximum width that already misses the title,
 * and a value that halves that width takes a quarter of the row. This row measures itself first
 * and publishes its own width, so that each half stays a half.
 *
 * @param modifier the modifier of the row.
 * @param verticalAlignment the alignment of the children, as in a `Row`.
 * @param content the children of the row.
 */
@Composable
fun FieldRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        CompositionLocalProvider(LocalFieldRowWidth provides maxWidth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = verticalAlignment,
                content = content
            )
        }
    }
}

/**
 * Limits the content to half of the row that holds it.
 *
 * A field title and a field value each take at most half of their row. The row is not always as
 * wide as the window: the activity caps the content column on a wide window, and the set screen
 * and the type screen fill the window. [FieldRow] reports the width of the row, so the maximum
 * stays correct in both cases. Neither [contentWidthDp] nor
 * `LocalConfiguration.current.screenWidthDp` can report this.
 */
@Composable
fun Modifier.halfRowWidth(): Modifier {
    val rowWidth = LocalFieldRowWidth.current
    return if (rowWidth == Dp.Unspecified) this else widthIn(max = halfRowWidth(rowWidth))
}
