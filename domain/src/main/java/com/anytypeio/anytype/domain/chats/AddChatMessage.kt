package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class AddChatMessage @Inject constructor(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Command.ChatCommand.AddChatMessage, Id>(dispatchers.io) {
    override suspend fun doWork(params: Command.ChatCommand.AddChatMessage): Id {
        return repo.addChatMessage(command = params)
    }
}