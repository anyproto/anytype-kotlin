package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SetVaultSpaceOrder @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<List<Id>, Unit>(dispatchers.io) {

    override suspend fun doWork(params: List<Id>): Unit {
        val acc = auth.getCurrentAccount()
        return settings.setVaultSpaceOrder(acc, params)
    }
}