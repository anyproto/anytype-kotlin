package com.anytypeio.anytype

import android.content.Context
import com.anytypeio.anytype.core_utils.tools.AppInfo
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions

class SentryCrashReporter(
    context: Context,
    appInfo: AppInfo
) {

    init {
        SentryAndroid.init(context) { options: SentryAndroidOptions ->
            options.release = appInfo.versionName
            options.environment = appInfo.sentryEnvironment.value
        }
    }

}