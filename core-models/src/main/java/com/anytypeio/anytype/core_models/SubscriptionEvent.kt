package com.anytypeio.anytype.core_models

/**
 * Events related to changes or transformations of objects.
 * @see ObjectWrapper.Basic
 */
sealed class SubscriptionEvent {
    /**
     * @property [target] id of the object
     * @property [diff] slice of changes to apply to the object
     */
    data class Amend(
        val target: Id,
        val diff: Map<Id, Any?>
    ) : SubscriptionEvent()
    /**
     * @property [target] id of the object
     * @property [keys] keys, whose values should be removed
     */
    data class Unset(
        val target: Id,
        val keys: List<Id>
    ) : SubscriptionEvent()
    /**
     * @property [target] id of the object
     * @property [data] new set of data for the object
     */
    data class Set(
        val target: Id,
        val data: Map<String, Any?>
    ) : SubscriptionEvent()
}