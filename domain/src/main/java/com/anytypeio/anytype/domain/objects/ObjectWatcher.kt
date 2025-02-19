package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan

class ObjectWatcher @Inject constructor(
    private val repo: BlockRepository,
    private val events: EventChannel,
    private val reducer: Reducer
) {
    /**
     * Watches an object and returns a flow of ObjectView.
     * If opening the object or observing events fails, the flow will throw an exception.
     */
    fun watch(target: Id, space: SpaceId): Flow<ObjectView> = flow {
        // Attempt to open the object. If it fails, throw.
        val initialObjectView = try {
            repo.openObject(id = target, space = space)
        } catch (e: Exception) {
            throw RuntimeException("Failed to open object with id=$target in space=$space", e)
        }

        // Start observing events. If events flow fails at any point, it will throw.
        val eventFlow = events.observeEvents(context = target)
            .catch { e -> throw RuntimeException("Failed to observe events for object=$target", e) }

        emitAll(eventFlow.scan(initialObjectView, reducer))
    }.catch { e ->
        // Optional: If you want a centralized place to rethrow or log, you can do it here.
        throw e
    }

    /**
     * Stops watching an object.
     * If closing the page fails, it will throw an exception.
     */
    suspend fun unwatch(target: Id, space: SpaceId) {
        try {
            repo.closePage(id = target, space = space)
        } catch (e: Exception) {
            throw RuntimeException("Failed to unwatch object with id=$target in space=$space", e)
        }
    }

    interface Reducer : (ObjectView, List<Event>) -> ObjectView
}

