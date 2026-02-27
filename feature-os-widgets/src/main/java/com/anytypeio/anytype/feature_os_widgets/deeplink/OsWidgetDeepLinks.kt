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
    private const val WIDGET_CREATE_OBJECT = "create-object"
    private const val WIDGET_SPACE_SHORTCUT = "space-shortcut"
    private const val WIDGET_OBJECT_SHORTCUT = "object-shortcut"

    // Actions
    private const val ACTION_OPEN_SPACE = "open-space"
    private const val ACTION_OPEN = "open"
    private const val ACTION_CREATE = "create"

    // Query params
    private const val PARAM_SPACE_ID = "spaceId"

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

    /**
     * Creates a deep link URI to create an object using a configured widget.
     * Format: anytype://os-widget/create-object/create/{appWidgetId}
     */
    fun buildCreateObjectDeepLink(appWidgetId: Int): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendPath(WIDGET_CREATE_OBJECT)
            .appendPath(ACTION_CREATE)
            .appendPath(appWidgetId.toString())
            .build()
    }

    /**
     * Creates an intent to create an object via deep link.
     */
    fun buildCreateObjectIntent(appWidgetId: Int): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = buildCreateObjectDeepLink(appWidgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    // ==================== Space Shortcut Widget ====================

    /**
     * Creates a deep link URI to open a space from a space shortcut widget.
     * Format: anytype://os-widget/space-shortcut/open/{spaceId}
     */
    fun buildSpaceShortcutDeepLink(spaceId: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendPath(WIDGET_SPACE_SHORTCUT)
            .appendPath(ACTION_OPEN)
            .appendPath(spaceId)
            .build()
    }

    /**
     * Creates an intent to open a space from a space shortcut widget.
     */
    fun buildSpaceShortcutIntent(spaceId: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = buildSpaceShortcutDeepLink(spaceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    // ==================== Object Shortcut Widget ====================

    /**
     * Creates a deep link URI to open an object from an object shortcut widget.
     * Format: anytype://os-widget/object-shortcut/open/{objectId}?spaceId={spaceId}
     */
    fun buildObjectShortcutDeepLink(objectId: String, spaceId: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendPath(WIDGET_OBJECT_SHORTCUT)
            .appendPath(ACTION_OPEN)
            .appendPath(objectId)
            .appendQueryParameter(PARAM_SPACE_ID, spaceId)
            .build()
    }

    /**
     * Creates an intent to open an object from an object shortcut widget.
     */
    fun buildObjectShortcutIntent(objectId: String, spaceId: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = buildObjectShortcutDeepLink(objectId, spaceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
