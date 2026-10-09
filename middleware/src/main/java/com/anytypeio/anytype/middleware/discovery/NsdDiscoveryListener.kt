package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import service.DiscoveryObserver
import timber.log.Timber

class NsdDiscoveryListener(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val nsdManager: NsdManager
) : NsdManager.DiscoveryListener {

    @Volatile
    private var observer: DiscoveryObserver? = null

    private val resolveSemaphore = Semaphore(1)
    private val discoveredPeers = DiscoveredPeers()

    fun registerObserver(observer: DiscoveryObserver) {
        try {
            discoveredPeers.clear()
            this.observer = observer
        } catch (e: Exception) {
            Timber.e("Error while registering observer")
        }
    }

    fun unregisterObserver() {
        try {
            this.observer = null
            discoveredPeers.clear()
        } catch (e: Exception) {
            Timber.e("Error while unregistering observer")
        }
    }

    override fun onDiscoveryStarted(regType: String) {
        Timber.d("Mdns discovery started with regType: $regType")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        val observer = observer ?: return
        discoveredPeers.found(service.serviceName, service.discoveryNetwork())
        scope.launch(dispatcher) {
            resolveSemaphore.acquire()
            if (this@NsdDiscoveryListener.observer !== observer) {
                resolveSemaphore.release()
                return@launch
            }
            nsdManager.resolveService(service, ResolveListener(observer, resolveSemaphore))
        }
    }

    override fun onServiceLost(service: NsdServiceInfo) {
        Timber.d("Mdns discovery lost with service: $service")
        val observer = observer ?: return
        if (discoveredPeers.lost(service.serviceName, service.discoveryNetwork())) {
            observer.observeLost(service.serviceName)
        }
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Timber.d("Mdns discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Timber.e("Mdns discovery start failed: $serviceType, error: $errorCode")
        observer?.observeNsdError(errorCode, "NSD discovery start failed: $serviceType")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Timber.e("Mdns discovery stop failed: $serviceType, error: $errorCode")
        observer?.observeNsdError(errorCode, "NSD discovery stop failed: $serviceType")
    }
}

private fun NsdServiceInfo.discoveryNetwork() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) network else null
