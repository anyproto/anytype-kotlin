package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.DsButtonNumberBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible

class ButtonPrimaryNumber @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = DsButtonNumberBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ButtonPrimaryNumber).use {
            val text = it.getString(R.styleable.ButtonPrimaryNumber_buttonTitle)
            binding.button.text = text
        }
    }

    fun setNumber(num: String) {
        binding.tvNumber.text = num
    }

    fun hideNumber() {
        binding.tvNumber.invisible()
    }

    fun showNumber() {
        binding.tvNumber.visible()
    }
}