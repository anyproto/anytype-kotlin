package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll

class ObserveRecentlyUsedChatReactions @Inject constructor(
    private val auth: AuthRepository,
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : FlowInteractor<Unit, List<String>>(dispatchers.io) {

    override fun build(): Flow<List<String>> = flow {
        val acc = auth.getCurrentAccount()
        emitAll(repo.observeRecentlyUsedChatReactions(acc))
    }

    override fun build(params: Unit): Flow<List<String>> = flow {
        val acc = auth.getCurrentAccount()
        emitAll(repo.observeRecentlyUsedChatReactions(acc))
    }
}