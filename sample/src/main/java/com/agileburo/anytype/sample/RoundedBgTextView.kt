package com.agileburo.anytype.sample

import android.content.Context
import android.graphics.Canvas
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import com.agileburo.anytype.core_ui.widgets.text.highlight.HighlightAttributeReader
import com.agileburo.anytype.core_ui.widgets.text.highlight.HighlightDrawer

class RoundedBgTextView : AppCompatTextView {

    private val highlightDrawer: HighlightDrawer

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.textViewStyle
    ) : super(context, attrs, defStyleAttr) {
        val attributeReader =
            HighlightAttributeReader(
                context,
                attrs
            )
        highlightDrawer =
            HighlightDrawer(
                horizontalPadding = attributeReader.horizontalPadding,
                verticalPadding = attributeReader.verticalPadding,
                drawable = attributeReader.drawable,
                drawableLeft = attributeReader.drawableLeft,
                drawableMid = attributeReader.drawableMid,
                drawableRight = attributeReader.drawableRight
            )
    }

    override fun onDraw(canvas: Canvas) {
        // need to draw bg first so that text can be on top during super.onDraw()
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                highlightDrawer.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }
}