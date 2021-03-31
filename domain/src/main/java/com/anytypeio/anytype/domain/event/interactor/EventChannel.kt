package com.anytypeio.anytype.domain.event.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Event
import kotlinx.coroutines.flow.Flow

interface EventChannel {
    fun observeEvents(context: Id? = null): Flow<List<Event>>
}