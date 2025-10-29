package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipTiers
import kotlinx.coroutines.flow.Flow

interface MembershipChannel {
    fun observe(): Flow<List<Membership.Event>>
    fun observeTiers(): Flow<List<MembershipTiers.Event>>
}