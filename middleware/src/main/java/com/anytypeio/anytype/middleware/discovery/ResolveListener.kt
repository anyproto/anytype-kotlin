package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.sync.Semaphore
import service.DiscoveryObserver
import timber.log.Timber

class ResolveListener(private val observer: DiscoveryObserver, private val resolved: Semaphore) :
    NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Timber.e("Mdns discovery resolve failed: $serviceInfo, error: $errorCode")
        resolved.release()
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Timber.e("Mdns discovery resolve succeed: $serviceInfo")
        observer.observeChange(
            DiscoveryResult(
                serviceInfo.host.hostAddress ?: "",
                serviceInfo.serviceName,
                serviceInfo.port
            )
        )
        resolved.release()
    }
}