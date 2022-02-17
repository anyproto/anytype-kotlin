package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetAvatarBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.bumptech.glide.Glide

class AvatarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    val binding = WidgetAvatarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        setupAttributeValues(attrs)
    }

    private fun setupAttributeValues(
        set: AttributeSet?
    ) {
        set?.let {
            val attrs = context.obtainStyledAttributes(it, R.styleable.AvatarWidget, 0, 0)

            val textSize = attrs.getDimensionPixelSize(R.styleable.AvatarWidget_text_size, 0)

            if (textSize > 0) binding.initials.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                textSize.toFloat()
            )

            attrs.recycle()
        }
    }


    fun bind(name: String, color: Int? = null) {
        binding.initials.visible()
        binding.initials.text =
            if (name.isNotEmpty()) name.first().toUpperCase().toString() else name
        binding.icon.invisible()
        backgroundTintList = ColorStateList.valueOf(color ?: randomColor(name))
    }

    fun setInitialColor(color: Int) {
        binding.initials.setTextColor(color)
    }

    private fun randomColor(name: String): Int {
        var hash = 0

        for (i in name.indices) {
            hash = name[i].toInt() + ((hash.shl(5) - hash))
        }

        val h = (hash % 360).toFloat()

        return Color.HSVToColor(floatArrayOf(h, 0.3f, 0.8f))
    }

    fun icon(url: String) {
        Glide
            .with(binding.icon)
            .load(url)
            .centerInside()
            .circleCrop()
            .into(binding.icon)

        binding.icon.visible()
        binding.initials.invisible()
    }
}