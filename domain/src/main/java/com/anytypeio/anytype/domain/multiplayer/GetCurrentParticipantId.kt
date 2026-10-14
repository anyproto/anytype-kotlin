package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * The id of the participant object of the current account in a space.
 * The middleware builds this id from the space id and the account id, so no search is necessary.
 */
class GetCurrentParticipantId @Inject constructor(
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SpaceId, Id>(dispatchers.io) {

    override suspend fun doWork(params: SpaceId): Id = participantId(
        space = params,
        account = auth.getCurrentAccountId()
    )

    companion object {
        fun participantId(space: SpaceId, account: Id): Id =
            "_participant_${space.id.replace('.', '_')}_$account"
    }
}
