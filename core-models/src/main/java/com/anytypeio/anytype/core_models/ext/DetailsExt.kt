package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id

fun Block.Details.process(event: Event.Command.Details) = when (event) {
    is Event.Command.Details.Set -> set(event.target, event.details)
    is Event.Command.Details.Amend -> amend(event.target, event.details)
    is Event.Command.Details.Unset -> unset(event.target, event.keys)
}

fun Block.Details.set(
    target: Id,
    fields: Block.Fields
): Block.Details = Block.Details(details + mapOf(target to fields))

fun Map<Id, Block.Fields>.amend(
    target: Id,
    slice: Map<Id, Any?>
) : Map<Id, Block.Fields> {
    return toMutableMap().apply {
        val current = getOrDefault(target, Block.Fields.empty())
        val new = Block.Fields(current.map + slice)
        set(target, new)
    }
}

fun Block.Details.amend(
    target: Id,
    slice: Map<Id, Any?>
): Block.Details {
    val updated = details.toMutableMap().apply {
        val current = getOrDefault(target, Block.Fields.empty())
        val new = Block.Fields(current.map + slice)
        set(target, new)
    }
    return Block.Details(updated)
}

fun Block.Details.unset(
    target: Id,
    keys: List<Id>
): Block.Details {
    val updated = details.toMutableMap().apply {
        val current = getOrDefault(target, Block.Fields.empty())
        val new = Block.Fields(
            current.map.toMutableMap().apply {
                keys.forEach { key -> remove(key) }
            }
        )
        set(target, new)
    }
    return Block.Details(updated)
}

fun Map<Id, Block.Fields>.unset(
    target: Id,
    keys: List<Id>
) : Map<Id, Block.Fields> {
    return toMutableMap().apply {
        val current = getOrDefault(target, Block.Fields.empty())
        val new = Block.Fields(
            current.map.toMutableMap().apply {
                keys.forEach { key -> remove(key) }
            }
        )
        set(target, new)
    }
}
