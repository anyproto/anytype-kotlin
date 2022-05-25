package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StaticTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var textToSet: String = ""
    var xTextPosition: Float = 0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //todo Fix y position after proper design
        canvas?.drawText(textToSet, xTextPosition, 45f, paint)
    }
}