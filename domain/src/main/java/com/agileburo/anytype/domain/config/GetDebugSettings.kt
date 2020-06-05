package com.agileburo.anytype.domain.config

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

class GetDebugSettings(private val repo: InfrastructureRepository) :
    BaseUseCase<DebugSettings, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, DebugSettings> = safe {
        DebugSettings(isAnytypeContextMenuEnabled = repo.getAnytypeContextMenu())
    }
}