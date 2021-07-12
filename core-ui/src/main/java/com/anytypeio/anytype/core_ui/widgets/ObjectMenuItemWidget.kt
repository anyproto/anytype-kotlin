package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import kotlinx.android.synthetic.main.widget_object_menu_item.view.*

class ObjectMenuItemWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_object_menu_item, this)
        orientation = HORIZONTAL
        setupAttributeValues(attrs)
    }

    private fun setupAttributeValues(set: AttributeSet?) {
        if (set == null) return
        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectMenuItemWidget, 0, 0)
        tvTitle.text = attrs.getString(R.styleable.ObjectMenuItemWidget_title)
        tvSubtitle.text = attrs.getString(R.styleable.ObjectMenuItemWidget_subtitle)
        ivIcon.setImageResource(attrs.getResourceId(R.styleable.ObjectMenuItemWidget_icon, -1))
        attrs.recycle()
    }

}