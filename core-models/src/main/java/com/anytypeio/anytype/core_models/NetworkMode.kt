package com.anytypeio.anytype.core_models

enum class NetworkMode {
    DEFAULT, LOCAL, CUSTOM
}

data class NetworkModeConfig(
    val networkMode: NetworkMode = NetworkMode.DEFAULT,
    val userFilePath: String? = null,
    val storedFilePath: String? = null
)
