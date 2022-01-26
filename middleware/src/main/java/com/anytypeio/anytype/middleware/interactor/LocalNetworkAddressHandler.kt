package com.anytypeio.anytype.middleware.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import service.InterfaceAddr
import service.InterfaceAddrIterator
import service.InterfaceAddrsGetter
import service.Service.setInterfaceAddrsGetter
import timber.log.Timber
import java.net.InterfaceAddress
import java.net.NetworkInterface

/**
 * This class is used for sending local Ip addresses to middleware.
 * See https://discuss.ipfs.io/t/basichosts-updatelocalipaddr-fails-on-android-11-net-interfaceaddrs-returns-error/13003
 */
class LocalNetworkAddressHandler(
    private val scope: CoroutineScope = GlobalScope
) {

    fun start() {
        scope.launch(Dispatchers.IO) {
            setInterfaceAddrsGetter(DefaultAddressProvider())
        }
    }

    class DefaultAddressProvider : InterfaceAddrsGetter {

        private var mLastUpdateTime: Long = 0
        private var addresses = mutableListOf<InterfaceAddress>()

        override fun interfaceAddrs(): InterfaceAddrIterator {
            Timber.d("Getting addresses")
            if (!isNeedToUpdateNetworkAddresses()) {
                return DefaultAddressIterator(addresses.iterator())
            }
            return try {
                addresses.clear()
                val interfaces = NetworkInterface.getNetworkInterfaces().toList()
                addresses.addAll(interfaces.flatMap { it.interfaceAddresses })
                mLastUpdateTime = System.currentTimeMillis()
                DefaultAddressIterator(addresses.iterator())
            } catch (e: Exception) {
                Timber.e(e, "Error getting local net address")
                mLastUpdateTime = System.currentTimeMillis()
                DefaultAddressIterator(addresses.iterator())
            }
        }

        private fun isNeedToUpdateNetworkAddresses(): Boolean {
            return System.currentTimeMillis() - mLastUpdateTime >= UPDATE_INTERVAL_MILLI_SECONDS
        }
    }

    class DefaultAddressIterator(private val ia: Iterator<InterfaceAddress>) :
        InterfaceAddrIterator {
        override fun next(): LocalInterfaceAddr? {
            if (ia.hasNext()) {
                return LocalInterfaceAddr(ia.next())
            }
            return null
        }
    }

    class LocalInterfaceAddr(private val interfaceAddress: InterfaceAddress?) : InterfaceAddr {

        override fun ip(): ByteArray? {
            return interfaceAddress?.address?.address
        }

        override fun prefix(): Long {
            return interfaceAddress?.networkPrefixLength?.toLong() ?: 0L
        }
    }

    companion object {
        const val UPDATE_INTERVAL_MILLI_SECONDS = 180000
    }
}