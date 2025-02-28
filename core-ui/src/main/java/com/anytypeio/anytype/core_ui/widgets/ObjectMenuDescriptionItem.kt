package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectMenuDescriptionBinding
import com.anytypeio.anytype.core_ui.R

class ObjectMenuDescriptionItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {


    val binding = WidgetObjectMenuDescriptionBinding.inflate(
        LayoutInflater.from(context), this
    )

    fun setAction(setAsHide: Boolean) {
        binding.descriptionAction.text =
            if (setAsHide) context.getString(R.string.modal_hide) else context.getString(R.string.modal_show)
    }
}