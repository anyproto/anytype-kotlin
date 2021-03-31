package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.presentation.page.editor.ThemeColor

class StatusWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    fun setColor(color: String?) {
        val themeColor = ThemeColor.values().find { it.title == color }
        if (themeColor != null) {
            setTextColor(themeColor.text)
        } else {
            setTextColor(context.color(R.color.default_filter_tag_text_color))
        }
    }
}