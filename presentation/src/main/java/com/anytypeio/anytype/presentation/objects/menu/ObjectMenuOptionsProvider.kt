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
        val hasObjectLayoutConflict: Boolean
    ) {
        companion object {
            val ALL = Options(
                hasIcon = true,
                hasCover = true,
                hasRelations = true,
                hasDiagnosticsVisibility = true,
                hasHistory = true,
                hasDescriptionShow = true,
                hasObjectLayoutConflict = false
            )
            val NONE = Options(
                hasIcon = false,
                hasCover = false,
                hasRelations = false,
                hasDiagnosticsVisibility = false,
                hasHistory = false,
                hasDescriptionShow = false,
                hasObjectLayoutConflict = false
            )
        }
    }

    fun provide(ctx: Id, isLocked: Boolean, isReadOnly: Boolean): Flow<Options>
}