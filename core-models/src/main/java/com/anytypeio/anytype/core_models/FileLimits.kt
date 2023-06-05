package com.anytypeio.anytype.core_models

data class FileLimits(
    val bytesUsage: Long?,
    val bytesLimit: Long?,
    val localBytesUsage: Long?
) {

    companion object {
        fun empty(): FileLimits = FileLimits(
            bytesUsage = null,
            bytesLimit = null,
            localBytesUsage = null
        )
    }
}
