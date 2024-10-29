package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ToggleChatMessageReaction @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Command.ChatCommand.ToggleMessageReaction, Unit>(dispatchers.io) {
    override suspend fun doWork(params: Command.ChatCommand.ToggleMessageReaction) {
        return repo.toggleChatMessageReaction(command = params)
    }
}