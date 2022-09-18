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
        val diff: Map<Id, Any?>,
        val subscriptions: List<Id>
    ) : SubscriptionEvent()
    /**
     * @property [target] id of the object
     * @property [keys] keys, whose values should be removed
     */
    data class Unset(
        val target: Id,
        val keys: List<Id>,
        val subscriptions: List<Id>
    ) : SubscriptionEvent()
    /**
     * @property [target] id of the object
     * @property [data] new set of data for the object
     */
    data class Set(
        val target: Id,
        val data: Map<String, Any?>,
        val subscriptions: List<Id>
    ) : SubscriptionEvent()

    /**
     * @property [target] id of the object removed from subscription results.
     */
    data class Remove(
        val target: Id,
        val subscription: Id
    ) : SubscriptionEvent()


    /**
     * @property [target] id of the object to move.
     * @property [afterId] id of the previous object in order, empty means first
     */
    data class Position(
        val target: Id,
        val afterId: Id?
    ) : SubscriptionEvent()

    /**
     * @property [counter] updated counter
     */
    data class Counter(
        val counter: SearchResult.Counter
    ) : SubscriptionEvent()

    /**
     * @property [target] id of the object to add.
     * @property [afterId] id of the previous object in order, empty means first
     */
    data class Add(
        val target: Id,
        val afterId: Id?,
        val subscription: Id
    ) : SubscriptionEvent()
}

data class Subscription(
    val objects: List<Id>
)