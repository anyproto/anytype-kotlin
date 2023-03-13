package com.anytypeio.anytype.middleware.discovery

import service.ObservationResult

data class DiscoveryResult(val ip: String, val peerId: String, val port: Int) : ObservationResult {
    override fun ip(): String {
        return ip
    }

    override fun peerId(): String {
        return peerId
    }

    override fun port(): Long {
        return port.toLong()
    }
}