package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

/**
 * Marks the spaces introduction popup as shown for the current account.
 */
class SetSpacesIntroductionShown @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Unit) {
        val account = auth.getCurrentAccount()
        settings.setHasShownSpacesIntroduction(account, true)
    }
}