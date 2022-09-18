package com.anytypeio.anytype.middleware.mappers

import anytype.Event
import com.anytypeio.anytype.core_models.SearchResult

fun Event.Object.Subscription.Counters.parse() : SearchResult.Counter = SearchResult.Counter(
    total = total.toInt(),
    prev = prevCount.toInt(),
    next = nextCount.toInt()
)