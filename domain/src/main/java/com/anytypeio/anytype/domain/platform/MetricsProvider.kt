package com.anytypeio.anytype.domain.platform

interface MetricsProvider {
    fun getVersion(): String
    fun getPlatform(): String
}