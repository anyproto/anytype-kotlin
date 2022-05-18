package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetDvListViewRelationTagBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor

class ListViewRelationTagValueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val binding = WidgetDvListViewRelationTagBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setup(name: String, tagColor: String, size: Int) = with(binding) {
        tvName.visible()
        tvName.text = name
        if (size > 1) {
            tvCount.visible()
            tvCount.text = "+${size - 1}"
        } else {
            tvCount.gone()
        }
        val color = ThemeColor.values().find { it.code == tagColor }
        val defaultTextColor = resources.getColor(R.color.text_primary, null)
        val defaultBackground = resources.getColor(R.color.shape_primary, null)
        if (color != null) {
            tvName.setTextColor(resources.dark(color, defaultTextColor))
            tvName.background.setDrawableColor(resources.light(color, defaultBackground))
        } else {
            tvName.setTextColor(defaultTextColor)
            tvName.background.setDrawableColor(defaultBackground)
        }
    }
}