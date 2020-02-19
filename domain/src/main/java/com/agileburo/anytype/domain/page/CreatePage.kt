package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.MainConfig

/**
 * A use-case for creating a new page.
 * Currently used for creating a new page inside a dashboard.
 */
class CreatePage(
    private val repo: BlockRepository
) : BaseUseCase<Id, CreatePage.Params>() {

    override suspend fun run(params: Params) = try {
        if (params.id == MainConfig.HOME_DASHBOARD_ID) {
            repo.getConfig().let { config ->
                repo.createPage(config.home).let {
                    Either.Right(it)
                }
            }
        } else {
            repo.createPage(params.id).let {
                Either.Right(it)
            }
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property id parent id for a new page
     */
    data class Params(val id: String) {
        companion object {
            fun insideDashboard() = Params(MainConfig.HOME_DASHBOARD_ID)
        }
    }
}