package com.anytypeio.anytype.core_ui.widgets.text.highlight

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Annotation
import android.text.Layout
import android.text.Spanned
import androidx.core.graphics.drawable.DrawableCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import timber.log.Timber

/**
 * Helper class to draw multi-line rounded background to certain parts of a text. The start/end
 * positions of the backgrounds are annotated with [android.text.Annotation] class. Each annotation
 * should have the annotation key set to **rounded**.
 *
 * i.e.:
 * ```
 *    <!--without the quotes at the begining and end Android strips the whitespace and also starts
 *        the annotation at the wrong position-->
 *    <string name="ltr">"this is <annotation key="rounded">a regular</annotation> paragraph."</string>
 * ```
 *
 * **Note:** BiDi text is not supported.
 *
 * @param horizontalPadding the padding to be applied to left & right of the background
 * @param verticalPadding the padding to be applied to top & bottom of the background
 * @param drawable the drawable used to draw the background
 * @param drawableLeft the drawable used to draw left edge of the background
 * @param drawableMid the drawable used to draw for whole line
 * @param drawableRight the drawable used to draw right edge of the background
 */

class HighlightDrawer(
    val horizontalPadding: Int,
    verticalPadding: Int,
    drawable: Drawable,
    drawableLeft: Drawable,
    val drawableMid: Drawable,
    drawableRight: Drawable
) {

    private val defaultSingleLineRenderer: TextRoundedBgRenderer by lazy {
        SingleLineRenderer(
            horizontalPadding = 0,
            verticalPadding = verticalPadding,
            drawable = drawableMid
        )
    }

    private val defaultMultiLineRenderer: TextRoundedBgRenderer by lazy {
        MultiLineRenderer(
            horizontalPadding = 0,
            verticalPadding = verticalPadding,
            drawableLeft = drawableMid,
            drawableMid = drawableMid,
            drawableRight = drawableMid
        )
    }

    private val singleLineHighlightCodeRenderer: TextRoundedBgRenderer by lazy {
        SingleLineRenderer(
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            drawable = drawable
        )
    }

    private val multiLineHighlightCodeRenderer: TextRoundedBgRenderer by lazy {
        MultiLineRenderer(
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            drawableLeft = drawableLeft,
            drawableMid = drawableMid,
            drawableRight = drawableRight
        )
    }

    /**
     * Call this function during onDraw of another widget such as TextView.
     *
     * @param canvas Canvas to draw onto
     * @param text
     * @param layout Layout that contains the text
     */
    fun draw(canvas: Canvas, text: Spanned, layout: Layout, resources: Resources) {
        text.getSpans(0, text.length, Annotation::class.java).forEach { span ->
            when (span.key) {
                Span.Keyboard.KEYBOARD_KEY -> drawCodeHighlight(
                    span = span,
                    text = text,
                    layout = layout,
                    canvas = canvas
                )
                Span.Highlight.HIGHLIGHT_KEY -> drawBackgroundHighlight(
                    span = span,
                    text = text,
                    layout = layout,
                    canvas = canvas,
                    resources = resources
                )
                else -> Timber.e("Unexpected span: $span")
            }
        }
    }

    private fun drawBackgroundHighlight(
        span: Annotation,
        text: Spanned,
        layout: Layout,
        canvas: Canvas,
        resources: Resources
    ) {
        val value = ThemeColor.values().find { value -> value.code == span.value }
        val default = resources.getColor(R.color.background_primary, null)
        val color = if (value != null && value != ThemeColor.DEFAULT) {
            resources.veryLight(value, default)
        } else {
            default
        }
        DrawableCompat.wrap(drawableMid).setTint(color)

        val spanStart = text.getSpanStart(span)
        val spanEnd = text.getSpanEnd(span)
        val startLine = layout.getLineForOffset(spanStart)
        val endLine = layout.getLineForOffset(spanEnd)

        val startOffset = layout.getPrimaryHorizontal(spanStart).toInt()
        val endOffset = layout.getPrimaryHorizontal(spanEnd).toInt()

        if (startLine == endLine)
            defaultSingleLineRenderer.draw(
                canvas = canvas,
                layout = layout,
                startLine = startLine,
                endLine = endLine,
                startOffset = startOffset,
                endOffset = endOffset
            )
        else
            defaultMultiLineRenderer.draw(
                canvas = canvas,
                layout = layout,
                startLine = startLine,
                endLine = endLine,
                startOffset = startOffset,
                endOffset = endOffset
            )
    }

    private fun drawCodeHighlight(
        text: Spanned,
        span: Annotation,
        layout: Layout,
        canvas: Canvas
    ) {
        val spanStart = text.getSpanStart(span)
        val spanEnd = text.getSpanEnd(span)
        val startLine = layout.getLineForOffset(spanStart)
        val endLine = layout.getLineForOffset(spanEnd)

        // start can be on the left or on the right depending on the language direction.
        val startOffset = (layout.getPrimaryHorizontal(spanStart)
                + -1 * layout.getParagraphDirection(startLine) * horizontalPadding).toInt()
        // end can be on the left or on the right depending on the language direction.
        val endOffset = (layout.getPrimaryHorizontal(spanEnd)
                + layout.getParagraphDirection(endLine) * horizontalPadding).toInt()

        if (startLine == endLine)
            singleLineHighlightCodeRenderer.draw(
                canvas = canvas,
                layout = layout,
                startLine = startLine,
                endLine = endLine,
                startOffset = startOffset,
                endOffset = endOffset
            )
        else
            multiLineHighlightCodeRenderer.draw(
                canvas = canvas,
                layout = layout,
                startLine = startLine,
                endLine = endLine,
                startOffset = startOffset,
                endOffset = endOffset
            )
    }
}