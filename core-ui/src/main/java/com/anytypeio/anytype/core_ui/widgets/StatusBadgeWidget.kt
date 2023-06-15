package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible

class StatusBadgeWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        setBackgroundResource(R.drawable.circle_solid_default)
        tint(color = context.color(R.color.palette_dark_grey))
    }

    fun bind(status: SyncStatus?) {
        when (status) {
            SyncStatus.FAILED,
            SyncStatus.INCOMPATIBLE_VERSION -> {
                visible()
                tint(color = context.color(R.color.palette_system_red))
            }
            SyncStatus.SYNCING -> {
                visible()
                tint(color = context.color(R.color.palette_system_amber_100))
            }
            SyncStatus.SYNCED -> {
                visible()
                tint(color = context.color(R.color.palette_system_green))
            }
            SyncStatus.UNKNOWN,
            SyncStatus.OFFLINE,
            null -> {
                gone()
            }
        }
    }
}