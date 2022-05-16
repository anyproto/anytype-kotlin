package com.anytypeio.anytype.core_ui.features.editor

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import kotlin.math.abs
import kotlin.math.min


class EditorDragAndDropListener(
    val onDragLocation: (v: View, ratio: Float) -> Unit,
    val onDrop: (v: View, event: DragEvent) -> Unit,
    val onDragEnded: (v: View, isMoved: Boolean) -> Unit,
    val onDragExited: (v: View) -> Unit,
    val onDragStart: () -> Unit
) : View.OnDragListener {

    private var isMoved = false

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_ENTERED -> {
                val ratio = event.y / v.height
                onDragLocation(v, ratio)
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                isMoved = true
                val ratio = event.y / v.height
                onDragLocation(v, ratio)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                onDragExited(v)
            }
            DragEvent.ACTION_DROP -> {
                onDrop(v, event)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                onDragEnded(v, isMoved)
            }
            DragEvent.ACTION_DRAG_STARTED -> {
                onDragStart()
                isMoved = false
            }
        }
        return true
    }
}

class DefaultEditorDragShadow(view: View, event: MotionEvent?) : DragSmoothShadowBuilder(
    view,
    event,
    viewHeightPadding = R.dimen.drag_shadow_padding_height_other,
    viewWidthPadding = R.dimen.drag_shadow_padding_width_other
)

class TextInputDragShadow(private val textInputId: Int?, parent: View, event: MotionEvent?) :
    DragSmoothShadowBuilder(parent, event) {
    val input: EditText?
        get() {
            return if (textInputId != null)
                view.findViewById(textInputId)
            else
                null
        }

    override fun onDrawShadow(canvas: Canvas) {
        input?.isCursorVisible = false
        super.onDrawShadow(canvas)
        input?.isCursorVisible = true
    }
}

open class DragSmoothShadowBuilder(
    view: View,
    event: MotionEvent? = null,
    @ColorRes shadowColor: Int = R.color.drag_shadow_alpha,
    @ColorRes backgroundColor: Int = R.color.white,
    @DimenRes cornerRadius: Int = R.dimen.drag_shadow_corner_radius,
    @DimenRes elevation: Int = R.dimen.drag_shadow_elevation,
    @DimenRes viewHeightPadding: Int = R.dimen.drag_shadow_padding_height_text,
    @DimenRes viewWidthPadding: Int = R.dimen.drag_shadow_padding_width_text,
) : View.DragShadowBuilder(view) {

    private val touchX = event?.x ?: 0f
    private val touchY = event?.y ?: 0f
    private val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)
    private val backgroundColorValue = ContextCompat.getColor(view.context, backgroundColor)
    private val cornerRadiusValue = view.context.resources.getDimension(cornerRadius)
    private val elevationValue = view.context.resources.getDimension(elevation).toInt()

    private var viewHeightPaddingValue =
        view.context.resources.getDimension(viewHeightPadding).toInt()
    private var viewWidthPaddingValue =
        view.context.resources.getDimension(viewWidthPadding).toInt()
    private var actualHeight = 0
    private var actualWidth = 0
    private var maxSize = view.context.resources.getDimension(R.dimen.drag_shadow_max_size)
    private val scale = if (min(view.width, view.height) > maxSize)
        maxSize * 1f / min(view.width, view.height) else 1f

    override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
        view?.let { view ->
            val lp = view.layoutParams
            actualWidth = (view.width + viewWidthPaddingValue * 2 / scale).toInt()
            actualHeight = (view.height + viewHeightPaddingValue * 2 / scale).toInt()

            if (lp is ViewGroup.MarginLayoutParams) {
                actualWidth += ((lp.leftMargin + lp.rightMargin) / scale).toInt()
                actualHeight += ((lp.topMargin + lp.bottomMargin) / scale).toInt()
                viewWidthPaddingValue += (lp.leftMargin /*/ scale*/).toInt()
                viewHeightPaddingValue += (lp.topMargin /*/ scale*/).toInt()
            }
            outShadowSize.set(actualWidth, actualHeight)

            val point = IntArray(2)
            view.getLocationOnScreen(point)
            val startX = point[0]
            val startY = point[1]
            val shadowTouchX =
                if (touchX == 0f) outShadowSize.x / 2f else abs((touchX - startX) * scale + viewWidthPaddingValue)
            val shadowTouchY =
                if (touchX == 0f) outShadowSize.y / 2f else abs((touchY - startY) * scale + viewHeightPaddingValue)
            outShadowTouchPoint.set(shadowTouchX.toInt(), shadowTouchY.toInt())
        }
    }

    override fun onDrawShadow(canvas: Canvas) {
        val view = view ?: return

        val shadowGravity: Int = Gravity.CENTER

        val firstLayer = 0
        val ratioTopBottom = 3
        val defaultRatio = 2

        val outerRadius = FloatArray(8) { cornerRadiusValue }

        val directionOfY = when (shadowGravity) {
            Gravity.CENTER -> 0
            Gravity.TOP -> -1 * elevationValue / ratioTopBottom
            Gravity.BOTTOM -> elevationValue / ratioTopBottom
            else -> elevationValue / defaultRatio // Gravity.LEFT & Gravity.RIGHT
        }

        val directionOfX = when (shadowGravity) {
            Gravity.LEFT -> -1 * elevationValue / ratioTopBottom
            Gravity.RIGHT -> elevationValue / ratioTopBottom
            else -> 0
        }

        val shapeDrawable = ShapeDrawable()
        shapeDrawable.paint.setShadowLayer(
            cornerRadiusValue / ratioTopBottom,
            directionOfX.toFloat(),
            directionOfY.toFloat(),
            shadowColorValue
        )

        shapeDrawable.paint.color = backgroundColorValue
        shapeDrawable.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)

        when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.BASE..Build.VERSION_CODES.O_MR1 -> view.setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                shapeDrawable.paint
            )
        }

        val drawable = LayerDrawable(arrayOf(shapeDrawable))
        drawable.setLayerInset(
            firstLayer,
            elevationValue,
            elevationValue * defaultRatio,
            elevationValue,
            elevationValue * defaultRatio
        )

        drawable.setBounds(0, 0, actualWidth, actualHeight)
        canvas.scale(scale, scale)
        drawable.draw(canvas)
        canvas.translate(viewWidthPaddingValue / scale, viewHeightPaddingValue / scale)
        super.onDrawShadow(canvas)
    }
}