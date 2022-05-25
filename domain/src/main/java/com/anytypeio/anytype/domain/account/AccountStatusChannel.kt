package com.anytypeio.anytype.domain.account

import com.anytypeio.anytype.core_models.AccountStatus
import kotlinx.coroutines.flow.Flow

interface AccountStatusChannel {
    fun observe(): Flow<AccountStatus>
}