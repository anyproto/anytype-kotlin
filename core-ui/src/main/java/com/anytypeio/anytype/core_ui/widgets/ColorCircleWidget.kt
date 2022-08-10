package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import kotlin.math.min

class ColorCircleWidget : View {

    var innerColor: Int = 0
    private var innerStrokeColor: Int = 0
    private var innerRadius: Float = 0f
    private var outerStrokeWidth: Float = 0f
    private var outerStrokeColor: Int = 0
    private var showSlantedLine: Boolean = false
    private var slantedLineHeight: Int = 0

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
        showSlantedLine = attrs.getBoolean(R.styleable.ColorCircleWidget_showSlantedLine, false)
        slantedLineHeight = attrs.getDimensionPixelSize(
            R.styleable.ColorCircleWidget_slantedLineWidth,
            resources.getDimension(R.dimen.default_style_color_slanted_line_height).toInt()
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
            color = 0
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
                drawCircle(cx, cy, innerRadius - outerStrokeWidth / 4, paint)
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
                drawCircle(cx, cy, radius - outerStrokeWidth / 2, paint)
            }
        }

        if (showSlantedLine) {
            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = outerStrokeWidth / 2
                color = outerStrokeColor
            }
            val dx = slantedLineHeight / 2
            val dy = slantedLineHeight / 2
            canvas.apply {
                drawLine(
                    width / 2f,
                    height / 2f,
                    width / 2f + dx,
                    height / 2f - dy,
                    paint
                )
                drawLine(
                    width / 2f,
                    height / 2f,
                    width / 2f - dx,
                    height / 2f + dy,
                    paint
                )
            }
        }
    }
}