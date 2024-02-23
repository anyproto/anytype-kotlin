package com.anytypeio.anytype.presentation.objects.menu

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow

interface ObjectMenuOptionsProvider {

    data class Options(
        val hasIcon: Boolean,
        val hasCover: Boolean,
        val hasLayout: Boolean,
        val hasRelations: Boolean,
        val hasDiagnosticsVisibility: Boolean,
    ) {
        val hasHistory: Boolean = false
        companion object {
            val ALL = Options(
                hasIcon = true,
                hasCover = true,
                hasLayout = true,
                hasRelations = true,
                hasDiagnosticsVisibility = true
            )
            val NONE = Options(
                hasIcon = false,
                hasCover = false,
                hasLayout = false,
                hasRelations = false,
                hasDiagnosticsVisibility = false
            )
        }
    }

    fun provide(ctx: Id, isLocked: Boolean): Flow<Options>
}