package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Function for applying granular changes in object, replacing existing values with the new ones.
 * @param [diff] difference
 */
fun ObjectWrapper.Basic.amend(diff: Map<String, Any?>) = ObjectWrapper.Basic(map + diff)
fun ObjectWrapper.Relation.amend(diff: Map<String, Any?>) = ObjectWrapper.Relation(map + diff)
fun ObjectWrapper.Type.amend(diff: Map<String, Any?>) = ObjectWrapper.Type(map + diff)
/**
 * Function for applying granular changes in object.
 */
fun ObjectWrapper.Basic.unset(keys: List<String>) = ObjectWrapper.Basic(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun ObjectWrapper.Relation.unset(keys: List<String>) = ObjectWrapper.Relation(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun ObjectWrapper.Type.unset(keys: List<String>) = ObjectWrapper.Type(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)

fun List<String>.move(target: String, afterId: String?) : List<String> {
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