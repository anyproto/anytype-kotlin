package com.anytypeio.anytype.middleware.discovery

import android.annotation.SuppressLint
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.util.concurrent.Executor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import service.DiscoveryObserver
import timber.log.Timber

class NsdDiscoveryListener internal constructor(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val nsdManager: NsdManager,
    private val serviceInfoReader: NsdServiceInfoReader
) : NsdManager.DiscoveryListener {

    constructor(scope: CoroutineScope, dispatcher: CoroutineDispatcher, nsdManager: NsdManager) :
        this(scope, dispatcher, nsdManager, AndroidNsdServiceInfoReader())

    // State changes and reports to heart share one queue. A resolve cannot pass its generation
    // check, race a loss, and then publish stale addresses after observeLost.
    private val events = Channel<() -> Unit>(Channel.UNLIMITED)
    private var observer: DiscoveryObserver? = null
    private val resolveSemaphore = Semaphore(1)
    private val discoveredPeers = DiscoveredPeers()
    private val subscriptions = mutableMapOf<DiscoveredPeers.Resolution, AutoCloseable>()
    private val callbackExecutor = Executor { callback -> dispatch { callback.run() } }

    init {
        scope.launch(dispatcher) {
            try {
                for (event in events) {
                    try {
                        event()
                    } catch (e: Exception) {
                        Timber.e(e, "Error handling NSD event")
                    }
                }
            } finally {
                observer = null
                discoveredPeers.clear()
                closeObsoleteSubscriptions()
            }
        }.invokeOnCompletion { events.cancel() }
    }

    fun registerObserver(observer: DiscoveryObserver) {
        dispatch {
            discoveredPeers.clear()
            closeObsoleteSubscriptions()
            this.observer = observer
        }
    }

    fun unregisterObserver() {
        dispatch {
            observer = null
            discoveredPeers.clear()
            closeObsoleteSubscriptions()
        }
    }

    override fun onDiscoveryStarted(regType: String) {
        Timber.d("Mdns discovery started with regType: $regType")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        dispatch {
            if (observer == null) return@dispatch
            val resolution = discoveredPeers.found(service.serviceName, serviceInfoReader.network(service))
            closeObsoleteSubscriptions()
            resolve(service, resolution)
        }
    }

    override fun onServiceLost(service: NsdServiceInfo) {
        Timber.d("Mdns discovery lost with service: $service")
        dispatch {
            val observer = observer ?: return@dispatch
            val loss = discoveredPeers.lost(service.serviceName, serviceInfoReader.network(service))
                ?: return@dispatch
            closeObsoleteSubscriptions()
            if (loss.peerLost) {
                observer.observeLost(service.serviceName)
            } else {
                loss.remaining?.let { observer.observeChange(it) }
                loss.refresh?.let { resolve(serviceInfoReader.unscoped(service), it) }
            }
        }
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Timber.d("Mdns discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Timber.e("Mdns discovery start failed: $serviceType, error: $errorCode")
        dispatch { observer?.observeNsdError(errorCode, "NSD discovery start failed: $serviceType") }
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Timber.e("Mdns discovery stop failed: $serviceType, error: $errorCode")
        dispatch { observer?.observeNsdError(errorCode, "NSD discovery stop failed: $serviceType") }
    }

    @SuppressLint("NewApi") // Reader capability includes API 34 and the T extension 7 backport.
    private fun resolve(service: NsdServiceInfo, resolution: DiscoveredPeers.Resolution) {
        if (serviceInfoReader.supportsUpdates()) {
            if (subscriptions.containsKey(resolution)) return
            try {
                subscriptions[resolution] = NsdServiceInfoSubscription(
                    nsdManager = nsdManager,
                    // A null public network can still carry a hidden interface index. The shared
                    // anonymous subscription must be unbound from the very first sighting.
                    service = if (resolution.network == null) serviceInfoReader.unscoped(service) else service,
                    executor = callbackExecutor,
                    onUpdated = { onResolved(resolution, it) },
                    onFailed = { code ->
                        subscriptions.remove(resolution)
                        onResolveFailed(resolution, code)
                    },
                    onUnregistered = { subscriptions.remove(resolution) }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error subscribing to NSD service updates")
                onResolveFailed(resolution, NsdManager.FAILURE_INTERNAL_ERROR)
            }
            return
        }
        scope.launch(dispatcher) {
            resolveSemaphore.acquire()
            val submitted = dispatch {
                if (!discoveredPeers.isCurrent(resolution)) {
                    resolveSemaphore.release()
                    return@dispatch
                }
                try {
                    nsdManager.resolveService(
                        service,
                        ResolveListener(
                            semaphore = resolveSemaphore,
                            onResolved = { resolved -> dispatch { onResolved(resolution, resolved) } },
                            onFailed = { _, code -> dispatch { onResolveFailed(resolution, code) } }
                        )
                    )
                } catch (e: Exception) {
                    resolveSemaphore.release()
                    Timber.e(e, "Error starting NSD resolve")
                    onResolveFailed(resolution, NsdManager.FAILURE_INTERNAL_ERROR)
                }
            }
            if (!submitted) resolveSemaphore.release()
        }
    }

    private fun onResolved(resolution: DiscoveredPeers.Resolution, service: NsdServiceInfo) {
        val observer = observer ?: return
        if (!discoveredPeers.isCurrent(resolution)) return
        discoveredPeers.resolved(resolution, serviceInfoReader.addresses(service), service.port)
            ?.let { observer.observeChange(it) }
    }

    private fun onResolveFailed(resolution: DiscoveredPeers.Resolution, code: Int) {
        if (discoveredPeers.isCurrent(resolution)) {
            observer?.observeNsdError(code, "NSD service resolve failed: ${resolution.peerId}")
        }
    }

    private fun closeObsoleteSubscriptions() {
        val iterator = subscriptions.iterator()
        while (iterator.hasNext()) {
            val (resolution, subscription) = iterator.next()
            if (!discoveredPeers.isCurrent(resolution)) {
                iterator.remove()
                try {
                    subscription.close()
                } catch (e: Exception) {
                    Timber.e(e, "Error unsubscribing from NSD service updates")
                }
            }
        }
    }

    private fun dispatch(event: () -> Unit): Boolean = events.trySend(event).isSuccess
}
