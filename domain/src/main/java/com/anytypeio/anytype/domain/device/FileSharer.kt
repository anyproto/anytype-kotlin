package com.anytypeio.anytype.domain.device

interface FileSharer {
    suspend fun getPath(uri: String) : String?
    suspend fun getDisplayName(uri: String) : String?
    suspend fun clear()
}