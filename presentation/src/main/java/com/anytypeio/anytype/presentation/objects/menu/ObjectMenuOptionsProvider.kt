package com.anytypeio.anytype.presentation.objects.menu

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow

interface ObjectMenuOptionsProvider {

    data class Options(
        val hasIcon: Boolean,
        val hasCover: Boolean,
        val hasRelations: Boolean,
        val hasDiagnosticsVisibility: Boolean,
        val hasHistory: Boolean,
        val hasDescriptionShow: Boolean,
        val hasObjectLayoutConflict: Boolean,
        val hasTemplateNamePrefill: Boolean = false,
        val isTemplateNamePrefillEnabled: Boolean = false,
        /**
         * DROID-4397: whether the object is in the current user's personal favorites
         * for the active space. Drives the Favorite ↔ Unfavorite menu item toggle.
         */
        val isFavorited: Boolean = false,
        /**
         * DROID-4397: whether the current user has Owner/Admin role in the space.
         * Pin/Unpin menu items are hidden entirely when false. The *which* of
         * Pin vs Unpin is decided by the existing `pinnedWidgetBlockId` state
         * in `ObjectMenuViewModelBase` — we don't duplicate it here.
         */
        val canToggleChannelPin: Boolean = false
    ) {
        companion object {
            val ALL = Options(
                hasIcon = true,
                hasCover = true,
                hasRelations = true,
                hasDiagnosticsVisibility = true,
                hasHistory = true,
                hasDescriptionShow = true,
                hasObjectLayoutConflict = false,
                hasTemplateNamePrefill = false,
                isTemplateNamePrefillEnabled = false
            )
            val NONE = Options(
                hasIcon = false,
                hasCover = false,
                hasRelations = false,
                hasDiagnosticsVisibility = false,
                hasHistory = false,
                hasDescriptionShow = false,
                hasObjectLayoutConflict = false,
                hasTemplateNamePrefill = false,
                isTemplateNamePrefillEnabled = false
            )
        }
    }

    fun provide(ctx: Id, isLocked: Boolean, isReadOnly: Boolean): Flow<Options>
}