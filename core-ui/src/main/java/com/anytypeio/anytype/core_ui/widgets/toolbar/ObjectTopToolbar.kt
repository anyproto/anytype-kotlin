package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectTopToolbarBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_ui.widgets.StatusBadgeWidget
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible

class ObjectTopToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = WidgetObjectTopToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    val status: StatusBadgeWidget get() = binding.statusBadge
    val statusText: TextView get() = binding.tvStatus
    val statusContainer: ViewGroup get() = binding.statusContainer
    val menu: View get() = binding.threeDotsButton
    val container: ViewGroup get() = binding.titleContainer
    val title: TextView get() = binding.tvTopToolbarTitle
    val icon: ObjectIconWidget get() = binding.ivTopToolbarIcon
    val templates: TextView get() = binding.tvTemplates

    init {
        container.alpha = 0f
    }

    fun setStyle(
        overCover: Boolean
    ) = with(binding) {
        if (overCover) {
            menu.setBackgroundResource(R.drawable.rect_object_menu_button_default)
            ivThreeDots.imageTintList = ColorStateList.valueOf(Color.WHITE)
            statusContainer.setBackgroundResource(R.drawable.rect_object_menu_button_default)
            statusText.setTextColor(Color.WHITE)
        } else {
            menu.background = null
            ivThreeDots.imageTintList = null
            statusContainer.background = null
            statusText.setTextColor(context.getColor(R.color.default_status_text_color))
        }
    }

    fun hideStatusContainer() {
        binding.statusContainer.alpha = 0f
    }

    fun showStatusContainer() {
        binding.statusContainer.animate().alpha(1f).setDuration(300).start()
    }

    fun setIsLocked(isLocked: Boolean) {
        if (isLocked) {
            if (title.compoundDrawables.getOrNull(LOCKED_DRAWABLED_INDEX) == null) {
                title.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_locked_object_editor_top_toolbar, 0
                )
            }
        } else {
            if (title.compoundDrawables.getOrNull(LOCKED_DRAWABLED_INDEX) != null) {
                title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

    fun showTemplates() {
        binding.titleContainer.visibility = View.INVISIBLE
        binding.tvTemplates.visibility = View.VISIBLE
    }

    fun hideTemplates() {
        binding.titleContainer.visibility = View.VISIBLE
        binding.tvTemplates.visibility = View.INVISIBLE
    }

    fun setTemplates(count: Int) {
        binding.tvTemplates.text = context.getString(
            R.string.this_type_has_templates,
            count
        )
    }

    fun setIsReadOnly(isReadOnly: Boolean) {
        if (isReadOnly) {
            binding.ivThreeDots.invisible()
        } else {
            binding.ivThreeDots.visible()
        }
    }

    companion object {
        const val LOCKED_DRAWABLED_INDEX = 2
    }
}