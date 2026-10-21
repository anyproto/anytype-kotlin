package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A holder binds a view before the list measures it, and the view then sizes its text from
 * [View.contentWidth]. The value must match the column that holds the view. The editor, the set
 * screen, and the type screen fill the window, so a fallback capped at 600dp under-reported the
 * width on a tablet (DROID-4601).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ContentWidthTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private companion object {
        const val COLUMN = 2560
    }

    @Test
    fun `a measured view reports its own width`() {
        val view = View(context)
        val spec = View.MeasureSpec.makeMeasureSpec(COLUMN, View.MeasureSpec.EXACTLY)
        view.measure(spec, spec)
        assertEquals(COLUMN, view.contentWidth())
    }

    @Test
    fun `an unmeasured view reports the width of the nearest ancestor with a size`() {
        val parent = FrameLayout(context)
        val spec = View.MeasureSpec.makeMeasureSpec(COLUMN, View.MeasureSpec.EXACTLY)
        parent.measure(spec, spec)
        parent.layout(0, 0, COLUMN, COLUMN)
        val child = View(context)
        // The parent adds the child after its own layout, so the child has no size yet.
        parent.addView(child)
        assertEquals(0, child.measuredWidth)
        assertEquals(COLUMN, child.contentWidth())
    }

    @Test
    fun `a detached unmeasured view reports the display width without a cap`() {
        val view = View(context)
        assertEquals(context.resources.displayMetrics.widthPixels, view.contentWidth())
    }
}
