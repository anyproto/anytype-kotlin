package com.anytypeio.anytype.domain.account

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class InterceptAccountStatus(
    private val channel: AccountStatusChannel,
    private val dispatchers: AppCoroutineDispatchers
) : FlowUseCase<AccountStatus, BaseUseCase.None>() {
    override fun build(params: BaseUseCase.None?): Flow<AccountStatus> {
        return channel.observe().flowOn(dispatchers.io)
    }
}