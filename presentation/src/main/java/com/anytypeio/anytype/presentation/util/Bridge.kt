package com.anytypeio.anytype.presentation.util

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * Event bus for passing data between view models.
 */
class Bridge<T> {
    private val channel = BroadcastChannel<T>(1)
    suspend fun send(t: T) = channel.send(t)
    fun flow(): Flow<T> = channel.asFlow()
}