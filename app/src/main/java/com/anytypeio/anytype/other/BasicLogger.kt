package com.anytypeio.anytype.other

import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import timber.log.Timber

class BasicLogger @Inject constructor() : Logger {
    override fun logWarning(msg: String) {
        Timber.w(msg)
    }

    override fun logException(e: Exception) {
        Timber.e(e)
    }
}