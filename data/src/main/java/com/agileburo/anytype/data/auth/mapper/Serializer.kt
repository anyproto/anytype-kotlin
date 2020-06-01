package com.agileburo.anytype.data.auth.mapper

import com.agileburo.anytype.data.auth.model.BlockEntity

interface Serializer {
    fun serialize(blocks: List<BlockEntity>) : ByteArray
    fun deserialize(blob: ByteArray) : List<BlockEntity>
}