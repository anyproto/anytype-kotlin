package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import timber.log.Timber
import java.lang.ref.WeakReference

class MentionSpan constructor(
    private val emoji: String? = null,
    private val image: String? = null,
    private val onImageResourceReady: (String) -> Unit = {},
    private val context: Context,
    private val placeholder: Drawable,
    private var imageSize: Int,
    private var imagePadding: Int,
    val param: String
) : DynamicDrawableSpan(), Span {

    val target: CustomTarget<Drawable> = object : CustomTarget<Drawable>() {
        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
        ) {
            icon = resource
            icon?.setBounds(imageSize)
            iconRef = WeakReference(icon)
            onImageResourceReady(param)
        }

        override fun onLoadCleared(placeholder: Drawable?) = Unit
    }

    private val endPaddingPx = 4
    private var icon: Drawable? = null
    private var iconRef: WeakReference<Drawable>? = null

    init {
        placeholder.setBounds(imageSize)

        if (!emoji.isNullOrBlank()) try {
            Glide.with(context).load(Emojifier.uri(emoji)).into(target)
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading emoji: $emoji for mention span")
        }

        if (!image.isNullOrBlank()) Glide.with(context).load(image).circleCrop().into(target)
    }

    override fun getDrawable(): Drawable = icon ?: placeholder

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
        val wr = iconRef
        val d = wr?.get()
        if (d != null)
            return d
        val newDrawable = drawable
        iconRef = WeakReference(newDrawable)
        return newDrawable
    }
}

fun Drawable.setBounds(imageSize: Int): Drawable =
    this.apply {
        val mWidth = imageSize * intrinsicWidth / intrinsicHeight
        setBounds(0, 0, mWidth, imageSize)
    }