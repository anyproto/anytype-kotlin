package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct

fun Map<Id, Struct>.process(event: Event.Command.Details) = when (event) {
    is Event.Command.Details.Set -> set(event.target, event.details.map)
    is Event.Command.Details.Amend -> amend(event.target, event.details)
    is Event.Command.Details.Unset -> unset(event.target, event.keys)
}

fun Map<Id, Struct>.set(
    target: Id,
    details: Struct
): Map<Id, Struct> = this + (target to details)

fun Map<Id, Struct>.amend(
    target: Id,
    slice: Map<Id, Any?>
): Map<Id, Struct> {
    val curr = getOrDefault(target, emptyMap())
    val update = buildMap<Id, Struct> {
        if (curr.isNotEmpty()) {
            put(target, curr + slice)
        } else {
            put(target, slice)
        }
    }
    return this + update
}

fun Map<Id, Struct>.unset(
    target: Id,
    keys: List<Id>
): Map<Id, Struct> {
    val curr = getOrDefault(target, emptyMap())
    val update = curr - keys
    return this + mapOf(target to update)
}

fun Struct.mapToObjectWrapperType(): ObjectWrapper.Type? =
    if (containsKey(Relations.ID) && containsKey(Relations.UNIQUE_KEY)) ObjectWrapper.Type(this)
    else null

inline fun <reified T> Struct.getValues(key: String): List<T> {
    return when (val value = getOrDefault(key, emptyList<T>())) {
        is T -> listOf(value)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
}