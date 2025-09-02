package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest

class ObserveVaultSettings @Inject constructor(
    private val awaitAccountStart: AwaitAccountStartManager,
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
): FlowInteractor<Unit, VaultSettings>(dispatchers.io) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun build(): Flow<VaultSettings> = awaitAccountStart
        .awaitStart()
        .flatMapLatest {
            val acc = auth.getCurrentAccount()
            settings.observeVaultSettings(acc)
        }.catch {
            logger.logException(it, "Error while observing vault settings")
            emit(VaultSettings.default())
        }

    override fun build(params: Unit): Flow<VaultSettings> = build()
}