package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage

/**
 * Use-case for opening a dashboard by sending a special request.
 *
 * @property repo
 */
class OpenDashboard(
    private val repo: BlockRepository,
    private val auth: AuthRepository,
    private val provider: ConfigStorage
) : ResultInteractor<Unit, Payload>() {

    override suspend fun doWork(params: Unit): Payload {
        val config = provider.get()
        val payload = repo.openDashboard(
            contextId = config.home,
            id = config.home
        )
        return payload.also { auth.clearLastOpenedObject() }
    }
}