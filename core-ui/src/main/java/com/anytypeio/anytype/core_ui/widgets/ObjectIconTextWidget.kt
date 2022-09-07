package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
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

        val nameTextSize = attrs.getDimensionPixelSize(R.styleable.ObjectIconTextWidget_nameTextSize, 0)
        if (nameTextSize > 0) {
            setTextSize(nameTextSize.toFloat())
        }

        val nameTextColor = attrs.getColor(R.styleable.ObjectIconTextWidget_nameTextColor, 0)
        setTextColor(nameTextColor)

        attrs.recycle()
    }

    fun setTextSize(textSize: Float) {
        binding.objectName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    fun setTextColor(textColor: Int) {
        binding.objectName.setTextColor(textColor)
    }

    fun setup(name: String?, icon: ObjectIcon) {
        binding.objectName.text = name
        binding.objectIcon.setIcon(icon)
        if (icon is ObjectIcon.None) {
            binding.objectIcon.gone()
        }
    }
}