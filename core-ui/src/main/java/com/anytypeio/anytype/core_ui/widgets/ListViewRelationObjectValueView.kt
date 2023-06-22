package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetDvListViewRelationObjectBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class ListViewRelationObjectValueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetDvListViewRelationObjectBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setup(name: String, icon: ObjectIcon, size: Int) = with(binding) {
        tvName.visible()
        tvName.text = name
        if (icon is ObjectIcon.None) {
            objectIcon.gone()
        } else {
            objectIcon.visible()
            objectIcon.setIcon(icon)
        }
        if (size > 1) {
            tvCount.visible()
            tvCount.text = "+${size - 1}"
        } else {
            tvCount.gone()
        }
    }

    fun setupAsNonExistent(size: Int) = with(binding) {
        tvName.visible()
        tvName.setText(R.string.non_existent_object)
        tvName.setTextColor(Color.parseColor("#CBC9BD"))
        objectIcon.setNonExistentIcon()
        if (size > 1) {
            tvCount.visible()
            tvCount.text = "+${size - 1}"
        } else {
            tvCount.gone()
        }
    }
}