package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.android.synthetic.main.widget_dv_grid_object.view.objectIcon
import kotlinx.android.synthetic.main.widget_dv_grid_object.view.tvName
import kotlinx.android.synthetic.main.widget_dv_list_view_relation_object.view.*

class ListViewRelationObjectValueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_dv_list_view_relation_object, this)
    }

    fun setup(name: String, icon: ObjectIcon, size: Int) {
        tvName.visible()
        tvName.text = name
        objectIcon.setIcon(icon)
        if (size > 1) {
            tvCount.visible()
            tvCount.text = "+${size - 1}"
        } else {
            tvCount.gone()
        }
    }

    fun setupAsNonExistent(size: Int) {
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