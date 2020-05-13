package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.presentation.page.PageViewModel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * Reactive store
 * @param T stored type
 */
interface Store<T> {

    /**
     * @return streams of values
     */
    fun stream(): Flow<T>

    /**
     * @return current/last value
     */
    fun current(): T

    /**
     * Updates current values
     */
    suspend fun update(t: T)

    fun cancel()

    open class Conflated<T>(default: T) : Store<T> {

        private val channel = ConflatedBroadcastChannel(default)
        private val stream = channel.asFlow()

        override fun stream(): Flow<T> = stream
        override fun current(): T = channel.value
        override suspend fun update(t: T) = channel.send(t)
        override fun cancel() = channel.cancel()
    }

    class Focus : Conflated<String>(PageViewModel.EMPTY_FOCUS_ID)
    class Context : Conflated<String>("")

    class Details : Conflated<Block.Details>(Block.Details()) {
        suspend fun add(target: Id, fields: Block.Fields) {
            update(current().copy(details = current().details + mapOf(target to fields)))
        }
    }
}