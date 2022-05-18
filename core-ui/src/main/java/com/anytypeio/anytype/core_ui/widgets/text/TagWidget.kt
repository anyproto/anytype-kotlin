package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor

class TagWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        val lr = context.dimen(R.dimen.dv_tag_left_right_padding).toInt()
        setPadding(lr, 0, lr, 0)
        setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
        maxLines = 1
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.END
    }

    fun setup(text: String, color: String?) {
        visible()
        this.text = text
        setColor(color)
    }

    private fun setColor(color: String?) {
        val defaultTextColor = resources.getColor(R.color.text_primary, null)
        val defaultBackground = resources.getColor(R.color.shape_primary, null)
        val themeColor = ThemeColor.values().find { it.code == color }
        if (themeColor != null && themeColor != ThemeColor.DEFAULT) {
            background.setDrawableColor(resources.light(themeColor, defaultBackground))
            setTextColor(resources.dark(themeColor, defaultTextColor))
        } else {
            background.setDrawableColor(defaultBackground)
            setTextColor(defaultTextColor)
        }
    }
}