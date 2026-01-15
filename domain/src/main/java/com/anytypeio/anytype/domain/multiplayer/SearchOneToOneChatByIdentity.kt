package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Searches for an existing ONE_TO_ONE chat space with the specified participant identity
 * directly via middleware, bypassing the cached SpaceViewSubscriptionContainer.
 *
 * This is necessary because SpaceViewSubscriptionContainer filters out deleted/left spaces,
 * but when trying to rejoin a 1-1 chat, we need to find the space even if it was left.
 *
 * @property repo BlockRepository for middleware access
 */
class SearchOneToOneChatByIdentity @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SearchOneToOneChatByIdentity.Params, ExistingOneToOneChat?>(dispatchers.io) {

    override suspend fun doWork(params: Params): ExistingOneToOneChat? {
        val results = repo.searchObjects(
            space = params.techSpace,
            filters = listOf(
                DVFilter(
                    relation = Relations.LAYOUT,
                    value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                ),
                DVFilter(
                    relation = Relations.ONE_TO_ONE_IDENTITY,
                    value = params.identity,
                    condition = DVFilterCondition.EQUAL
                )
            ),
            keys = listOf(
                Relations.ID,
                Relations.TARGET_SPACE_ID,
                Relations.ONE_TO_ONE_IDENTITY
            ),
            limit = 1
        )

        val spaceView = results.firstOrNull() ?: return null
        val targetSpaceId = spaceView.getSingleValue<String>(Relations.TARGET_SPACE_ID) ?: return null
        val spaceId = spaceView.getSingleValue<String>(Relations.ID) ?: return null

        return ExistingOneToOneChat(
            spaceId = SpaceId(targetSpaceId),
            spaceViewId = spaceId
        )
    }

    data class Params(
        val identity: Id,
        val techSpace: SpaceId
    )
}

/**
 * Represents an existing ONE_TO_ONE chat space found by identity.
 *
 * @property spaceId The target space ID of the ONE_TO_ONE space
 * @property spaceViewId The ID of the SpaceView object
 */
data class ExistingOneToOneChat(
    val spaceId: SpaceId,
    val spaceViewId: Id
)
