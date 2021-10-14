package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase

/**
 * Use case for clearing last open object's id from user session.
 * @see GetLastOpenedObject
 * @see SaveLastOpenedObject
 */
class ClearLastOpenedObject(
    private val repo: AuthRepository
) : BaseUseCase<Unit, BaseUseCase.None>() {
    override suspend fun run(params: None) = safe { repo.clearLastOpenedObject() }
}