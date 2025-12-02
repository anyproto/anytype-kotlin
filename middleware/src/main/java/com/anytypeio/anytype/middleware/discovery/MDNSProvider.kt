package com.anytypeio.anytype.middleware.discovery

import javax.inject.Inject
import service.Service.setDiscoveryProxy
import timber.log.Timber

class MDNSProvider @Inject constructor(
    private val delegate: MDNSDelegate
) {

    fun setup() {
        try {
            setDiscoveryProxy(delegate)
        } catch (e: NoClassDefFoundError) {
            // Service class initialization failed (likely fdsan/getaddrinfo crash in Go middleware)
            // This can happen on Android 14+ with certain OEM devices
            Timber.e(e, "Service class not available - P2P discovery disabled")
        } catch (e: UnsatisfiedLinkError) {
            // Native library loading failed
            Timber.e(e, "Native library error - P2P discovery disabled")
        } catch (e: Error) {
            // Catch any other native/linkage errors to prevent app crash
            Timber.e(e, "Critical error in MDNS setup - P2P discovery disabled")
        } catch (e: Exception) {
            // Catch any other exceptions
            Timber.e(e, "Failed to setup discovery proxy - P2P discovery disabled")
        }
    }

    fun start() {
        try {
            delegate.start()
        } catch (e: Error) {
            Timber.e(e, "Critical error starting MDNS discovery")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start MDNS discovery")
        }
    }

    fun stop() {
        try {
            delegate.stop()
        } catch (e: Error) {
            Timber.e(e, "Critical error stopping MDNS discovery")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop MDNS discovery")
        }
    }

}

