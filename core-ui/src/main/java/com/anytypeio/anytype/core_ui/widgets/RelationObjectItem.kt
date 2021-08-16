package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.android.synthetic.main.widget_dv_grid_object.view.*

class RelationObjectItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_relation_object, this)
    }

    fun setup(name: String, icon: ObjectIcon) {
        tvName.visible()
        tvName.text = name
        objectIcon.setIcon(icon)
    }
}