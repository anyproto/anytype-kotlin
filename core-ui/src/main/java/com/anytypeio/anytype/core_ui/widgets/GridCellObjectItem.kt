package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetDvGridObjectBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class GridCellObjectItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetDvGridObjectBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setup(name: String, icon: ObjectIcon) = with(binding) {
        tvName.visible()
        tvName.text = name
        tvName.setTextColor(context.getColor(R.color.black))
        objectIcon.setIcon(icon)
        // Do not show basic avatar in data view cell.
        if (icon is ObjectIcon.Basic.Avatar) {
            objectIcon.gone()
            tvName.updateLayoutParams<LayoutParams> {
                marginStart = 0
            }
        } else {
            objectIcon.visible()
            if (tvName.marginStart == 0) {
                tvName.updateLayoutParams<LayoutParams> {
                    marginStart = resources.getDimension(R.dimen.dp_20).toInt()
                }
            }
        }
    }

    fun setupAsNonExistent() = with(binding) {
        tvName.visible()
        tvName.setText(R.string.non_existent_object)
        tvName.setTextColor(Color.parseColor("#CBC9BD"))
        objectIcon.setNonExistentIcon()
    }
}