package com.anytypeio.anytype.core_utils.tools

import com.anytypeio.anytype.core_utils.BuildConfig
import javax.inject.Inject

interface AppInfo {
    val sentryEnvironment: SentryEnvironment
    val versionName: String
}

class DefaultAppInfo @Inject constructor(
    versionNameValue: String
): AppInfo {

    override val sentryEnvironment = if (BuildConfig.DEBUG) SentryEnvironment.DEV else SentryEnvironment.PROD

    override val versionName: String = versionNameValue

}

enum class SentryEnvironment(val value: String) {
    DEV("development"), PROD("production")
}