package com.anytypeio.anytype.middleware.discovery.adresshandler

import java.net.NetworkInterface
import service.InterfaceAddrIterator
import service.NetInterface

class LocalInterface(private val netInterface: NetworkInterface) : NetInterface {
    override fun flags(): Long {
        var flags = 0
        if (netInterface.isUp) {
            flags = flags or 1
        }
        if (netInterface.isLoopback) {
            flags = flags or 4
        }
        if (netInterface.isPointToPoint) {
            flags = flags or 8
        }
        if (netInterface.supportsMulticast()) {
            flags = flags or 16
        }
        return flags.toLong()
    }

    override fun hardwareAddr(): ByteArray {
        if (netInterface.hardwareAddress == null) {
            return ByteArray(0)
        }
        return netInterface.hardwareAddress
    }

    override fun index(): Long {
        return netInterface.index.toLong()
    }

    override fun interfaceAddrIter(): InterfaceAddrIterator {
        return DefaultAddressIterator(
            netInterface.interfaceAddresses.toList().iterator()
        )
    }

    override fun mtu(): Long {
        return netInterface.mtu.toLong()
    }

    override fun name(): String {
        return netInterface.name
    }
}