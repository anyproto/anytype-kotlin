package com.anytypeio.anytype.core_utils.tools

import android.os.Looper
import javax.inject.Inject

interface ThreadInfo {
    fun isOnMainThread(): Boolean
}

class DefaultThreadInfo @Inject constructor() : ThreadInfo {
    override fun isOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}