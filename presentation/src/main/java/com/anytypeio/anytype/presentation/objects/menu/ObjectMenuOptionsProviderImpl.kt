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
import kotlinx.coroutines.flow.map
import timber.log.Timber

class ObjectMenuOptionsProviderImpl(
    private val objectViewDetailsFlow: Flow<ObjectViewDetails>,
    private val hasObjectLayoutConflict: Flow<Boolean>
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

    override fun provide(ctx: Id, isLocked: Boolean, isReadOnly: Boolean): Flow<Options> {
        return combine(
            observeLayout(ctx),
            observeFeatureFieldsContainsDescription(ctx),
            observeHasObjectLayoutConflict(),
            observeTemplateNamePrefillEnabled(ctx)
        ) { layout, featuredContainsDescription, hasObjectLayoutConflict, isTemplateNamePrefillEnabled ->
            createOptions(
                layout = layout,
                isLocked = isLocked,
                isReadOnly = isReadOnly,
                featuredContainsDescription = featuredContainsDescription,
                hasObjectLayoutConflict = hasObjectLayoutConflict,
                isTemplateNamePrefillEnabled = isTemplateNamePrefillEnabled
            )
        }
    }

    private fun createOptions(
        layout: ObjectType.Layout?,
        isLocked: Boolean,
        isReadOnly: Boolean,
        featuredContainsDescription: Boolean,
        hasObjectLayoutConflict: Boolean,
        isTemplateNamePrefillEnabled: Boolean
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
        return options
    }
}