package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class GetDefaultEditorType(
    private val userSettingsRepository: UserSettingsRepository
) : ResultInteractor<Unit, GetDefaultEditorType.Response>() {

    override suspend fun doWork(params: Unit): Response {
        val pair = userSettingsRepository.getDefaultObjectType()
        return Response(pair.first, pair.second)
    }

    class Response(val type: String?, val name: String?)
}