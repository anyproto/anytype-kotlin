package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectMenuTemplateNamePrefillBinding

class ObjectMenuTemplateNamePrefillItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetObjectMenuTemplateNamePrefillBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var listener: CompoundButton.OnCheckedChangeListener? = null

    fun setChecked(isChecked: Boolean) {
        // Temporarily remove listener to avoid triggering it when setting value programmatically
        binding.switchPrefill.setOnCheckedChangeListener(null)
        binding.switchPrefill.isChecked = isChecked
        binding.switchPrefill.setOnCheckedChangeListener(listener)
    }

    fun setOnCheckedChangeListener(callback: (Boolean) -> Unit) {
        listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            callback(isChecked)
        }
        binding.switchPrefill.setOnCheckedChangeListener(listener)
    }
}
