package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.StatusBadgeWidget
import kotlinx.android.synthetic.main.widget_object_top_toolbar.view.*

class ObjectTopToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val status: StatusBadgeWidget get() = statusBadge
    val statusText: TextView get() = tvStatus
    val menu: View get() = threeDotsButton
    val container: ViewGroup get() = titleContainer
    val title: TextView get() = tvTopToolbarTitle
    val image: ImageView get() = ivTopToolbarIcon

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_object_top_toolbar, this)
        container.alpha = 0f
    }

    fun setStyle(
        overCover: Boolean
    ) {
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
        statusContainer.alpha = 0f
    }

    fun showStatusContainer() {
        statusContainer.animate().alpha(1f).setDuration(300).start()
    }
}