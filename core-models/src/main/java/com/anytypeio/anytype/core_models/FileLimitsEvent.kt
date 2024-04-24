package com.anytypeio.anytype.core_models

sealed class FileLimitsEvent {
    data class SpaceUsage(
        val space: Id,
        val bytesUsage: Long
    ) : FileLimitsEvent()
    data class LocalUsage(val bytesUsage: Long) : FileLimitsEvent()
    data class FileLimitReached(
        val spaceId: String,
        val fileId: String
    ) : FileLimitsEvent()
    data class FileLimitUpdated(
        val bytesLimit: Long
    ) : FileLimitsEvent()
}