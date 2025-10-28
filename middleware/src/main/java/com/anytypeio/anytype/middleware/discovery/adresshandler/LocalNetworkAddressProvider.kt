package com.anytypeio.anytype.middleware.discovery.adresshandler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import service.InterfaceGetter
import service.Service
import timber.log.Timber

/**
 * This class is used for sending local Ip addresses to middleware.
 * See https://discuss.ipfs.io/t/basichosts-updatelocalipaddr-fails-on-android-11-net-interfaceaddrs-returns-error/13003
 */
class LocalNetworkAddressProvider(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val interfaceProvider: InterfaceGetter
) {

    fun start() {
        scope.launch(dispatcher) {
            try {
                Service.setInterfaceGetter(interfaceProvider)
            } catch (e: NoClassDefFoundError) {
                // Service class initialization failed (likely fdsan/getaddrinfo crash in Go middleware)
                // This can happen on Android 14+ with certain OEM devices (e.g., Realme)
                Timber.e(e, "Service class initialization failed - P2P discovery disabled")
            } catch (e: UnsatisfiedLinkError) {
                // Native library loading failed
                Timber.e(e, "Failed to load native library for P2P discovery")
            } catch (e: Exception) {
                Timber.e(e, "Failed to set interface getter")
            } catch (e: Error) {
                // Catch any other native/linkage errors
                Timber.e(e, "Critical error setting interface getter - P2P discovery disabled")
            }
        }
    }
}