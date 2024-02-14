package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

open class OpenPage @Inject constructor(
    private val repo: BlockRepository,
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<OpenPage.Params, Result<Payload>>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result<Payload> {
        return repo.openPage(params.obj).also {
            if (params.saveAsLastOpened) {
                settings.setLastOpenedObject(
                    id = params.obj,
                    space = params.space
                )
            } else {
                val givenSpace = params.space
                if (givenSpace.id.isNotEmpty()) {
                    settings.clearLastOpenedObject(
                        SpaceId(givenSpace.id)
                    )
                }
            }
        }
    }

    data class Params(
        val obj: Id,
        val saveAsLastOpened: Boolean,
        val space: SpaceId
    )
}