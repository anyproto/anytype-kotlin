package com.agileburo.anytype.domain.dashboard.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for opening a dashboard by sending a special request.
 *
 * @property repo
 */
class OpenDashboard(
    private val repo: BlockRepository
) : BaseUseCase<Unit, OpenDashboard.Param?>() {

    override suspend fun run(params: Param?) = try {
        if (params != null)
            repo.openDashboard(
                contextId = params.contextId,
                id = params.id
            ).let {
                Either.Right(it)
            }
        else {
            repo.getConfig().let { config ->
                repo.openDashboard(
                    contextId = config.homeId,
                    id = config.homeId
                ).let {
                    Either.Right(it)
                }
            }
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property id dashboard id
     * @property contextId contextId id
     */
    class Param(val contextId: String, val id: String)
}