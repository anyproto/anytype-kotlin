package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.presentation.page.model.TextUpdate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

interface Proxy<T> {

    /**
     * @return streams of values
     */
    fun stream(): Flow<T>

    /**
     * Updates current values
     */
    suspend fun send(t: T)

    fun cancel()

    open class Subject<T> : Proxy<T> {

        private val channel = Channel<T>()
        private val stream = channel.consumeAsFlow()

        override fun stream(): Flow<T> = stream
        override suspend fun send(t: T) = channel.send(t)
        override fun cancel() = channel.cancel()
    }

    sealed class Text : Subject<TextUpdate?>() {
        class Changes : Text()
        class Saves : Text()
    }

    class Intents : Subject<Intent>()

    class Payloads : Subject<Payload>()

    class Error : Subject<Throwable>()
}