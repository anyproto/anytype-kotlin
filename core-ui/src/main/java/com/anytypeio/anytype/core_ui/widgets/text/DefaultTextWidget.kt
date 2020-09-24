package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Canvas
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import com.anytypeio.anytype.core_ui.widgets.text.highlight.HighlightAttributeReader
import com.anytypeio.anytype.core_ui.widgets.text.highlight.HighlightDrawer

class DefaultTextWidget : AppCompatTextView {

    private var highlightDrawer: HighlightDrawer? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupHighlightDrawer(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        setupHighlightDrawer(context, attrs)
    }

    private fun setupHighlightDrawer(context: Context, attrs: AttributeSet) {
        HighlightAttributeReader(context, attrs).let { reader ->
            highlightDrawer = HighlightDrawer(
                horizontalPadding = reader.horizontalPadding,
                verticalPadding = reader.verticalPadding,
                drawable = reader.drawable,
                drawableLeft = reader.drawableLeft,
                drawableMid = reader.drawableMid,
                drawableRight = reader.drawableRight
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                highlightDrawer?.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }
}