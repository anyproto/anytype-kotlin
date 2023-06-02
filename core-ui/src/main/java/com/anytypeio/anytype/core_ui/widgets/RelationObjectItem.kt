package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetRelationObjectBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class RelationObjectItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = WidgetRelationObjectBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setup(name: String, icon: ObjectIcon) = with(binding) {
        tvName.visible()
        tvName.text = name
        when (icon) {
            ObjectIcon.None -> objectIcon.gone()
            else -> {
                objectIcon.visible()
                objectIcon.setIcon(icon)
            }
        }
    }

    fun setupAsNonExistent() = with(binding) {
        tvName.visible()
        tvName.setText(R.string.non_existent_object)
        tvName.setTextColor(context.color(R.color.text_tertiary))
        objectIcon.setNonExistentIcon()
    }
}