package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectIconTextBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class ObjectIconTextWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = WidgetObjectIconTextBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        setupAttributeValues(attrs)
    }

    private fun setupAttributeValues(set: AttributeSet?) {

        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectIconTextWidget, 0, 0)

        attrs.recycle()
    }

    fun setup(
        name: String?,
        icon: ObjectIcon = ObjectIcon.None
    ) {
        binding.objectName.text = name
        binding.objectIcon.setIcon(icon)
        if (icon is ObjectIcon.None) {
            binding.objectIcon.gone()
        }
    }
}