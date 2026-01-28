package com.anytypeio.anytype.feature_vault.presentation

import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.spaces.ClearLastOpenedSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import timber.log.Timber

interface ExitToVaultDelegate {

    suspend fun proceedWithClearingSpaceBeforeExitingToVault()

    class Default @Inject constructor(
        val spaceManager: SpaceManager,
        val clearLastOpenedSpace: ClearLastOpenedSpace
    ): ExitToVaultDelegate {
        override suspend fun proceedWithClearingSpaceBeforeExitingToVault() {
            spaceManager.clear()
            clearLastOpenedSpace.async(Unit).fold(
                onSuccess = {
                    Timber.d("Cleared last opened space before opening vault")
                },
                onFailure = {
                    Timber.e(it, "Error while clearing last opened space before opening vault")
                }
            )
        }
    }

}
