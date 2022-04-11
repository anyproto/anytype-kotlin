package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.data.auth.account.AccountStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.core
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class AccountStatusMiddlewareChannel(
    private val events: EventProxy
) : AccountStatusRemoteChannel {

    override fun observe(): Flow<AccountStatus> = events.flow().mapNotNull { e ->
        val updates = e.messages.filter { m ->
            m.accountUpdate != null
        }
        val lastUpdate = updates.lastOrNull()?.accountUpdate

        lastUpdate?.status?.core()
    }
}