package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetRelationObjectBinding
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class RelationObjectItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = WidgetRelationObjectBinding.inflate(
        LayoutInflater.from(context), this
    )

    fun setup(name: String, icon: ObjectIcon) = with(binding) {
        tvName.visible()
        tvName.text = name
        objectIcon.setIcon(icon)
    }

    fun setupAsNonExistent() = with(binding) {
        tvName.visible()
        tvName.setText(R.string.non_existent_object)
        tvName.setTextColor(Color.parseColor("#CBC9BD"))
        objectIcon.setNonExistentIcon()
    }
}