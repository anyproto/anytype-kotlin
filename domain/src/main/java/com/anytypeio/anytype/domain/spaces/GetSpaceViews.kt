package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetSpaceViews @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<Unit, List<ObjectWrapper.SpaceView>>(dispatchers.io) {
    override suspend fun doWork(params: Unit): List<ObjectWrapper.SpaceView> {
        val result = repo.searchObjects(
            keys = listOf(
                Relations.ID,
                Relations.TARGET_SPACE_ID,
                Relations.NAME,
                Relations.LAYOUT,
                Relations.ICON_IMAGE,
                Relations.ICON_OPTION,
                Relations.SPACE_ACCOUNT_STATUS,
                Relations.SPACE_LOCAL_STATUS,
                Relations.LAST_OPENED_DATE
            ),
            filters = listOf(
                DVFilter(
                    relation = Relations.LAYOUT,
                    value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                ),
                DVFilter(
                    relation = Relations.SPACE_ACCOUNT_STATUS,
                    value = buildList {
                        add(SpaceStatus.SPACE_DELETED.code.toDouble())
                        add(SpaceStatus.SPACE_REMOVING.code.toDouble())
                    },
                    condition = DVFilterCondition.NOT_IN
                ),
                DVFilter(
                    relation = Relations.SPACE_LOCAL_STATUS,
                    value = SpaceStatus.OK.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                )
            ),
            sorts = listOf(
                DVSort(
                    relationKey = Relations.LAST_OPENED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true
                )
            ),
            limit = 0
        )
        return result.map {
            ObjectWrapper.SpaceView(it)
        }
    }
}