package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CancelSearchSubscription(
    private val repo: BlockRepository
) : BaseUseCase<Unit, CancelSearchSubscription.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.cancelObjectSearchSubscription(
            subscriptions = params.subscriptions
        )
    }

    class Params(val subscriptions: List<Id>)
}