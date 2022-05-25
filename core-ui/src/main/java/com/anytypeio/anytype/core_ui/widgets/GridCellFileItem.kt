package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetDvGridFileBinding
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible

class GridCellFileItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetDvGridFileBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setup(name: String, mime: String?) = with(binding) {
        tvName.visible()
        tvName.text = name
        if (mime != null) {
            ivIcon.visible()
            val mimeIcon = mime.getMimeIcon(name)
            ivIcon.setImageResource(mimeIcon)
        } else {
            ivIcon.gone()
        }
    }
}