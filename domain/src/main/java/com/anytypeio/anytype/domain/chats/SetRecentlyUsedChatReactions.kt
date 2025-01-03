package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SetRecentlyUsedChatReactions @Inject constructor(
    private val auth: AuthRepository,
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Set<String>, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Set<String>) {
        val acc = auth.getCurrentAccount()
        repo.setRecentlyUsedChatReactions(
            account = acc,
            emojis = params
        )
    }
}