package com.anytypeio.anytype.core_models

sealed class Response {

    sealed class Clipboard : Response() {
        /**
         * Response for the use-case.
         * @param cursor caret position
         * @param blocks ids of the new blocks
         * @param isSameBlockCursor whether cursor stays at the same block.
         * @param payload response payload
         */
        data class Paste(
            val cursor: Int,
            val isSameBlockCursor: Boolean,
            val blocks: List<Id>,
            val payload: Payload
        ) : Clipboard()

        /**
         * @param text plain text
         * @param html optional html
         * @param blocks anytype clipboard slot
         */
        class Copy(
            val text: String,
            val html: String?,
            val blocks: List<Block>
        ) : Clipboard()
    }

    sealed class Media : Response() {
        class Upload(
            val hash: String
        )
    }

    sealed class Set : Response() {
        data class Create(
            val blockId: Id?,
            val targetId: Id,
            val payload: Payload
        )
    }

}

