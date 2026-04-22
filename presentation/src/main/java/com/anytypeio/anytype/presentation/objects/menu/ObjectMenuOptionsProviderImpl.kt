package com.anytypeio.anytype.presentation.objects.menu

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider.Options
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * @param personalFavoriteTargets emits the set of object IDs currently in the user's
 *  personal favorites for the active space. Used to flip the Favorite ↔ Unfavorite
 *  menu item. Defaults to [emptySet] when the caller hasn't wired personal favorites
 *  (e.g. legacy callers not yet updated for DROID-4397).
 * @param canToggleChannelPin emits true iff the current user has Owner/Admin role in
 *  the active space. Gates visibility of the Pin/Unpin menu items. Defaults to false.
 */
class ObjectMenuOptionsProviderImpl(
    private val objectViewDetailsFlow: Flow<ObjectViewDetails>,
    private val hasObjectLayoutConflict: Flow<Boolean>,
    private val personalFavoriteTargets: Flow<Set<Id>> = flowOf(emptySet()),
    private val canToggleChannelPin: Flow<Boolean> = flowOf(false)
) : ObjectMenuOptionsProvider {

    private fun observeLayout(ctx: Id): Flow<ObjectType.Layout?> = objectViewDetailsFlow
        .filter { details ->
            details.details.containsKey(ctx).also { isValuePresent ->
                if (!isValuePresent) Timber.w("Details missing for object: $ctx")
            }
        }
        .map { details ->
            details.getObject(ctx)?.layout
        }

    private fun observeFeatureFieldsContainsDescription(ctx: Id): Flow<Boolean> =
        objectViewDetailsFlow
            .filter { details ->
                details.details.containsKey(ctx).also { isValuePresent ->
                    if (!isValuePresent) Timber.w("Details missing for object: $ctx")
                }
            }
            .map { details ->
                val featuredRelations = details.getObject(ctx)?.featuredRelations
                return@map featuredRelations?.any { it == Relations.DESCRIPTION } == true
            }

    private fun observeHasObjectLayoutConflict(): Flow<Boolean> = hasObjectLayoutConflict

    private fun observeTemplateNamePrefillEnabled(ctx: Id): Flow<Boolean> =
        objectViewDetailsFlow
            .filter { details ->
                details.details.containsKey(ctx)
            }
            .map { details ->
                details.getObject(ctx)?.isNamePrefillEnabled ?: false
            }

    private fun observeFavoritesPinState(ctx: Id): Flow<FavoritesPinState> = combine(
        personalFavoriteTargets,
        canToggleChannelPin
    ) { favorites, canPin ->
        FavoritesPinState(
            isFavorited = ctx in favorites,
            canToggleChannelPin = canPin
        )
    }

    override fun provide(ctx: Id, isLocked: Boolean, isReadOnly: Boolean): Flow<Options> {
        return combine(
            observeLayout(ctx),
            observeFeatureFieldsContainsDescription(ctx),
            observeHasObjectLayoutConflict(),
            observeTemplateNamePrefillEnabled(ctx),
            observeFavoritesPinState(ctx)
        ) { layout, featuredContainsDescription, hasObjectLayoutConflict, isTemplateNamePrefillEnabled, favPin ->
            createOptions(
                layout = layout,
                isLocked = isLocked,
                isReadOnly = isReadOnly,
                featuredContainsDescription = featuredContainsDescription,
                hasObjectLayoutConflict = hasObjectLayoutConflict,
                isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled,
                favPin = favPin
            )
        }
    }

    private data class FavoritesPinState(
        val isFavorited: Boolean,
        val canToggleChannelPin: Boolean
    )

    private fun createOptions(
        layout: ObjectType.Layout?,
        isLocked: Boolean,
        isReadOnly: Boolean,
        featuredContainsDescription: Boolean,
        hasObjectLayoutConflict: Boolean,
        isTemplateNamePrefillEnabled: Boolean,
        favPin: FavoritesPinState
    ): Options {
        val hasIcon = !isLocked && !isReadOnly
        val hasCover = !isLocked && !isReadOnly
        val options = if (layout != null) {
            when (layout) {
                ObjectType.Layout.PARTICIPANT -> Options.ALL.copy(
                    hasIcon = false,
                    hasCover = false,
                    hasDiagnosticsVisibility = true,
                    hasHistory = false,
                    hasRelations = false,
                    hasDescriptionShow = !featuredContainsDescription,
                    hasObjectLayoutConflict = hasObjectLayoutConflict,
                    hasTemplateNamePrefill = false,
                    isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
                )

                in SupportedLayouts.systemLayouts -> Options.NONE
                in SupportedLayouts.fileLayouts -> {
                    Options.ALL.copy(
                        hasIcon = false,
                        hasCover = false,
                        hasDiagnosticsVisibility = true,
                        hasHistory = false,
                        hasDescriptionShow = !featuredContainsDescription,
                        hasObjectLayoutConflict = hasObjectLayoutConflict,
                        hasTemplateNamePrefill = false,
                        isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
                    )
                }

                ObjectType.Layout.SET,
                ObjectType.Layout.COLLECTION -> {
                    Options.ALL.copy(
                        hasIcon = hasIcon,
                        hasCover = hasCover,
                        hasDiagnosticsVisibility = true,
                        hasHistory = !isLocked && !isReadOnly,
                        hasDescriptionShow = !featuredContainsDescription,
                        hasObjectLayoutConflict = hasObjectLayoutConflict,
                        hasTemplateNamePrefill = false,
                        isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
                    )
                }

                ObjectType.Layout.BASIC,
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.BOOKMARK -> Options.ALL.copy(
                    hasIcon = hasIcon,
                    hasCover = hasCover,
                    hasDiagnosticsVisibility = true,
                    hasHistory = !isLocked && !isReadOnly,
                    hasDescriptionShow = !featuredContainsDescription,
                    hasObjectLayoutConflict = hasObjectLayoutConflict,
                    hasTemplateNamePrefill = true,
                    isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
                )

                ObjectType.Layout.TODO -> Options(
                    hasIcon = false,
                    hasCover = hasCover,
                    hasRelations = true,
                    hasDiagnosticsVisibility = true,
                    hasHistory = !isLocked && !isReadOnly,
                    hasDescriptionShow = !featuredContainsDescription,
                    hasObjectLayoutConflict = hasObjectLayoutConflict,
                    hasTemplateNamePrefill = true,
                    isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
                )

                ObjectType.Layout.NOTE -> Options(
                    hasIcon = false,
                    hasCover = false,
                    hasRelations = true,
                    hasDiagnosticsVisibility = true,
                    hasHistory = !isLocked && !isReadOnly,
                    hasDescriptionShow = !featuredContainsDescription,
                    hasObjectLayoutConflict = hasObjectLayoutConflict,
                    hasTemplateNamePrefill = false, // Notes don't have titles
                    isTemplateNamePrefillEnabled = false
                )

                else -> Options.NONE.copy(
                    hasDiagnosticsVisibility = true,
                    hasObjectLayoutConflict = hasObjectLayoutConflict,
                    hasTemplateNamePrefill = false,
                    isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
                )
            }
        } else {
            // unknown layout
            Options.NONE.copy(
                hasDiagnosticsVisibility = true,
                hasObjectLayoutConflict = hasObjectLayoutConflict,
                hasTemplateNamePrefill = false,
                isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
            )
        }
        return options.copy(
            isFavorited = favPin.isFavorited,
            canToggleChannelPin = favPin.canToggleChannelPin
        )
    }
}