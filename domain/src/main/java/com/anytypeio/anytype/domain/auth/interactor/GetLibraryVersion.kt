package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

open class GetLibraryVersion(
    private val repo: AuthRepository
) : BaseUseCase<String, BaseUseCase.None>() {

    override suspend fun run(params: BaseUseCase.None): Either<Throwable, String> = safe {
        repo.getVersion().removePrefix(PREFIX)
    }

    companion object {
        const val PREFIX = "v"
    }
}