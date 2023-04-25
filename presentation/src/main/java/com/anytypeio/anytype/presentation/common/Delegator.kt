package com.anytypeio.anytype.presentation.common

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface Delegator<T> {
    val channel : SharedFlow<T>
    suspend fun delegate(action: T)
    suspend fun receive() : Flow<T> = channel
    class Default<T> : Delegator<T> {
        override val channel = MutableSharedFlow<T>()
        override suspend fun delegate(action: T) {
            channel.emit(action)
        }
    }
}

sealed class Action {
    data class SetUnsplashImage(val img: Id) : Action()
    object SearchOnPage: Action()
    object UndoRedo : Action()
    data class OpenObject(val id: Id) : Action()
    data class OpenCollection(val id: Id) : Action()
    data class Duplicate(val id: Id) : Action()
}