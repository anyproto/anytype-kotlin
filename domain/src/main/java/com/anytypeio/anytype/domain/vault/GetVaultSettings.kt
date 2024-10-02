package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class GetVaultSettings @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<Unit, VaultSettings>(dispatchers.io) {

    override suspend fun doWork(params: Unit): VaultSettings {
        val acc = auth.getCurrentAccount()
        return settings.getVaultSettings(acc)
    }
}