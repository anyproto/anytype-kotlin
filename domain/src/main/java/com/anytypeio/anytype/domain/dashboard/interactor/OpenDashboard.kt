package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for opening a dashboard by sending a special request.
 *
 * @property repo
 */
class OpenDashboard(
    private val repo: BlockRepository
) : BaseUseCase<Payload, OpenDashboard.Param?>() {

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
                    contextId = config.home,
                    id = config.home
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