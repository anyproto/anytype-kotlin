package com.anytypeio.anytype.middleware.discovery

import android.annotation.TargetApi
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.util.concurrent.Executor

// Only instantiated on API 34+ or Android 13 with T extension 7. Keeping the callback type
// in a separate class avoids loading the newer NSD interface on legacy Android versions.
@TargetApi(34)
internal class NsdServiceInfoSubscription(
    private val nsdManager: NsdManager,
    service: NsdServiceInfo,
    executor: Executor,
    private val onUpdated: (NsdServiceInfo) -> Unit,
    private val onFailed: (Int) -> Unit,
    private val onUnregistered: () -> Unit
) : NsdManager.ServiceInfoCallback, AutoCloseable {

    // Callbacks and close run on the same discovery event queue.
    private var closed = false

    init {
        nsdManager.registerServiceInfoCallback(service, executor, this)
    }

    override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
        if (!closed) onUpdated(serviceInfo)
    }

    override fun onServiceLost() {
        // An unscoped subscription can observe several networks and this callback has no
        // network identity. DiscoveryListener is the authority for presence and generations.
    }

    override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
        if (!closed) {
            closed = true
            onFailed(errorCode)
        }
    }

    override fun onServiceInfoCallbackUnregistered() {
        closed = true
        onUnregistered()
    }

    override fun close() {
        if (!closed) {
            closed = true
            nsdManager.unregisterServiceInfoCallback(this)
        }
    }
}
