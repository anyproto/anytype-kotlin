package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectIconTextBinding
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class ObjectIconTextWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = WidgetObjectIconTextBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        setupAttributeValues(attrs)
    }

    private fun setupAttributeValues(set: AttributeSet?) {
        if (set == null) return

        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectIconTextWidget, 0, 0)

        val nameTextSize = attrs.getDimensionPixelSize(R.styleable.ObjectIconTextWidget_nameTextSize, 0)
        if (nameTextSize > 0) {
            binding.objectName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameTextSize.toFloat())
        }

        val nameTextColor = attrs.getColor(R.styleable.ObjectIconTextWidget_nameTextColor, 0)
        binding.objectName.setTextColor(nameTextColor)

        attrs.recycle()
    }

    fun setup(name: String?, icon: ObjectIcon) {
        binding.objectName.text = name
        binding.objectIcon.setIcon(icon)
    }
}