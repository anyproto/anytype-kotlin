package com.anytypeio.anytype.middleware.discovery

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import service.AndroidDiscoveryProxy
import service.DiscoveryObserver
import timber.log.Timber

class MDNSDelegate(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val resolver: MDNSResolver
) : AndroidDiscoveryProxy {

    private var isStarted = false

    @Volatile
    private var observer: DiscoveryObserver? = null

    fun start() {
        try {
            isStarted = true
            scope.launch(dispatcher) {
                observer?.let { observer ->
                    resolver.start(observer)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while starting MDNS delegate")
        }
    }

    fun stop() {
        try {
            isStarted = false
            scope.launch(dispatcher) {
                resolver.stop()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while stopping MDNS delegate")
        }
    }

    override fun setObserver(localObserver: DiscoveryObserver?) {
        try {
            scope.launch(dispatcher) {
                observer = localObserver
                if (isStarted) start()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while setting observer")
        }
    }

    override fun removeObserver() {
        try {
            scope.launch(dispatcher) {
                observer = null
                stop()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while removing observer")
        }
    }
}