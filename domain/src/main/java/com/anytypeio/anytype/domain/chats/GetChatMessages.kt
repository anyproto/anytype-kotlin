package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetChatMessages @Inject constructor(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
): ResultInteractor<Command.ChatCommand.GetChatMessages, List<Chat.Message>>(dispatchers.io) {
    override suspend fun doWork(
        params: Command.ChatCommand.GetChatMessages
    ): List<Chat.Message> {
        return repo.getChatMessages(params)
    }
}