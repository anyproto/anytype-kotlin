package com.anytypeio.anytype

import android.content.Context
import com.anytypeio.anytype.core_utils.tools.AppInfo
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.android.timber.SentryTimberIntegration

class SentryCrashReporter(
    private val context: Context,
    private val appInfo: AppInfo,
) {
    fun init(
        withTimber: Boolean
    ) {
        SentryAndroid.init(context) { options: SentryAndroidOptions ->
            with(options) {
                release = appInfo.versionName
                environment = appInfo.sentryEnvironment.value
                if (withTimber) {
                    addIntegration(
                        SentryTimberIntegration(
                            minEventLevel = SentryLevel.ERROR,
                            minBreadcrumbLevel = SentryLevel.INFO
                        )
                    )
                }
            }
        }
    }
}