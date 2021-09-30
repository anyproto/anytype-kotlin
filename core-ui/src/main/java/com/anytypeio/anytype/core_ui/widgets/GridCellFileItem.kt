package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import kotlinx.android.synthetic.main.widget_dv_grid_file.view.*

class GridCellFileItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_dv_grid_file, this)
    }

    fun setup(name: String, mime: String?) {
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