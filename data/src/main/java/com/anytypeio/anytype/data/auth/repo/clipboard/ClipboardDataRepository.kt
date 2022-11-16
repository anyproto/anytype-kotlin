package com.anytypeio.anytype.data.auth.repo.clipboard

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.clipboard.Clip
import com.anytypeio.anytype.domain.clipboard.Clipboard

class ClipboardDataRepository(
    private val factory: ClipboardDataStore.Factory
) : Clipboard {

    override suspend fun put(text: String, html: String?, blocks: List<Block>) {
        factory.storage.persist(
            blocks = blocks
        )
        factory.system.put(
            text = text,
            html = html,
            ignoreHtml = true
        )
    }

    override suspend fun blocks(): List<Block> {
        return factory.storage.fetch()
    }

    override suspend fun clip(): Clip? = factory.system.clip()
}