package com.agileburo.anytype.presentation.common

interface StateReducer<STATE, EVENT> {
    val function: suspend (STATE, EVENT) -> STATE
    suspend fun reduce(state: STATE, event: EVENT): STATE
}