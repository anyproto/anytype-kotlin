package com.agileburo.anytype.data.auth.repo.clipboard

import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.ClipEntity

interface ClipboardDataStore {
    /**
     * Stores last copied Anytype blocks.
     * @see ClipEntity
     */
    interface Storage {
        fun persist(blocks: List<BlockEntity>)
        fun fetch() : List<BlockEntity>
    }

    /**
     * Provides access to system clipboard.
     */
    interface System {
        /**
         * Stores copied [text] and optionally a [html] representation on the systen clipboard.
         */
        suspend fun put(text: String, html: String?)

        /**
         * @return current clip on the clipboard.
         */
        suspend fun clip() : ClipEntity?
    }

    class Factory(
        val storage: Storage,
        val system: System
    )
}