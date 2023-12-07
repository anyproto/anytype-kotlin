package com.anytypeio.anytype.core_models

data class NetworkModeConfig(
    val networkMode: NetworkMode,
    val userFilePath: String?,
    val storedFilePath: String?
)
