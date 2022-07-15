package com.anytypeio.anytype.presentation.objects.menu

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider.Options
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import timber.log.Timber

class ObjectMenuOptionsProviderImpl(
    private val details: Flow<Map<Id, Block.Fields>>,
    private val restrictions: Flow<List<ObjectRestriction>>,
) : ObjectMenuOptionsProvider {

    private fun observeLayout(ctx: Id): Flow<ObjectType.Layout?> = details
        .filter { details ->
            details.containsKey(ctx).also { isValuePresent ->
                if (!isValuePresent) Timber.w("Details missing for object: $ctx")
            }
        }
        .map { details ->
            val fields = requireNotNull(details[ctx])
            ObjectWrapper.Basic(fields.map).layout
        }

    override fun provide(ctx: Id, isLocked: Boolean): Flow<Options> {
        return combine(observeLayout(ctx), restrictions) { layout, restrictions ->
            createOptions(layout, restrictions, isLocked)
        }
    }

    private fun createOptions(
        layout: ObjectType.Layout?,
        restrictions: List<ObjectRestriction>,
        isLocked: Boolean,
    ): Options {
        val hasIcon = !isLocked
        val hasCover = !isLocked
        val hasLayout = !isLocked && !restrictions.contains(ObjectRestriction.LAYOUT_CHANGE)
        val options = if (layout != null) {
            when (layout) {
                ObjectType.Layout.BASIC,
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.OBJECT_TYPE,
                ObjectType.Layout.RELATION,
                ObjectType.Layout.FILE,
                ObjectType.Layout.DASHBOARD,
                ObjectType.Layout.IMAGE,
                ObjectType.Layout.SPACE,
                ObjectType.Layout.SET,
                ObjectType.Layout.BOOKMARK,
                ObjectType.Layout.DATABASE -> Options.ALL.copy(
                    hasIcon = hasIcon,
                    hasCover = hasCover,
                    hasLayout = hasLayout,
                )
                ObjectType.Layout.TODO -> Options(
                    hasIcon = false,
                    hasCover = hasCover,
                    hasLayout = hasLayout,
                    hasRelations = true,
                )

                ObjectType.Layout.NOTE -> Options(
                    hasIcon = false,
                    hasCover = false,
                    hasLayout = hasLayout,
                    hasRelations = true,
                )
            }
        } else {
            // unknown layout show all options
            Options.ALL.copy(
                hasIcon = hasIcon,
                hasCover = hasCover,
                hasLayout = hasLayout,
            )
        }
        return options
    }
}