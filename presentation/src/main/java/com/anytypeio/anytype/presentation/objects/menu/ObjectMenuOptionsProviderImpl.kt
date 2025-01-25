package com.anytypeio.anytype.presentation.objects.menu

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.editor.editor.AllObjectsDetails
import com.anytypeio.anytype.presentation.editor.editor.getObject
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider.Options
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

class ObjectMenuOptionsProviderImpl(
    private val allObjectSDetailsFlow: Flow<AllObjectsDetails>,
    private val restrictions: Flow<List<ObjectRestriction>>
) : ObjectMenuOptionsProvider {

    private fun observeLayout(ctx: Id): Flow<ObjectType.Layout?> = allObjectSDetailsFlow
        .filter { details ->
            details.details.containsKey(ctx).also { isValuePresent ->
                if (!isValuePresent) Timber.w("Details missing for object: $ctx")
            }
        }
        .mapNotNull { details ->
            details.getObject(ctx)?.layout
        }

    override fun provide(ctx: Id, isLocked: Boolean, isReadOnly: Boolean): Flow<Options> {
        return combine(observeLayout(ctx), restrictions) { layout, restrictions ->
            createOptions(
                layout = layout,
                restrictions = restrictions,
                isLocked = isLocked,
                isReadOnly = isReadOnly
            )
        }
    }

    private fun createOptions(
        layout: ObjectType.Layout?,
        restrictions: List<ObjectRestriction>,
        isLocked: Boolean,
        isReadOnly: Boolean
    ): Options {
        val hasIcon = !isLocked && !isReadOnly
        val hasCover = !isLocked && !isReadOnly
        val hasLayout = !isLocked && !restrictions.contains(ObjectRestriction.LAYOUT_CHANGE) && !isReadOnly
        val options = if (layout != null) {
            when (layout) {
                ObjectType.Layout.PARTICIPANT -> Options.ALL.copy(
                    hasIcon = false,
                    hasCover = false,
                    hasLayout = false,
                    hasDiagnosticsVisibility = true,
                    hasHistory = false,
                    hasRelations = false
                )
                in SupportedLayouts.systemLayouts -> Options.NONE
                in SupportedLayouts.fileLayouts -> {
                    Options.ALL.copy(
                        hasIcon = false,
                        hasCover = false,
                        hasLayout = false,
                        hasDiagnosticsVisibility = true,
                        hasHistory = false
                    )
                }
                ObjectType.Layout.SET,
                ObjectType.Layout.COLLECTION -> {
                    Options.ALL.copy(
                        hasIcon = hasIcon,
                        hasCover = hasCover,
                        hasLayout = false,
                        hasDiagnosticsVisibility = true,
                        hasHistory = !isLocked && !isReadOnly
                    )
                }
                ObjectType.Layout.BASIC,
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.BOOKMARK -> Options.ALL.copy(
                    hasIcon = hasIcon,
                    hasCover = hasCover,
                    hasLayout = hasLayout,
                    hasDiagnosticsVisibility = true,
                    hasHistory = !isLocked && !isReadOnly
                )
                ObjectType.Layout.TODO -> Options(
                    hasIcon = false,
                    hasCover = hasCover,
                    hasLayout = hasLayout,
                    hasRelations = true,
                    hasDiagnosticsVisibility = true,
                    hasHistory = !isLocked && !isReadOnly
                )

                ObjectType.Layout.NOTE -> Options(
                    hasIcon = false,
                    hasCover = false,
                    hasLayout = hasLayout,
                    hasRelations = true,
                    hasDiagnosticsVisibility = true,
                    hasHistory = !isLocked && !isReadOnly
                )
                else -> Options.NONE.copy(
                    hasDiagnosticsVisibility = true
                )
            }
        } else {
            // unknown layout
            Options.NONE.copy(
                hasDiagnosticsVisibility = true
            )
        }
        return options
    }
}