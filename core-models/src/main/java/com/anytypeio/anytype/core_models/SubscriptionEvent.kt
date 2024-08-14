package com.anytypeio.anytype.core_models

/**
 * Events related to changes or transformations of objects.
 * @see ObjectWrapper.Basic
 */
sealed class SubscriptionEvent {

    interface Target {
        val target: Id
    }

    /**
     * @property [target] id of the object
     * @property [diff] slice of changes to apply to the object
     */
    data class Amend(
        override val target: Id,
        val diff: Map<Id, Any?>,
        val subscriptions: List<Id>
    ) : SubscriptionEvent(), Target
    /**
     * @property [target] id of the object
     * @property [keys] keys, whose values should be removed
     */
    data class Unset(
        override val target: Id,
        val keys: List<Id>,
        val subscriptions: List<Id>
    ) : SubscriptionEvent(), Target
    /**
     * @property [target] id of the object
     * @property [data] new set of data for the object
     */
    data class Set(
        override val target: Id,
        val data: Map<String, Any?>,
        val subscriptions: List<Id>
    ) : SubscriptionEvent(), Target

    /**
     * @property [target] id of the object removed from subscription results.
     */
    data class Remove(
        override val target: Id,
        val subscription: Id
    ) : SubscriptionEvent(), Target


    /**
     * @property [target] id of the object to move.
     * @property [afterId] id of the previous object in order, empty means first
     */
    data class Position(
        override val target: Id,
        val afterId: Id?
    ) : SubscriptionEvent(), Target

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
        override val target: Id,
        val afterId: Id?,
        val subscription: Id
    ) : SubscriptionEvent(), Target
}

data class Subscription(
    val objects: List<Id>
)