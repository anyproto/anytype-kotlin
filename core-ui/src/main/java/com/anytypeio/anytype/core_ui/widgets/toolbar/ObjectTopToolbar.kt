package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
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
}