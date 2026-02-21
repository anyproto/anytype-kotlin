package com.anytypeio.anytype.feature_os_widgets.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.anytypeio.anytype.feature_os_widgets.ui.OsSpacesListWidget

/**
 * BroadcastReceiver for the Spaces List widget.
 * This is the entry point registered in the AndroidManifest.
 */
class OsSpacesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = OsSpacesListWidget()
}
