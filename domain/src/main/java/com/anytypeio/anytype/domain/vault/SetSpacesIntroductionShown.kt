package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

/**
 * Marks the spaces introduction popup as shown for the current account.
 */
class SetSpacesIntroductionShown @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetSpacesIntroductionShown.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: SetSpacesIntroductionShown.Params) {
        settings.setHasShownSpacesIntroduction(params.account, true)
    }

    data class Params(val account: Account)
}