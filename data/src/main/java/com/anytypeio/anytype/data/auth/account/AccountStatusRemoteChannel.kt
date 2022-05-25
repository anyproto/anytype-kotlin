package com.anytypeio.anytype.data.auth.account

import com.anytypeio.anytype.core_models.AccountStatus
import kotlinx.coroutines.flow.Flow

interface AccountStatusRemoteChannel {
    fun observe(): Flow<AccountStatus>
}