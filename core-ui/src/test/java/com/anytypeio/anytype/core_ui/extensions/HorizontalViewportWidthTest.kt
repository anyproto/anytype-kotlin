package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The grid row header is pinned to the visible area with `translationX`, so it must be as wide as
 * that area. Its row list sits inside a [HorizontalScrollView], which measures its child with
 * `UNSPECIFIED`. The list therefore reports the width of the whole grid, not the width the user
 * sees, and [View.contentWidth] on the list returns the wrong number (DROID-4402).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class HorizontalViewportWidthTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private companion object {
        const val VIEWPORT = 600
        const val CONTENT = 2400
    }

    /** Builds viewport -> HorizontalScrollView -> LinearLayout(wrap) -> child(match), measured. */
    private fun buildScrollingGrid(): Pair<View, HorizontalScrollView> {
        val child = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        // A sibling with a fixed width stands for the header row: it makes the wrap_content
        // column as wide as the whole grid content.
        val wide = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(CONTENT, 10)
        }
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(wide)
            addView(child)
        }
        val scroller = HorizontalScrollView(context).apply {
            layoutParams = FrameLayout.LayoutParams(VIEWPORT, 400)
            addView(column)
        }
        val root = FrameLayout(context).apply { addView(scroller) }

        val widthSpec = View.MeasureSpec.makeMeasureSpec(VIEWPORT, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
        root.measure(widthSpec, heightSpec)
        root.layout(0, 0, VIEWPORT, 400)
        return child to scroller
    }

    @Test
    fun `the child of a horizontal scroll view reports the content width, not the viewport`() {
        val (child, _) = buildScrollingGrid()
        // This is the trap the fix exists for.
        assertEquals(CONTENT, child.contentWidth())
    }

    @Test
    fun `horizontalViewportWidth returns the viewport of the scroll container`() {
        val (child, scroller) = buildScrollingGrid()
        assertEquals(VIEWPORT, scroller.width)
        assertEquals(VIEWPORT, child.horizontalViewportWidth())
    }

    @Test
    fun `without a scroll container the view itself is the viewport`() {
        val view = View(context)
        val spec = View.MeasureSpec.makeMeasureSpec(VIEWPORT, View.MeasureSpec.EXACTLY)
        view.measure(spec, spec)
        assertEquals(VIEWPORT, view.horizontalViewportWidth())
    }
}
