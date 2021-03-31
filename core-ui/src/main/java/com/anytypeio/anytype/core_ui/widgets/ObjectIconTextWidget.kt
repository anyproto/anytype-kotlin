package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import kotlinx.android.synthetic.main.widget_object_icon_text.view.*

class ObjectIconTextWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context)
        setupAttributeValues(attrs)
    }

    internal fun inflate(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.widget_object_icon_text, this)
    }

    private fun setupAttributeValues(set: AttributeSet?) {
        if (set == null) return

        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectIconTextWidget, 0, 0)

        val nameTextSize = attrs.getDimensionPixelSize(R.styleable.ObjectIconTextWidget_nameTextSize, 0)
        if (nameTextSize > 0) {
            objectName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameTextSize.toFloat())
        }

        val nameTextColor = attrs.getColor(R.styleable.ObjectIconTextWidget_nameTextColor, 0)
        objectName.setTextColor(nameTextColor)

        attrs.recycle()
    }

    fun setup(name: String, emoji: String?, image: String?) {
        objectName.text = name
        objectIcon.setIcon(
            emoji = emoji,
            image = image,
            name = name
        )
    }
}