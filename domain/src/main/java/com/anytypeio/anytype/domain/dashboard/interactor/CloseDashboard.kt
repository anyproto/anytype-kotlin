package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.MainConfig

/**
 * Use-case for closing a dashboard by sending a special request.
 *
 * @property repo
 */
class CloseDashboard(
    private val repo: BlockRepository,
    private val provider: ConfigStorage
) : BaseUseCase<Unit, CloseDashboard.Param>() {

    override suspend fun run(params: Param) = safe {
        if (params.id == MainConfig.HOME_DASHBOARD_ID) {
            val config = provider.get()
            repo.closeDashboard(id = config.home)
        } else {
            repo.closeDashboard(id = params.id)
        }
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