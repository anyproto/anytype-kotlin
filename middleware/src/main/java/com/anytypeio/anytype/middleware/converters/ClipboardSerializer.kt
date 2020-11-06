package com.anytypeio.anytype.middleware.converters

import anytype.clipboard.Clipboard
import com.anytypeio.anytype.data.auth.mapper.Serializer
import com.anytypeio.anytype.data.auth.model.BlockEntity

class ClipboardSerializer : Serializer {

    override fun serialize(blocks: List<BlockEntity>): ByteArray {
        val models = blocks.map { it.block() }
        val clipboard = Clipboard(blocks = models)
        return Clipboard.ADAPTER.encode(clipboard)
    }

    override fun deserialize(blob: ByteArray): List<BlockEntity> {
        return Clipboard.ADAPTER.decode(blob).blocks.blocks()
    }
}