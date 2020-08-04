package com.agileburo.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import com.agileburo.anytype.core_ui.common.Span
import java.lang.ref.WeakReference

class MentionSpan constructor(
    private var context: Context,
    private var mResourceId: Int = 0,
    private var bitmap: Bitmap? = null,
    private var imageSize: Int,
    private var imagePadding: Int,
    val param: String
) : DynamicDrawableSpan(), Span {

    private val endPaddingPx = 4
    private var mDrawable: Drawable? = null
    private var mDrawableRef: WeakReference<Drawable>? = null

    override fun getDrawable(): Drawable = mDrawable ?: initDrawable()

    private fun initDrawable(): Drawable {
        val d = if (bitmap != null) {
            BitmapDrawable(context.resources, bitmap).apply {
                val mWidth = imageSize * intrinsicWidth / intrinsicHeight
                setBounds(0, 0, mWidth, imageSize)
            }
        } else {
            context.resources.getDrawable(mResourceId, null).apply {
                val mWidth = imageSize * intrinsicWidth / intrinsicHeight
                setBounds(0, 0, mWidth, imageSize)
            }
        }
        mDrawable = d
        return d
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fontMetrics: Paint.FontMetricsInt?
    ): Int {
        if (fontMetrics != null) {
            val paintFontMetrics = paint.fontMetrics
            fontMetrics.top = paintFontMetrics.top.toInt()
            fontMetrics.bottom = paintFontMetrics.bottom.toInt()
        }
        val rect = Rect()
        paint.getTextBounds(text.toString(), start, end, rect)
        return imageSize + imagePadding + (rect.right - rect.left) + endPaddingPx
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {

        val drawable = getCachedDrawable()
        val paintFontMetrics = paint.fontMetrics
        val fontHeight = paintFontMetrics.descent - paintFontMetrics.ascent
        val centerY = y + paintFontMetrics.descent - fontHeight / 2
        val transitionY = centerY - imageSize / 2

        canvas.save()
        canvas.translate(x, transitionY)
        drawable.draw(canvas)

        paint.flags = Paint.UNDERLINE_TEXT_FLAG
        canvas.drawText(
            text.substring(start, end),
            imageSize + imagePadding.toFloat(),
            y - transitionY,
            paint
        )
        canvas.restore()
    }

    private fun getCachedDrawable(): Drawable {
        if (mDrawableRef == null || mDrawableRef?.get() == null) {
            mDrawableRef = WeakReference(drawable)
        }
        return mDrawableRef!!.get()!!
    }
}