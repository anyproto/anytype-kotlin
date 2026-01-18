package com.anytypeio.anytype.core_models.ui

import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url

/**
 * Represents the visual icon for a space in the UI.
 * Supports both chat spaces and data spaces with image or placeholder variants.
 */
sealed class SpaceIconView {
    data object Loading : SpaceIconView()

    sealed class ChatSpace : SpaceIconView() {
        data class Image(
            val url: Url,
            val color: SystemColor = SystemColor.SKY,
        ) : ChatSpace()

        data class Placeholder(
            val color: SystemColor = SystemColor.SKY,
            val name: String = ""
        ) : ChatSpace()
    }

    sealed class DataSpace : SpaceIconView() {
        data class Image(
            val url: Url,
            val color: SystemColor = SystemColor.SKY,
        ) : DataSpace()

        data class Placeholder(
            val color: SystemColor = SystemColor.SKY,
            val name: String = ""
        ) : DataSpace()
    }
}
