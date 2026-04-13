package com.anytypeio.anytype.core_models

/**
 * Per-device preference for how aggressively the middleware should auto-download files.
 * Null limit means "no automatic downloads". A non-null limit expresses the upper bound
 * in bytes; [UNLIMITED_BYTES] signals no upper bound.
 *
 * Stored on disk by its [storageKey]. Never rename the storage keys without a migration.
 */
enum class FileDownloadLimit(val storageKey: String, val bytes: Long?) {
    OFF(storageKey = "off", bytes = null),
    MB_20(storageKey = "mb_20", bytes = 20L * 1024 * 1024),
    MB_100(storageKey = "mb_100", bytes = 100L * 1024 * 1024),
    MB_250(storageKey = "mb_250", bytes = 250L * 1024 * 1024),
    GB_1(storageKey = "gb_1", bytes = 1024L * 1024 * 1024),
    UNLIMITED(storageKey = "unlimited", bytes = Long.MAX_VALUE);

    companion object {
        /** Sentinel value meaning "no upper bound". */
        const val UNLIMITED_BYTES: Long = Long.MAX_VALUE

        /** Default value for new installations. */
        val DEFAULT: FileDownloadLimit = OFF

        /** Resolve a stored key back to an enum, falling back to [DEFAULT] for unknown keys. */
        fun fromStorageKey(key: String?): FileDownloadLimit =
            entries.firstOrNull { it.storageKey == key } ?: DEFAULT
    }
}
