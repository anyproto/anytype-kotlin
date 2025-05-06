package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class RegisterDeviceTokenUseCase @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RegisterDeviceTokenUseCase.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val request = Command.RegisterDeviceToken(
            token = params.token,
        )
        repository.registerDeviceToken(request)
    }

    data class Params(
        val token: String
    )
}