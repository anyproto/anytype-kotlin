package com.anytypeio.anytype.core_models

sealed class FileLimitsEvent {
    data class SpaceUsage(val bytesUsage: Long) : FileLimitsEvent()
    data class LocalUsage(val bytesUsage: Long) : FileLimitsEvent()
    data class FileLimitReached(
        val spaceId: String,
        val fileId: String
    ) : FileLimitsEvent()
}