package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetArchiveToolbarBinding
import com.anytypeio.anytype.core_ui.extensions.color

class ArchiveToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = WidgetArchiveToolbarBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun update(count: Int) = with(binding) {
        tvSelect.text = resources.getQuantityString(R.plurals.page_selected, count, count)
        val color = if (count == 0) {
            R.color.toolbar_archive_button_disable
        } else {
            R.color.black
        }
        btnRestore.setTextColor(context.color(color))
    }
}