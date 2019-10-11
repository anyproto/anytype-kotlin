package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.feature_login.ui.login.domain.common.FlowUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccounts(
    private val repository: UserRepository
) : FlowUseCase<Account, Unit>() {

    override fun stream(params: Unit): Flow<Account> = repository.observeAccounts()
}