package com.anytypeio.anytype.feature_os_widgets.deeplink

import android.content.Intent
import android.net.Uri

/**
 * Deep link constants and builders for widget navigation.
 */
object OsWidgetDeepLinks {

    private const val SCHEME = "anytype"
    private const val HOST = "os-widget"

    // Widget types
    private const val WIDGET_SPACES_LIST = "spaces-list"

    // Actions
    private const val ACTION_OPEN_SPACE = "open-space"

    /**
     * Creates a deep link URI to open a specific space from the spaces list widget.
     * Format: anytype://os-widget/spaces-list/open-space/{spaceId}
     */
    fun buildSpaceDeepLink(spaceId: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendPath(WIDGET_SPACES_LIST)
            .appendPath(ACTION_OPEN_SPACE)
            .appendPath(spaceId)
            .build()
    }

    /**
     * Creates an intent to open a specific space via deep link.
     */
    fun buildSpaceIntent(spaceId: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = buildSpaceDeepLink(spaceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
