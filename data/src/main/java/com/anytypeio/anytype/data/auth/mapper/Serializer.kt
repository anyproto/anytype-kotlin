package com.anytypeio.anytype.data.auth.mapper

import com.anytypeio.anytype.core_models.Block

interface Serializer {
    fun serialize(blocks: List<Block>): ByteArray
    fun deserialize(blob: ByteArray): List<Block>
}