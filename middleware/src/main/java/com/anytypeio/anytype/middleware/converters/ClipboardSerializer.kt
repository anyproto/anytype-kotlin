package com.anytypeio.anytype.middleware.converters

import anytype.clipboard.Clipboard
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.data.auth.mapper.Serializer
import com.anytypeio.anytype.middleware.mappers.toCoreModels
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel

class ClipboardSerializer : Serializer {

    override fun serialize(blocks: List<Block>): ByteArray {
        val models = blocks.map { it.toMiddlewareModel() }
        val clipboard = Clipboard(blocks = models)
        return Clipboard.ADAPTER.encode(clipboard)
    }

    override fun deserialize(blob: ByteArray): List<Block> {
        return Clipboard.ADAPTER.decode(blob).blocks.toCoreModels()
    }
}