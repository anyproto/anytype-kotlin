package com.agileburo.anytype.data.auth.model

sealed class Response {
    sealed class Clipboard : Response() {
        class Paste(
            val cursor: Int,
            val isSameBlockCursor: Boolean,
            val blocks: List<String>,
            val payload: PayloadEntity
        ) : Clipboard()

        class Copy(
            val plain: String,
            val html: String?,
            val blocks: List<BlockEntity>
        ) : Clipboard()
    }

    sealed class Media : Response() {
        class Upload(
            val hash: String
        )
    }
}