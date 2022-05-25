package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.anytypeio.anytype.core_ui.databinding.WidgetSetMarkupUrlBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class SetMarkupUrlWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetSetMarkupUrlBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        binding.clearUrlButton.setOnClickListener {
            binding.setUrlMarkupTextInput.text = null
        }
    }

    fun onApply() = binding.setMarkupDoneButton
        .clicks()
        .map { binding.setUrlMarkupTextInput.text.toString() }
        .onEach { binding.setUrlMarkupTextInput.clearFocus() }

    fun takeFocus() {
        binding.setUrlMarkupTextInput.requestFocus()
    }

    fun bind(url: String?) {
        binding.setUrlMarkupTextInput.setText(url, TextView.BufferType.EDITABLE)
        if (url != null) binding.setUrlMarkupTextInput.setSelection(url.length)
    }
}