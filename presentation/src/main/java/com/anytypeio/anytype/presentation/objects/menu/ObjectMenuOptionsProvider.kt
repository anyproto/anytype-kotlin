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
        val isTemplateNamePrefillEnabled: Boolean = false
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