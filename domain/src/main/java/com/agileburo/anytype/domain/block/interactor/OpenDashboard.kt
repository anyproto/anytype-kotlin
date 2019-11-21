package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.MainConfig

/**
 * Use-case for opening a dashboard by sending a special request.
 *
 * @property repo
 */
class OpenDashboard(
    private val repo: BlockRepository
) : BaseUseCase<Unit, OpenDashboard.Param>() {

    override suspend fun run(params: Param) = try {
        repo.openDashboard(contextId = params.contextId, id = params.id).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property id dashboard id
     * @property contextId contextId id
     */
    class Param(val contextId: String, val id: String) {
        companion object {
            fun home(): Param {
                return Param(
                    contextId = MainConfig.HOME_DASHBOARD_ID,
                    id = MainConfig.HOME_DASHBOARD_ID
                )
            }
        }
    }
}