package com.anytypeio.anytype.middleware.discovery

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import service.AndroidDiscoveryProxy
import service.DiscoveryObserver

class MDNSDelegate(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val resolver: MDNSResolver
) : AndroidDiscoveryProxy {

    private var isStarted = false

    @Volatile
    private var observer: DiscoveryObserver? = null

    fun start() {
        isStarted = true
        scope.launch(dispatcher) {
            observer?.let { observer ->
                resolver.start(observer)
            }
        }
    }

    fun stop() {
        isStarted = false
        scope.launch(dispatcher) {
            resolver.stop()
        }
    }

    override fun setObserver(localObserver: DiscoveryObserver?) {
        scope.launch(dispatcher) {
            observer = localObserver
            if (isStarted) start()
        }
    }

    override fun removeObserver() {
        scope.launch(dispatcher) {
            observer = null
            stop()
        }
    }
}