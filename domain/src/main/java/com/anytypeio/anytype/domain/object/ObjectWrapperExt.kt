package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper

/**
 * Function for applying granular changes in object, replacing existing values with the new ones.
 * @param [diff] difference
 */
fun ObjectWrapper.Basic.amend(diff: Map<Id, Any?>) = ObjectWrapper.Basic(map + diff)
/**
 * Function for applying granular changes in object.
 */
fun ObjectWrapper.Basic.unset(keys: List<Id>) = ObjectWrapper.Basic(
    map.toMutableMap().apply {
        keys.forEach { k -> remove(k) }
    }
)
