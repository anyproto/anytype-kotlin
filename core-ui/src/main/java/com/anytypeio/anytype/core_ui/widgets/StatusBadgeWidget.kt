package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sync.SyncStatusView

class StatusBadgeWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        setBackgroundResource(R.drawable.circle_solid_default)
        tint(color = context.color(R.color.palette_dark_grey))
    }

    fun bind(status: SyncStatusView?) {
        when (status) {
            SyncStatusView.Failed,
            SyncStatusView.IncompatibleVersion -> {
                visible()
                tint(color = context.color(R.color.palette_system_red))
            }
            SyncStatusView.Syncing -> {
                visible()
                tint(color = context.color(R.color.palette_system_amber_100))
            }
            SyncStatusView.Unknown, SyncStatusView.Offline, null -> {
                gone()
            }
            SyncStatusView.Synced.LocalOnly -> {
                gone()
            }
            SyncStatusView.Synced.AnyNetwork,
            SyncStatusView.Synced.SelfHostedNetwork,
            SyncStatusView.Synced.StagingNetwork -> {
                visible()
                tint(color = context.color(R.color.palette_system_green))
            }
        }
    }
}