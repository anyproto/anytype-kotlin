package com.anytypeio.anytype.middleware.discovery

import android.net.Network

internal class DiscoveredPeers {
    private val networksByPeer = mutableMapOf<String, MutableSet<Network?>>()

    @Synchronized
    fun found(peerId: String, network: Network?) {
        networksByPeer.getOrPut(peerId) { mutableSetOf() }.add(network)
    }

    // Heart expects a loss only after the peer disappears from every known network.
    @Synchronized
    fun lost(peerId: String, network: Network?): Boolean {
        val networks = networksByPeer[peerId] ?: return false
        if (!networks.remove(network) || networks.isNotEmpty()) return false
        networksByPeer.remove(peerId)
        return true
    }

    @Synchronized
    fun clear() {
        networksByPeer.clear()
    }
}
