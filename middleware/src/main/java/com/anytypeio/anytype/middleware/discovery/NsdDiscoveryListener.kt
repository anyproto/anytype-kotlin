package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
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

    fun registerObserver(observer: DiscoveryObserver) {
        try {
            this.observer = observer
        } catch (e: Exception) {
            Timber.e("Error while registering observer")
        }
    }

    fun unregisterObserver() {
        try {
            this.observer = null
        } catch (e: Exception) {
            Timber.e("Error while unregistering observer")
        }
    }

    override fun onDiscoveryStarted(regType: String) {
        Timber.d("Mdns discovery started with regType: $regType")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        scope.launch(dispatcher) {
            val observer = observer ?: return@launch
            resolveSemaphore.acquire()
            nsdManager.resolveService(service, ResolveListener(observer, resolveSemaphore))
        }
    }

    override fun onServiceLost(service: NsdServiceInfo) {
        Timber.d("Mdns discovery lost with service: $service")
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Timber.d("Mdns discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Timber.e("Mdns discovery start failed: $serviceType, error: $errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Timber.e("Mdns discovery stop failed: $serviceType, error: $errorCode")
    }
}