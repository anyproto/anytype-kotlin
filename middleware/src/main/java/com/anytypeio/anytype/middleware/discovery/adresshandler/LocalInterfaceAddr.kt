package com.anytypeio.anytype.middleware.discovery.adresshandler

import java.net.InterfaceAddress
import service.InterfaceAddr

class LocalInterfaceAddr(private val interfaceAddress: InterfaceAddress?) : InterfaceAddr {

    override fun ip(): ByteArray? {
        return interfaceAddress?.address?.address
    }

    override fun prefix(): Long {
        return interfaceAddress?.networkPrefixLength?.toLong() ?: 0L
    }
}