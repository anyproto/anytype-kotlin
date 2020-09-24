package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

class GetDebugSettings(private val repo: InfrastructureRepository) :
    BaseUseCase<DebugSettings, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, DebugSettings> = safe {
        DebugSettings(isAnytypeContextMenuEnabled = repo.getAnytypeContextMenu())
    }
}