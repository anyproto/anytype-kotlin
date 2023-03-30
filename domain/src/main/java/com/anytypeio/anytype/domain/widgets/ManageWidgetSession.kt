package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class GetWidgetSession @Inject constructor(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, WidgetSession>(dispatchers.io) {
    override suspend fun doWork(params: Unit): WidgetSession = repo.getWidgetSession()
}

class SaveWidgetSession @Inject constructor(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SaveWidgetSession.Params, Unit>(dispatchers.io) {
    override suspend fun doWork(params: Params) {
        repo.saveWidgetSession(params.session)
    }

    data class Params(val session: WidgetSession)
}