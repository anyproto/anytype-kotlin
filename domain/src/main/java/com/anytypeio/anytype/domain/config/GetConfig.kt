package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

class GetConfig(
    private val provider: ConfigStorage
) : BaseUseCase<Config, Unit>() {

    override suspend fun run(params: Unit) = try {
        provider.get().let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }
}