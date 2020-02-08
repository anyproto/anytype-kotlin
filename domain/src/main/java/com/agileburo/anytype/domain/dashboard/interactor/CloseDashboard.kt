package com.agileburo.anytype.domain.dashboard.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.MainConfig

/**
 * Use-case for closing a dashboard by sending a special request.
 *
 * @property repo
 */
class CloseDashboard(
    private val repo: BlockRepository
) : BaseUseCase<Unit, CloseDashboard.Param>() {

    override suspend fun run(params: Param) = try {
        if (params.id == MainConfig.HOME_DASHBOARD_ID)
            repo.getConfig().let { config ->
                repo.closeDashboard(id = config.home)
            }.let {
                Either.Right(it)
            }
        else
            repo.closeDashboard(id = params.id).let {
                Either.Right(it)
            }

    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property id dashboard id
     */
    class Param(val id: String) {
        companion object {
            fun home() = Param(MainConfig.HOME_DASHBOARD_ID)
        }
    }
}