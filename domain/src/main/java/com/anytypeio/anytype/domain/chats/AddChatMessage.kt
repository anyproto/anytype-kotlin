package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * returns message ID and payload commands.
 */
class AddChatMessage @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Command.ChatCommand.AddMessage, Pair<Id, List<Event.Command.Chats>>>(dispatchers.io) {

    override suspend fun doWork(params: Command.ChatCommand.AddMessage): Pair<Id, List<Event.Command.Chats>> {
        return repo.addChatMessage(params)
    }
}