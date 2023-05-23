package com.anytypeio.anytype.core_models

data class FileLimits(
    val filesCount: Long?,
    val cidsCount: Long?,
    val bytesUsage: Long?,
    val bytesLeft: Long?,
    val bytesLimit: Long?,
    val localBytesUsage: Long?
) {

    companion object {
        fun empty(): FileLimits = FileLimits(
            filesCount = null,
            cidsCount = null,
            bytesUsage = null,
            bytesLeft = null,
            bytesLimit = null,
            localBytesUsage = null
        )
    }
}
