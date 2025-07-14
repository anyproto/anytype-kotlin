package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.DynamicDrawableSpan
import coil3.Image
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.emojifier.Emojifier
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.target.Target
import coil3.transform.CircleCropTransformation
import java.lang.ref.WeakReference
import timber.log.Timber

class MentionSpan constructor(
    emoji: String? = null,
    image: String? = null,
    profile: String? = null,
    private val onImageResourceReady: (String) -> Unit = {},
    context: Context,
    private val placeholder: Drawable? = null,
    private var imageSize: Int,
    private var imagePadding: Int,
    val param: String? = null,
    val isDeleted: Boolean = false,
    private val initials: String? = null,
    private val initialsTextSize: Float = 0F,
    private val isArchived: Boolean
) : DynamicDrawableSpan(), Span {

    val target: Target = object : Target {
        override fun onSuccess(result: Image) {
            icon = result.asDrawable(context.resources)
            icon?.setBounds(imageSize)
            iconRef = WeakReference(icon)
            if (param != null) onImageResourceReady(param)
        }
    }

    private val endPaddingPx = 4
    private var icon: Drawable? = null
    private var iconRef: WeakReference<Drawable>? = null
    private val textColorDeleted = context.color(R.color.text_tertiary)
    private val textColorArchive = context.color(R.color.text_tertiary)

    init {
        placeholder?.setBounds(imageSize)
        val imageLoader = context.imageLoader

        if (!emoji.isNullOrBlank()) try {
            val request = ImageRequest.Builder(context)
                .data(Emojifier.safeUri(emoji))
                .target(target)
                .build()
            imageLoader.enqueue(request)
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading emoji: $emoji for mention span")
        }

        if (!image.isNullOrBlank()) {
            val request = ImageRequest.Builder(context)
                .data(image)
                .target(target)
                .build()
            imageLoader.enqueue(request)
        }

        if (!profile.isNullOrBlank()) {
            val request = ImageRequest.Builder(context)
                .data(profile)
                .transformations(CircleCropTransformation())
                .target(target)
                .build()
            imageLoader.enqueue(request)
        }
    }

    override fun getDrawable(): Drawable? = icon ?: placeholder

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

        // Add imageSize and imagePadding only if icon or placeholder is not null
        val additionalSize = if (icon != null || placeholder != null) {
            imageSize + imagePadding
        } else {
            0
        }

        return additionalSize + (rect.right - rect.left) + endPaddingPx
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

        drawable?.let {
            val fontHeight = paintFontMetrics.descent - paintFontMetrics.ascent
            val centerY = y + paintFontMetrics.descent - fontHeight / 2
            canvas.save()
            canvas.translate(x, centerY - imageSize / 2)
            it.draw(canvas)
            canvas.restore()
        }

        if (initials != null) {
            val textColor = paint.color
            val textAlign = paint.textAlign
            val textSize = paint.textSize
            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.WHITE
            paint.textSize = initialsTextSize

            canvas.drawText(
                initials,
                x + (imageSize / 2),
                y.toFloat() - (paintFontMetrics.descent / 2),
                paint
            )
            paint.color = textColor
            paint.textAlign = textAlign
            paint.textSize = textSize
        }

        if (isDeleted) {
            paint.color = textColorDeleted
        } else {
            paint.flags = Paint.UNDERLINE_TEXT_FLAG
        }

        (paint as? TextPaint)?.let {
            if (isArchived) {
                it.color = textColorArchive
            }
        }

        // Adjust starting position for text drawing based on presence of icon or placeholder
        val textStartX = if (icon != null || placeholder != null) {
            x + imageSize + imagePadding
        } else {
            x
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.isAntiAlias = true
        canvas.drawText(
            text.substring(start, end),
            textStartX,
            y.toFloat(),
            paint
        )
    }

    private fun getCachedDrawable(): Drawable? {
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
        setBounds(0, 0, imageSize, imageSize)
    }