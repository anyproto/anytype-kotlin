package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_models.ThemeColor

class StatusWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    fun setColor(color: String?) {
        val defaultTextColor = context.getColor(R.color.text_primary)
        val themeColor = ThemeColor.values().find { it.code == color } ?: ThemeColor.DEFAULT
        setTextColor(resources.dark(themeColor, defaultTextColor))
    }
}