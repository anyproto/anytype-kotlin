package com.agileburo.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.agileburo.anytype.core_ui.R
import kotlin.math.min

class ColorCircleWidget : View {

    private var innerColor: Int = 0
    private var innerStrokeColor: Int = 0
    private var innerRadius : Float = 0f
    private var outerStrokeWidth: Float = 0f
    private var outerStrokeColor: Int = 0

    private val paint = Paint(ANTI_ALIAS_FLAG)

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        attrs?.let { setAttributes(it) }
    }

    private fun setAttributes(set: AttributeSet) {
        val attrs = context.obtainStyledAttributes(set, R.styleable.ColorCircleWidget, 0, 0)
        innerColor = attrs.getColor(R.styleable.ColorCircleWidget_innerColor, 0)
        innerRadius = attrs.getDimensionPixelSize(
            R.styleable.ColorCircleWidget_innerRadius,
            resources.getDimension(R.dimen.default_style_color_circle_inner_radius).toInt()
        ).toFloat()
        innerStrokeColor = attrs.getColor(
            R.styleable.ColorCircleWidget_innerStrokeColor,
            ContextCompat.getColor(context, R.color.default_style_color_circle_inner_stroke_color)
        )
        outerStrokeWidth = attrs.getDimensionPixelSize(
            R.styleable.ColorCircleWidget_outerStrokeWidth,
            resources.getDimension(R.dimen.default_style_color_outer_stroke_width).toInt()
        ).toFloat()
        outerStrokeColor = attrs.getColor(
            R.styleable.ColorCircleWidget_outerStrokeColor,
            ContextCompat.getColor(context, R.color.default_style_color_circle_outer_stroke_color)
        )
        attrs.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f

        // background

        paint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.apply { drawCircle(cx, cy, radius, paint) }

        // inner circle

        paint.apply { color = innerColor }
        canvas.apply { drawCircle(cx, cy, innerRadius, paint) }

        // inner stroke for white color

        if (innerColor == Color.WHITE) {
            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = outerStrokeWidth / 2
                color = innerStrokeColor
            }
            canvas.apply {
                drawCircle(cx, cy, innerRadius - outerStrokeWidth / 4 , paint)
            }
        }

        // outer stroke for selected state

        if (isSelected) {
            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = outerStrokeWidth
                color = outerStrokeColor
            }
            canvas.apply {
                drawCircle(cx, cy, radius - outerStrokeWidth / 2 , paint)
            }
        }
    }
}