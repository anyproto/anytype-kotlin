package com.agileburo.anytype.domain.config

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

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