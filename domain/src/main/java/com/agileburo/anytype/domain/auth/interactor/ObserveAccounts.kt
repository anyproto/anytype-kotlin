package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.FlowUseCase
import kotlinx.coroutines.flow.collect

class ObserveAccounts(
    private val repository: AuthRepository
) : FlowUseCase<Account, Unit>() {

    override suspend fun build(
        params: Unit?
    ) = repository.observeAccounts()


    override suspend fun stream(receiver: suspend (Account) -> Unit) {
        build().collect(receiver)
    }
}