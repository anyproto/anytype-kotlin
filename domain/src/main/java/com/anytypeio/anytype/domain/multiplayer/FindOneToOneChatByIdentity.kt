package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Finds an existing ONE_TO_ONE chat space with the specified participant identity.
 *
 * This use case searches through all space views to find a ONE_TO_ONE space
 * where the [Relations.ONE_TO_ONE_IDENTITY] matches the given identity.
 *
 * @property spaceViewSubscriptionContainer Provides access to all space views
 */
class FindOneToOneChatByIdentity @Inject constructor(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<FindOneToOneChatByIdentity.Params, ExistingOneToOneChat?>(dispatchers.io) {

    override suspend fun doWork(params: Params): ExistingOneToOneChat? {
        val spaceViews = spaceViewSubscriptionContainer.observe().first()

        val matchingSpace = spaceViews.firstOrNull { spaceView ->
            spaceView.isOneToOneSpace && spaceView.oneToOneIdentity == params.identity
        }

        return matchingSpace?.let { spaceView ->
            ExistingOneToOneChat(
                spaceId = SpaceId(spaceView.targetSpaceId ?: return@let null),
                spaceViewId = spaceView.id,
                chatId = spaceView.chatId
            )
        }
    }

    data class Params(
        val identity: Id
    )
}

/**
 * Represents an existing ONE_TO_ONE chat space found by identity.
 *
 * @property spaceId The target space ID of the ONE_TO_ONE space
 * @property spaceViewId The ID of the SpaceView object
 * @property chatId The chat object ID within the space, if available
 */
data class ExistingOneToOneChat(
    val spaceId: SpaceId,
    val spaceViewId: Id,
    val chatId: Id?
)
