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

    // State changes and reports to heart share one queue. A report cannot pass its validity
    // check, race a loss, and then publish stale addresses after observeLost.
    private val events = Channel<() -> Unit>(Channel.UNLIMITED)
    private var observer: DiscoveryObserver? = null
    private val resolveSemaphore = Semaphore(1)
    private val discoveredPeers = DiscoveredPeers()
    // One subscription per peer id. It reports the peer on every network.
    private val subscriptions = mutableMapOf<String, AutoCloseable>()
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
                closeSubscriptions()
            }
        }.invokeOnCompletion { events.cancel() }
    }

    fun registerObserver(observer: DiscoveryObserver) {
        dispatch {
            discoveredPeers.clear()
            closeSubscriptions()
            this.observer = observer
        }
    }

    fun unregisterObserver() {
        dispatch {
            observer = null
            discoveredPeers.clear()
            closeSubscriptions()
        }
    }

    override fun onDiscoveryStarted(regType: String) {
        Timber.d("Mdns discovery started with regType: $regType")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        dispatch {
            val observer = observer ?: return@dispatch
            val found = discoveredPeers.found(service.serviceName, serviceInfoReader.network(service))
            found.publish?.let { observer.observeChange(it) }
            if (serviceInfoReader.supportsUpdates()) {
                subscribe(service)
            } else {
                resolve(service, found.sighting)
            }
        }
    }

    override fun onServiceLost(service: NsdServiceInfo) {
        Timber.d("Mdns discovery lost with service: $service")
        dispatch {
            val observer = observer ?: return@dispatch
            val loss = discoveredPeers.lost(service.serviceName, serviceInfoReader.network(service))
                ?: return@dispatch
            if (loss.peerLost) {
                closeSubscription(service.serviceName)
                observer.observeLost(service.serviceName)
                return@dispatch
            }
            loss.publish?.let { observer.observeChange(it) }
            loss.refresh?.let { refresh ->
                if (serviceInfoReader.supportsUpdates()) {
                    // A new subscription replays the current records of every network.
                    closeSubscription(service.serviceName)
                    subscribe(service)
                } else {
                    resolve(serviceInfoReader.unscoped(service), refresh)
                }
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
    private fun subscribe(service: NsdServiceInfo) {
        val peerId = service.serviceName
        if (subscriptions.containsKey(peerId)) return
        try {
            subscriptions[peerId] = NsdServiceInfoSubscription(
                nsdManager = nsdManager,
                // A discovered service can carry a hidden interface index that binds the
                // subscription to that interface. The peer's subscription must stay unbound.
                service = serviceInfoReader.unscoped(service),
                executor = callbackExecutor,
                // A closed subscription ignores its callbacks: an update comes from the live one.
                onUpdated = { update -> onUpdated(peerId, update) },
                onFailed = { code ->
                    // A failure arrives only while the subscription is open: the entry is this one.
                    subscriptions.remove(peerId)
                    reportResolveFailure(peerId, code)
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error subscribing to NSD service updates")
            reportResolveFailure(peerId, NsdManager.FAILURE_INTERNAL_ERROR)
        }
    }

    private fun resolve(service: NsdServiceInfo, sighting: DiscoveredPeers.Sighting) {
        scope.launch(dispatcher) {
            resolveSemaphore.acquire()
            val submitted = dispatch {
                if (!discoveredPeers.isCurrent(sighting)) {
                    resolveSemaphore.release()
                    return@dispatch
                }
                try {
                    nsdManager.resolveService(
                        service,
                        ResolveListener(
                            semaphore = resolveSemaphore,
                            onResolved = { resolved -> dispatch { onResolved(sighting, resolved) } },
                            onFailed = { _, code -> dispatch { onResolveFailed(sighting, code) } }
                        )
                    )
                } catch (e: Exception) {
                    resolveSemaphore.release()
                    Timber.e(e, "Error starting NSD resolve")
                    onResolveFailed(sighting, NsdManager.FAILURE_INTERNAL_ERROR)
                }
            }
            if (!submitted) resolveSemaphore.release()
        }
    }

    // Each subscription update describes one network, and the update itself names it.
    private fun onUpdated(peerId: String, update: NsdServiceInfo) {
        val observer = observer ?: return
        discoveredPeers.updated(
            peerId,
            serviceInfoReader.network(update),
            serviceInfoReader.addresses(update),
            update.port
        )?.let { observer.observeChange(it) }
    }

    private fun onResolved(sighting: DiscoveredPeers.Sighting, resolved: NsdServiceInfo) {
        val observer = observer ?: return
        discoveredPeers.resolved(
            sighting,
            serviceInfoReader.network(resolved),
            serviceInfoReader.addresses(resolved),
            resolved.port
        )?.let { observer.observeChange(it) }
    }

    private fun onResolveFailed(sighting: DiscoveredPeers.Sighting, code: Int) {
        if (discoveredPeers.isCurrent(sighting)) reportResolveFailure(sighting.peerId, code)
    }

    private fun reportResolveFailure(peerId: String, code: Int) {
        observer?.observeNsdError(code, "NSD service resolve failed: $peerId")
    }

    private fun closeSubscription(peerId: String) {
        subscriptions.remove(peerId)?.let(::closeQuietly)
    }

    private fun closeSubscriptions() {
        subscriptions.values.forEach(::closeQuietly)
        subscriptions.clear()
    }

    private fun closeQuietly(subscription: AutoCloseable) {
        try {
            subscription.close()
        } catch (e: Exception) {
            Timber.e(e, "Error unsubscribing from NSD service updates")
        }
    }

    private fun dispatch(event: () -> Unit): Boolean = events.trySend(event).isSuccess
}
