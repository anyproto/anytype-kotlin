package com.agileburo.anytype.data.auth.repo.clipboard

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.clipboard.Clip
import com.agileburo.anytype.domain.clipboard.Clipboard

class ClipboardDataRepository(
    private val factory: ClipboardDataStore.Factory
) : Clipboard {

    override suspend fun put(text: String, html: String?, blocks: List<Block>) {
        factory.storage.persist(
            blocks = blocks.map { it.toEntity() }
        )
        factory.system.put(
            text = text,
            html = html
        )
    }

    override suspend fun blocks(): List<Block> {
        return factory.storage.fetch().map { it.toDomain() }
    }

    override suspend fun clip(): Clip? = factory.system.clip()
}