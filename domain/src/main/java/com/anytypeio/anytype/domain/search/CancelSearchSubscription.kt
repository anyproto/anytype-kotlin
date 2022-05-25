package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.ObjectStore

class CancelSearchSubscription(
    private val repo: BlockRepository,
    private val store: ObjectStore
) : BaseUseCase<Unit, CancelSearchSubscription.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.cancelObjectSearchSubscription(params.subscriptions)
        store.unsubscribe(params.subscriptions)
    }

    class Params(val subscriptions: List<Id>)
}