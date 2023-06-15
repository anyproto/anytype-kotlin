package com.anytypeio.anytype.app

import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.domain.platform.MetricsProvider

class DefaultMetricsProvider : MetricsProvider {
    override fun getVersion(): String {
        return if (BuildConfig.DEBUG)
            BuildConfig.VERSION_NAME + DEV_PREFIX
        else {
            return BuildConfig.VERSION_NAME
        }
    }

    override fun getPlatform(): String = PLATFORM_NAME

    companion object {
        const val PLATFORM_NAME = "kotlin"
        const val DEV_PREFIX = "-dev"
    }
}