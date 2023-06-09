package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.tint

class StatusBadgeWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        setBackgroundResource(R.drawable.circle_solid_default)
        tint(Color.WHITE)
    }

    fun bind(status: SyncStatus?) {
        when (status) {
            SyncStatus.UNKNOWN,
            SyncStatus.FAILED,
            SyncStatus.OFFLINE,
            SyncStatus.INCOMPATIBLE_VERSION -> tint(
                color = context.color(R.color.sync_status_red)
            )
            SyncStatus.SYNCING -> tint(
                color = context.color(R.color.sync_status_orange)
            )
            SyncStatus.SYNCED -> tint(
                color = context.color(R.color.sync_status_green)
            )
            else -> tint(Color.TRANSPARENT)
        }
    }
}