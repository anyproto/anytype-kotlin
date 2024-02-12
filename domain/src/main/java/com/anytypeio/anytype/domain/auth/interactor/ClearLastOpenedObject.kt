package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository

/**
 * Use case for clearing last open object's id from user session.
 * @see SaveLastOpenedObject
 */
class ClearLastOpenedObject(
    private val repo: UserSettingsRepository
) : BaseUseCase<Unit, ClearLastOpenedObject.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.clearLastOpenedObject(params.space)
    }

    data class Params(val space: SpaceId)
}