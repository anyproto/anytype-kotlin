package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

class UseCustomContextMenu(private val repository: InfrastructureRepository) :
    BaseUseCase<Unit, UseCustomContextMenu.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = safe {
        if (params.use) {
            repository.enableAnytypeContextMenu()
        } else {
            repository.disableAnytypeContextMenu()
        }
    }

    data class Params(val use: Boolean)
}