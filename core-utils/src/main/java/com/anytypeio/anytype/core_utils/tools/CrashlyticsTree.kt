package com.anytypeio.anytype.core_utils.tools

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority)
        crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)
        if (tag != null) crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag)

        if (t == null) {
            crashlytics.recordException(Exception(message))
        } else {
            crashlytics.recordException(t)
        }
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}