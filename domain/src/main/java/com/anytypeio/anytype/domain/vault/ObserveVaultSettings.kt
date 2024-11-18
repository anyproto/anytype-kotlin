package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.DEFAULT_DATE_PATTERN
import com.anytypeio.anytype.core_models.DEFAULT_RELATIVE_DATES
import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class ObserveVaultSettings @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
): FlowInteractor<Unit, VaultSettings>(dispatchers.io) {
    override fun build(): Flow<VaultSettings> = flow {
        val acc = auth.getCurrentAccount()
        emitAll(
            settings
                .observeVaultSettings(acc)
                .catch {
                    logger.logException(it, "Error while observing vault settings")
                    emit(
                        VaultSettings(
                            showIntroduceVault = false,
                            orderOfSpaces = emptyList(),
                            isRelativeDates = DEFAULT_RELATIVE_DATES,
                            dateFormat = DEFAULT_DATE_PATTERN
                        )
                    )
                }
        )
    }
    override fun build(params: Unit): Flow<VaultSettings> = build()
}