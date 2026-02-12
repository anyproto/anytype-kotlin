package com.anytypeio.anytype.feature_os_widgets.model

import com.anytypeio.anytype.core_models.SystemColor

/**
 * Represents the icon for a space in the OS widget.
 * Mirrors [com.anytypeio.anytype.core_models.ui.SpaceIconView] but simplified for widget use.
 */
sealed class OsWidgetSpaceIcon {

    /**
     * Space has a custom image icon.
     * @param hash The image hash to build URL via UrlBuilder
     * @param color Fallback/background color
     */
    data class Image(
        val hash: String,
        val color: SystemColor
    ) : OsWidgetSpaceIcon()

    /**
     * Space uses a placeholder icon (gradient with initial).
     * @param color The gradient color from SystemColor
     * @param name The space name (first letter used as initial)
     */
    data class Placeholder(
        val color: SystemColor,
        val name: String
    ) : OsWidgetSpaceIcon()
}
