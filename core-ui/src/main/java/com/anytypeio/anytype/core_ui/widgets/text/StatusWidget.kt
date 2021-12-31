package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor

class StatusWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    fun setColor(color: String?) {
        val defaultTextColor = resources.getColor(R.color.text_primary, null)
        val themeColor = ThemeColor.values().find { it.title == color }
        if (themeColor != null && themeColor != ThemeColor.DEFAULT) {
            setTextColor(resources.dark(themeColor, defaultTextColor))
        } else {
            setTextColor(defaultTextColor)
        }
    }
}