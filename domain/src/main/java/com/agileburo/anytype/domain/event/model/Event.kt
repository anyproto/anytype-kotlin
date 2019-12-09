package com.agileburo.anytype.domain.event.model

import com.agileburo.anytype.domain.block.model.Block

sealed class Event {

    abstract val contextId: String

    data class OnBlockAdded(
        override val contextId: String,
        val blocks: List<Block>
    ) : Event()

}