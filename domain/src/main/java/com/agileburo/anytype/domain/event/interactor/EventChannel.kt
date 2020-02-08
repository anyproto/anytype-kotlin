package com.agileburo.anytype.domain.event.interactor

import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.Flow

interface EventChannel {
    fun observeEvents(context: Id? = null): Flow<List<Event>>
}