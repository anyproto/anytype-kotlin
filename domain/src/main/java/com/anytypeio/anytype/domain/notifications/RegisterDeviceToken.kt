package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class RegisterDeviceToken @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RegisterDeviceToken.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.RegisterDeviceToken(
            token = params.token,
        )
        repository.registerDeviceToken(command = command)
    }

    data class Params(
        val token: String
    )
}