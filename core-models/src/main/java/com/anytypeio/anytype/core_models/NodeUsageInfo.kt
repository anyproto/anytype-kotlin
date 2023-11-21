package com.anytypeio.anytype.core_models

data class NodeUsageInfo(
    val nodeUsage: NodeUsage = NodeUsage.empty(),
    val spaces: List<SpaceUsage> = emptyList()
)

data class NodeUsage(
    var filesCount: Long?,
    var cidsCount: Long?,
    var bytesUsage: Long?,
    var bytesLeft: Long?,
    var bytesLimit: Long?,
    var localBytesUsage: Long?
) {
    companion object {
        fun empty() = NodeUsage(
            filesCount = null,
            cidsCount = null,
            bytesUsage = null,
            bytesLeft = null,
            bytesLimit = null,
            localBytesUsage = null
        )
    }
}

data class SpaceUsage(
    var space: Id,
    var filesCount: Long,
    var cidsCount: Long,
    var bytesUsage: Long
)