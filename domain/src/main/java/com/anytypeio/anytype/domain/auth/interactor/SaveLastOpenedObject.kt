package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase

/**
 * Use case for saving last open object's id for restoring user session.
 * @see GetLastOpenedObject
 * @see ClearLastOpenedObject
 */
class SaveLastOpenedObject(
    private val repo: AuthRepository
) : BaseUseCase<Unit, Id>() {
    override suspend fun run(params: Id) = safe { repo.saveLastOpenedObjectId(params) }
}