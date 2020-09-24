package com.anytypeio.anytype.data.auth.mapper

import com.anytypeio.anytype.data.auth.model.BlockEntity

interface Serializer {
    fun serialize(blocks: List<BlockEntity>) : ByteArray
    fun deserialize(blob: ByteArray) : List<BlockEntity>
}