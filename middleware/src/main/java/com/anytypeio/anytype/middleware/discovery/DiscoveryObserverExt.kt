package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import service.DiscoveryObserver

internal fun DiscoveryObserver.observeNsdError(errorCode: Int, message: String) {
    // Heart reserves 0 for a healthy registration, but NSD uses it for internal errors.
    val code = if (errorCode == NsdManager.FAILURE_INTERNAL_ERROR) -1L else errorCode.toLong()
    observeError(code, "$message (NsdManager errorCode=$errorCode)")
}
