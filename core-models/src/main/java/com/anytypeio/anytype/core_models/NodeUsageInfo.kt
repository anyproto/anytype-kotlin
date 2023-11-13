package com.anytypeio.anytype.core_models

data class NodeUsageInfo(
    val nodeUsage: NodeUsage,
    val spaces: List<SpaceUsage>
)

data class NodeUsage(
    var filesCount: Long?,
    var cidsCount: Long?,
    var bytesUsage: Long?,
    var bytesLeft: Long?,
    var bytesLimit: Long?,
    var localBytesUsage: Long?
)

data class SpaceUsage(
    var spaceID: String,
    var filesCount: Long,
    var cidsCount: Long,
    var bytesUsage: Long
)



