package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import service.DiscoveryObserver
import timber.log.Timber

class MDNSResolver(
    private val nsdManager: NsdManager,
    wifiManager: WifiManager,
    private val discoveryListener: NsdDiscoveryListener,
    private val registrationListener: NsdRegistrationListener,
) {

    private val lock =
        wifiManager.createMulticastLock(MULTICAST_TAG).apply { setReferenceCounted(true) }

    @Volatile
    private var isStarted = false

    fun start(observer: DiscoveryObserver) {
        try {
            if (!isStarted) {
                isStarted = true

                lock.acquire()

                discoveryListener.registerObserver(observer)
                val serviceInfo = collectNsdServiceInfo(observer)

                nsdManager.apply {
                    registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
                    discoverServices(
                        serviceInfo.serviceType,
                        NsdManager.PROTOCOL_DNS_SD,
                        discoveryListener
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while starting MDNS Resolver")
        }
    }

    fun stop() {
        try {
            if (isStarted) {
                isStarted = false

                discoveryListener.unregisterObserver()

                nsdManager.apply {
                    unregisterService(registrationListener)
                    stopServiceDiscovery(discoveryListener)
                }

                lock.release()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while stopping MDNS resolver")
        }
    }

    private fun collectNsdServiceInfo(observer: DiscoveryObserver): NsdServiceInfo {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = observer.peerId()
            serviceType = observer.serviceType()
            port = observer.port().toInt()
        }
        return serviceInfo
    }

}

private const val MULTICAST_TAG = "Multicast"
