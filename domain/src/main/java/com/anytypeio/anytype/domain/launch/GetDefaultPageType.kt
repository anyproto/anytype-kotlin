package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class GetDefaultPageType(
    private val userSettingsRepository: UserSettingsRepository
) : BaseUseCase<GetDefaultPageType.Response, Unit>() {

    override suspend fun run(params: Unit) = safe {
        Response(userSettingsRepository.getDefaultPageType())
    }

    class Response(val type: String?)
}