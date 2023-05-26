package com.anytypeio.anytype

import android.content.Context
import com.anytypeio.anytype.core_utils.tools.AppInfo
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.android.timber.SentryTimberIntegration
import io.sentry.protocol.User
import java.lang.Exception
import timber.log.Timber

class SentryCrashReporter(
    private val context: Context,
    private val appInfo: AppInfo,
    private val withTimber: Boolean
) : CrashReporter {

    override fun init() {
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

    override fun setUser(userId: String) {
        try {
            val user = User().apply {
                id = userId
            }
            Sentry.setUser(user)
        } catch (e: Exception) {
            Timber.e(e, "Sentry set user error")
        }
    }
}