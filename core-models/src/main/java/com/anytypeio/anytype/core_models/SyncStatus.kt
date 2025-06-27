package com.anytypeio.anytype.core_models

enum class SyncStatus(val code: Int) {
    Synced(0),
    Syncing(1),
    Error(2),
    Queued(3);

    companion object {
        fun fromCode(code: Int): SyncStatus? = entries.find { it.code == code }
    }
}

enum class SyncError(val code: Int) {
    Null(0),
    IncompatibleVersion(2),
    NetworkError(3),
    Oversized(4);

    companion object {
        fun fromCode(code: Int): SyncError? = entries.find { it.code == code }
    }
}

fun ObjectView.syncStatus(target: Id): SyncStatus? {
    val wrapper = ObjectWrapper.Basic(details[target].orEmpty())
    if (!wrapper.isValid) return null

    val code = wrapper.getSingleValue<Double>(Relations.SYNC_STATUS)
    return code?.toInt()?.let(SyncStatus::fromCode)
}