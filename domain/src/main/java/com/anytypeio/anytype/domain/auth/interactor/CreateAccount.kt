package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.workspace.WorkspaceManager

/**
 * Creates an account, then stores it and sets as current user account.
 */
open class CreateAccount(
    private val repository: AuthRepository,
    private val configStorage: ConfigStorage,
    private val workspaceManager: WorkspaceManager
) : BaseUseCase<Account, CreateAccount.Params>() {

    override suspend fun run(params: Params) = safe {
        val setup = repository.createAccount(
            name = params.name,
            avatarPath = params.avatarPath,
            invitationCode = params.invitationCode,
            icon = params.iconGradientValue
        )
        with(repository) {
            saveAccount(setup.account)
            setCurrentAccount(setup.account.id)
        }
        configStorage.set(setup.config)
        workspaceManager.setCurrentWorkspace(setup.config.workspace)
        setup.account
    }

    /**
     * @property avatarPath optional avatar image file path
     * @property name username
     * @property iconGradientValue random icon gradient value for new account/space background
     */
    class Params(
        val name: String,
        val avatarPath: String? = null,
        val invitationCode: String,
        val iconGradientValue: Int
    )
}