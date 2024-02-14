package com.anytypeio.anytype.domain.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository

@Deprecated("Legacy. Need to migrate to OpenObject useCase")
class OpenObjectSet(
    private val repo: BlockRepository,
    private val settings: UserSettingsRepository
) : BaseUseCase<Result<Payload>, OpenObjectSet.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.openObjectSet(params.obj).also {
            settings.setLastOpenedObject(
                id = params.obj,
                space = params.space
            )
        }
    }

    data class Params(
        val space: SpaceId,
        val obj: Id
    )
}