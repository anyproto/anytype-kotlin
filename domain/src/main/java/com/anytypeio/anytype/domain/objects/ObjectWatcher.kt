package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan

class ObjectWatcher @Inject constructor(
    private val repo: BlockRepository,
    private val events: EventChannel,
    private val reducer: Reducer
) {
    fun watch(target: Id, space: SpaceId): Flow<ObjectView> = flow {
        emitAll(
            events.observeEvents(context = target).scan(
                initial = repo.openObject(id = target, space = space),
                operation = reducer
            )
        )
    }

    suspend fun unwatch(target: Id, space: SpaceId) {
        repo.closePage(id = target, space = space)
    }

    interface Reducer : (ObjectView, List<Event>) -> ObjectView
}

