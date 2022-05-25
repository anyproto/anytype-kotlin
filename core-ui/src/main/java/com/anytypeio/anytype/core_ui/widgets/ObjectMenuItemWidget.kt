package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectMenuItemBinding

class ObjectMenuItemWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetObjectMenuItemBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = HORIZONTAL
        setupAttributeValues(attrs)
    }

    private fun setupAttributeValues(set: AttributeSet?) = with(binding) {
        if (set == null) return
        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectMenuItemWidget, 0, 0)
        tvTitle.text = attrs.getString(R.styleable.ObjectMenuItemWidget_title)
        tvSubtitle.text = attrs.getString(R.styleable.ObjectMenuItemWidget_subtitle)
        ivIcon.setImageResource(attrs.getResourceId(R.styleable.ObjectMenuItemWidget_icon, -1))
        attrs.recycle()
    }
}