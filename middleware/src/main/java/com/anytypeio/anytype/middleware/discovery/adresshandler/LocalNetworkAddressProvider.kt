package com.anytypeio.anytype.middleware.discovery.adresshandler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import service.InterfaceGetter
import service.Service

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
            Service.setInterfaceGetter(interfaceProvider)
        }
    }
}