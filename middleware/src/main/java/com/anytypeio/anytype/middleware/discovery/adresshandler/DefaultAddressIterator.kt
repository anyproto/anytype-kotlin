package com.anytypeio.anytype.middleware.discovery.adresshandler

import java.net.InterfaceAddress
import service.InterfaceAddrIterator

class DefaultAddressIterator(
    private val ia: Iterator<InterfaceAddress>
) : InterfaceAddrIterator {
    override fun next(): LocalInterfaceAddr? {
        if (ia.hasNext()) {
            return LocalInterfaceAddr(ia.next())
        }
        return null
    }
}