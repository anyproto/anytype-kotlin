package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetChatMessagesByIds @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Command.ChatCommand.GetMessagesByIds, List<Chat.Message>>(dispatchers.io) {

    override suspend fun doWork(params: Command.ChatCommand.GetMessagesByIds): List<Chat.Message> {
        return repo.getChatMessagesByIds(params)
    }
}