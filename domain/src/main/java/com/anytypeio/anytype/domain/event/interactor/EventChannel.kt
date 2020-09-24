package com.anytypeio.anytype.domain.event.interactor

import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.Flow

interface EventChannel {
    fun observeEvents(context: Id? = null): Flow<List<Event>>
}