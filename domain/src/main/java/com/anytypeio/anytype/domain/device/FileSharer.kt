package com.anytypeio.anytype.domain.device

interface FileSharer {
    fun getPath(uri: String) : String?
    fun clear()
}