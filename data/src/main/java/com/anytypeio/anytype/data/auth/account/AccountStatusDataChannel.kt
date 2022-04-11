package com.anytypeio.anytype.data.auth.account

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import kotlinx.coroutines.flow.Flow

class AccountStatusDataChannel(
    private val remote: AccountStatusRemoteChannel
) : AccountStatusChannel {
    override fun observe(): Flow<AccountStatus> = remote.observe()
}