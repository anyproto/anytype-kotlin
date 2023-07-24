package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetTypeHasTemplateToolbarBinding

class TypeHasTemplateToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    val binding = WidgetTypeHasTemplateToolbarBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setText(count: Int, typeName: String) {
        binding.tvTitle.text = context.getString(
            R.string.this_type_has_templates,
            typeName,
            count
        )
    }
}