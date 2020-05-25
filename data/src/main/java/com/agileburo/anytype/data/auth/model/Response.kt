package com.agileburo.anytype.data.auth.model

sealed class Response {
    sealed class Clipboard : Response() {
        data class Paste(
            val cursor: Int,
            val blocks: List<String>,
            val payload: PayloadEntity
        ) : Clipboard()
    }
}