package com.anytypeio.anytype.middleware.discovery.adresshandler

import java.net.NetworkInterface
import javax.inject.Inject
import service.InterfaceGetter
import service.InterfaceIterator
import timber.log.Timber

class DefaultInterfaceProvider @Inject constructor() : InterfaceGetter {

    private var mLastUpdateTime: Long = 0
    private var interfaces = listOf<NetworkInterface>()

    override fun interfaces(): InterfaceIterator {
        return try {
            val update = fetchInterfaces()
            mLastUpdateTime = System.currentTimeMillis()
            DefaultInterfaceIterator(update.iterator()).also {
                interfaces = update
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting local net address")
            mLastUpdateTime = System.currentTimeMillis()
            DefaultInterfaceIterator(interfaces.iterator())
        }
    }

    private fun fetchInterfaces(): List<NetworkInterface> {
        return NetworkInterface.getNetworkInterfaces().toList()
    }
}