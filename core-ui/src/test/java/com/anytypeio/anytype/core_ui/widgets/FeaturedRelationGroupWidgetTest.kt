package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.os.Build
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class FeaturedRelationGroupWidgetTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun before() {
        context.setTheme(R.style.Theme_MaterialComponents)
    }

    private fun givenItem(count: Int) = BlockView.FeaturedRelation(
        id = MockDataFactory.randomUuid(),
        relations = (1..count).map { index ->
            ObjectRelationView.Default(
                id = MockDataFactory.randomUuid(),
                key = MockDataFactory.randomUuid(),
                name = "Relation $index",
                value = "value $index",
                system = false,
                format = Relation.Format.SHORT_TEXT
            )
        }
    )

    @Test
    fun `all children are measured and the widget wraps its content height`() {
        val widget = FeaturedRelationGroupWidget(context)

        widget.set(item = givenItem(count = 3), click = {})

        widget.measure(
            View.MeasureSpec.makeMeasureSpec(1016, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1838, View.MeasureSpec.AT_MOST)
        )
        widget.layout(0, 0, widget.measuredWidth, widget.measuredHeight)

        // The broken ConstraintLayout+Flow implementation left children unmeasured
        // (0x0) while the widget itself expanded to the full AT_MOST height.
        (0 until widget.childCount).forEach { index ->
            val child = widget.getChildAt(index)
            assertTrue(
                actual = child.measuredHeight > 0 && child.measuredWidth > 0,
                message = "Child $index must be measured but was " +
                    "${child.measuredWidth}x${child.measuredHeight}"
            )
        }
        assertTrue(
            actual = widget.measuredHeight in 1 until 1838,
            message = "Widget must wrap content height but was ${widget.measuredHeight}"
        )
    }

    @Test
    fun `re-populating the widget re-measures the new children`() {
        val widget = FeaturedRelationGroupWidget(context)

        widget.set(item = givenItem(count = 8), click = {})
        widget.set(item = givenItem(count = 1), click = {})

        widget.measure(
            View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST)
        )
        widget.layout(0, 0, widget.measuredWidth, widget.measuredHeight)

        assertTrue(
            actual = widget.childCount == 1,
            message = "Repopulation must replace children but count was ${widget.childCount}"
        )
        val child = widget.getChildAt(0)
        assertTrue(
            actual = child.measuredWidth > 0 && child.measuredHeight > 0,
            message = "Re-populated child must be measured but was " +
                "${child.measuredWidth}x${child.measuredHeight}"
        )
    }

    /** A child whose size does not depend on Robolectric's zero-width text measurement. */
    private class FixedSizeView(
        context: Context,
        private val w: Int,
        private val h: Int
    ) : View(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(w, h)
        }
    }

    @Test
    fun `children wrap to new rows when the available width is exhausted`() {
        val widget = FeaturedRelationGroupWidget(context)
        repeat(6) { widget.addView(FixedSizeView(context, w = 100, h = 20)) }

        widget.measure(
            View.MeasureSpec.makeMeasureSpec(3000, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val singleRowHeight = widget.measuredHeight

        widget.measure(
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        widget.layout(0, 0, 300, widget.measuredHeight)
        val wrappedHeight = widget.measuredHeight

        // 6 children x 100px in a 300px-wide widget = 2 rows of 3.
        assertTrue(
            actual = singleRowHeight == 20 && wrappedHeight == 40,
            message = "Narrow width must wrap rows (wide=$singleRowHeight, narrow=$wrappedHeight)"
        )
        // Second row children are laid out below the first row.
        assertTrue(
            actual = widget.getChildAt(3).top == 20 && widget.getChildAt(3).left == 0,
            message = "4th child must start the 2nd row but was at " +
                "(${widget.getChildAt(3).left}, ${widget.getChildAt(3).top})"
        )
    }
}
