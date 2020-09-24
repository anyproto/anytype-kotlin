package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import kotlinx.android.synthetic.main.widget_archive_toolbar.view.*

class ArchiveToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_archive_toolbar, this)
    }

    fun update(count: Int) {
        if (count == 0) {
            tvSelect.text = resources.getString(R.string.widget_archive_select_pages)
            btnPutBack.setTextColor(context.color(R.color.toolbar_archive_button_disable))
        } else {
            tvSelect.text = resources.getQuantityString(R.plurals.page_selected, count, count)
            btnPutBack.setTextColor(context.color(R.color.black))
        }
    }
}