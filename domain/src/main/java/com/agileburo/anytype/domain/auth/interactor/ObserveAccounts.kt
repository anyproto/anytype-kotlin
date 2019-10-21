package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAccounts(
    private val repository: AuthRepository
) : FlowUseCase<Account, Unit>() {

    override fun stream(params: Unit): Flow<Account> = repository.observeAccounts()
}