package com.anytypeio.anytype.domain.clipboard

import com.anytypeio.anytype.domain.block.model.Block

interface Clipboard {
    /**
     * @param text plain text to put on the clipboard
     * @param html optional html to put on the clipboard
     * @param blocks Anytype blocks to store on the clipboard)
     */
    suspend fun put(text: String, html: String?, blocks: List<Block>)

    /**
     * @return Anytype blocks currently stored on (or linked to) the clipboard
     */
    suspend fun blocks() : List<Block>

    /**
     * @return return current clip on the clipboard
     */
    suspend fun clip() : Clip?

    interface UriMatcher {
        /**
         * Checks whether this [uri] is internal Anytype clipboard URI.
         */
        fun isAnytypeUri(uri: String) : Boolean
    }
}