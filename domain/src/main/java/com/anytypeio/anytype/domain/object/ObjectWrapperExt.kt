package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Function for applying granular changes in object, replacing existing values with the new ones.
 * @param [diff] difference
 */
fun ObjectWrapper.Basic.amend(diff: Map<Id, Any?>) = ObjectWrapper.Basic(map + diff)
fun ObjectWrapper.Relation.amend(diff: Map<Id, Any?>) = ObjectWrapper.Relation(map + diff)
fun ObjectWrapper.Type.amend(diff: Map<Id, Any?>) = ObjectWrapper.Type(map + diff)
fun ObjectWrapper.Option.amend(diff: Map<Id, Any?>) = ObjectWrapper.Option(map + diff)
/**
 * Function for applying granular changes in object.
 */
fun ObjectWrapper.Basic.unset(keys: List<Id>) = ObjectWrapper.Basic(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun ObjectWrapper.Relation.unset(keys: List<Id>) = ObjectWrapper.Relation(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun ObjectWrapper.Type.unset(keys: List<Id>) = ObjectWrapper.Type(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun ObjectWrapper.Option.unset(keys: List<Id>) = ObjectWrapper.Option(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun List<Id>.move(target: Id, afterId: Id?) : List<Id> {
    val result = toMutableList()
    val targetIdx = indexOfFirst { it == target }
    if (targetIdx != -1) {
        val item = get(targetIdx)
        result.removeAt(targetIdx)
        val prevIdx = indexOfFirst { it == afterId }
        if (prevIdx != -1) {
            if (prevIdx < targetIdx) {
                result.add(prevIdx.inc(), item)
            } else {
                result.add(prevIdx, item)
            }
        } else {
            result.add(0, item)
        }
    }
    return result
}

/**
 * Resolves a participant's display name by identity from the participant map.
 * Uses the standard fallback chain: name → globalName → fallback.
 *
 * @param identity The identity ID to look up
 * @param fallback The fallback string if participant not found, has no name, or identity is null/empty
 * @return The resolved name or fallback - always returns a non-null value
 */
fun Map<Id, ObjectWrapper.SpaceMember>.resolveParticipantName(
    identity: Id?,
    fallback: String
): String {
    if (identity.isNullOrEmpty()) return fallback
    val participant = this[identity]
    return participant?.name
        ?: participant?.globalName
        ?: fallback
}