package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.sync.Semaphore
import service.DiscoveryObserver
import timber.log.Timber

class ResolveListener(
    private val observer: DiscoveryObserver,
    private val semaphore: Semaphore
) : NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        try {
            semaphore.release()
        } catch (e: Exception) {
            Timber.e(e, "Error while releasing semaphore")
        }
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Timber.d("Mdns discovery resolve succeed: $serviceInfo")
        try {
            observer.observeChange(
                DiscoveryResult(
                    serviceInfo.host.hostAddress.orEmpty(),
                    serviceInfo.serviceName,
                    serviceInfo.port
                )
            )
            semaphore.release()
        } catch (e: Exception) {
            Timber.e(e, "Error after onServiceResolved")
        }
    }
}