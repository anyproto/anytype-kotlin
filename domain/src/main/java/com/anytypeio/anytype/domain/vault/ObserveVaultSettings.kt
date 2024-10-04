package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class ObserveVaultSettings @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
): FlowInteractor<Unit, VaultSettings>(dispatchers.io) {
    override fun build(): Flow<VaultSettings> = flow {
        val acc = auth.getCurrentAccount()
        emitAll(settings.observeVaultSettings(acc))
    }
    override fun build(params: Unit): Flow<VaultSettings> = build()
}