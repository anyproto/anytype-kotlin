package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import kotlinx.coroutines.Dispatchers

class GetDefaultEditorType(
    private val userSettingsRepository: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, GetDefaultEditorType.Response>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Response {
        val pair = userSettingsRepository.getDefaultObjectType()
        return Response(pair.first, pair.second)
    }

    class Response(val type: String?, val name: String?)
}