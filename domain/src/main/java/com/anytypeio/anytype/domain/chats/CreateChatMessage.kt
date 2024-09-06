package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateChatMessage @Inject constructor(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Unit>(dispatchers.io) {


    override suspend fun doWork(params: Unit) {
        TODO()
    }
}