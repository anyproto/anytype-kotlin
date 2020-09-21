package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.model.Payload

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