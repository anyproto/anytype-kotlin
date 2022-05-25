package com.anytypeio.anytype.domain.misc

interface Reducer<STATE, EVENT> {
    fun reduce(state: STATE, event: EVENT): STATE
}