package com.anytypeio.anytype.middleware.mappers

import anytype.Event
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVFilterUpdate
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVSortUpdate
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVViewerRelationUpdate

fun Event.Object.Subscription.Counters.parse(): SearchResult.Counter = SearchResult.Counter(
    total = total.toInt(),
    prev = prevCount.toInt(),
    next = nextCount.toInt()
)

fun MDVFilterUpdate.toCoreModels(): DVFilterUpdate? {
    val filter = this
    if (filter.add != null) {
        val add = filter.add
        checkNotNull(add)
        return DVFilterUpdate.Add(
            filters = add.items.map { it.toCoreModels() },
            afterId = add.afterId
        )
    }
    if (filter.move != null) {
        val move = filter.move
        checkNotNull(move)
        return DVFilterUpdate.Move(
            afterId = move.afterId,
            ids = move.ids,
        )
    }
    if (filter.remove != null) {
        val remove = filter.remove
        checkNotNull(remove)
        return DVFilterUpdate.Remove(
            ids = remove.ids,
        )
    }
    if (filter.update != null) {
        val update = filter.update
        checkNotNull(update)
        val item = update.item
        if (item != null) {
            return DVFilterUpdate.Update(
                id = update.id,
                filter = item.toCoreModels()
            )
        }
    }
    return null
}

fun MDVSortUpdate.toCoreModels(): DVSortUpdate? {
    val sort = this
    if (sort.add != null) {
        val add = sort.add
        checkNotNull(add)
        return DVSortUpdate.Add(
            sorts = add.items.map { it.toCoreModels() },
            afterId = add.afterId
        )
    }
    if (sort.move != null) {
        val move = sort.move
        checkNotNull(move)
        return DVSortUpdate.Move(
            afterId = move.afterId,
            ids = move.ids,
        )
    }
    if (sort.remove != null) {
        val remove = sort.remove
        checkNotNull(remove)
        return DVSortUpdate.Remove(
            ids = remove.ids,
        )
    }
    if (sort.update != null) {
        val update = sort.update
        checkNotNull(update)
        val item = update.item
        if (item != null) {
            return DVSortUpdate.Update(
                id = update.id,
                sort = item.toCoreModels()
            )
        }
    }
    return null
}

fun MDVRelationUpdate.toCoreModels(): DVViewerRelationUpdate? {
    val relation = this
    if (relation.add != null) {
        val add = relation.add
        checkNotNull(add)
        return DVViewerRelationUpdate.Add(
            relations = add.items.map { it.toCoreModels() },
            afterId = add.afterId
        )
    }
    if (relation.move != null) {
        val move = relation.move
        checkNotNull(move)
        return DVViewerRelationUpdate.Move(
            afterId = move.afterId,
            ids = move.ids,
        )
    }
    if (relation.remove != null) {
        val remove = relation.remove
        checkNotNull(remove)
        return DVViewerRelationUpdate.Remove(
            ids = remove.ids,
        )
    }
    if (relation.update != null) {
        val update = relation.update
        checkNotNull(update)
        val item = update.item
        if (item != null) {
            return DVViewerRelationUpdate.Update(
                id = update.id,
                relation = item.toCoreModels()
            )
        }
    }
    return null
}