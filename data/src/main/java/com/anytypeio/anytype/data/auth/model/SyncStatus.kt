package com.anytypeio.anytype.data.auth.model

enum class SyncStatusEntity {
    UNKNOWN,
    OFFLINE,
    SYNCING,
    SYNCED,
    FAILED,
    INCOMPATIBLE_VERSION
}