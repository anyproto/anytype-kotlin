package com.anytypeio.anytype.core_models

enum class NetworkMode {
    DEFAULT, LOCAL, CUSTOM
}

data class NetworkModeConfig(
    val networkMode: NetworkMode = NetworkMode.DEFAULT,
    val userFilePath: String? = null,
    val storedFilePath: String? = null,
    val useReserveMultiplexLib: Boolean = false
)

object NetworkModeConst {
    const val NODE_STAGING_ID = "N9DU6hLkTAbvcpji3TCKPPd3UQWKGyzUxGmgJEyvhByqAjfD"
}
