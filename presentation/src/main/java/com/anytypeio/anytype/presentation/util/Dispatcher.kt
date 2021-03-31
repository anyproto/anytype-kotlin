package com.anytypeio.anytype.presentation.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 *  Event bus for passing data between dependent components.
 */
interface Dispatcher<T> {
    suspend fun send(t: T)
    fun flow(): Flow<T>

    class Default<T> : Dispatcher<T> {
        private val flow = MutableSharedFlow<T>()
        override suspend fun send(t: T) = flow.emit(t)
        override fun flow(): Flow<T> = flow
    }
}