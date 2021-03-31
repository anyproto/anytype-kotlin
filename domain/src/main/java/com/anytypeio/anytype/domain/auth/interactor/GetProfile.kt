package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload

/** Use case for getting currently selected user account.
 */
class GetProfile(
    private val repo: BlockRepository
) : BaseUseCase<Payload, BaseUseCase.None>() {

    override suspend fun run(params: None) = try {

        val config = repo.getConfig()

        val payload = repo.openProfile(config.profile)

        Either.Right(payload)

    } catch (t: Throwable) {
        Either.Left(t)
    }
}