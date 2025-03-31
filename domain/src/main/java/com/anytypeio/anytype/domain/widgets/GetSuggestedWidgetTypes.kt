package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetSuggestedWidgetTypes @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository,
): ResultInteractor<GetSuggestedWidgetTypes.Params, List<ObjectWrapper.Type>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<ObjectWrapper.Type> {

        // TODO DROID-3438 open widget object preview and filter out existing object types

        val types = repo.searchObjects(
            space = params.space,
            limit = 0,
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.SPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = params.space.id
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IS_HIDDEN,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IS_HIDDEN_DISCOVERY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = listOf(
                            ObjectTypeUniqueKeys.OBJECT_TYPE,
                            ObjectTypeUniqueKeys.TEMPLATE
                        )
                    )
                )
            },
            keys = listOf(
                Relations.ID,
                Relations.SPACE_ID,
                Relations.UNIQUE_KEY,
                Relations.NAME,
                Relations.IS_ARCHIVED,
                Relations.IS_DELETED,
                Relations.ICON_IMAGE,
                Relations.ICON_EMOJI,
                Relations.ICON_NAME,
                Relations.ICON_OPTION,
            ),
            offset = 0
        ).map { result ->
            ObjectWrapper.Type(result)
        }

        return types
    }

    data class Params(val space: Space)
}