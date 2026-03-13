package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SearchChatMessages @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Command.ChatCommand.SearchMessages, Command.ChatCommand.SearchMessages.Response>(dispatchers.io) {
    override suspend fun doWork(params: Command.ChatCommand.SearchMessages): Command.ChatCommand.SearchMessages.Response {
        return repo.searchChatMessages(params)
    }
}
