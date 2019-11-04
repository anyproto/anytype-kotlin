package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.FlowUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.scan

class ObserveAccounts(
    private val repository: AuthRepository
) : FlowUseCase<List<Account>, Unit>() {

    override suspend fun build(
        params: Unit?
    ) = repository
        .observeAccounts()
        .scan(emptyList<Account>()) { list, value -> list + value }
        .drop(1)

    override suspend fun stream(receiver: suspend (List<Account>) -> Unit) {
        build().collect(receiver)
    }
}