package com.anytypeio.anytype.middleware.discovery

import android.net.Network

internal class DiscoveredPeers {
    // Accessed only from NsdDiscoveryListener's event queue.
    private val peers = mutableMapOf<String, Peer>()

    // Identity is the generation: an update from an older source is rejected.
    class Source(val peerId: String)

    data class Found(val source: Source, val publish: DiscoveryResult?)

    data class Loss(
        val peerLost: Boolean,
        val publish: DiscoveryResult? = null,
        val refresh: Source? = null
    )

    private class Peer(var source: Source) {
        val networks = mutableSetOf<Network>()
        // Android does not identify a tethering interface, so its sightings are counted.
        var anonymousSightings = 0
        // Each update is the complete address set of one network. Insertion order is the
        // order in which the networks first reported an address.
        val endpoints = linkedMapOf<Network?, List<String>>()
        var port = 0
        var published: DiscoveryResult? = null
    }

    fun found(peerId: String, network: Network?): Found {
        val peer = peers.getOrPut(peerId) { Peer(Source(peerId)) }
        if (network == null) peer.anonymousSightings++ else peer.networks += network
        return Found(peer.source, publish(peer))
    }

    fun isCurrent(source: Source): Boolean = peers[source.peerId]?.source === source

    fun updated(source: Source, network: Network?, addresses: List<String>, port: Int): DiscoveryResult? {
        if (!isCurrent(source)) return null
        val peer = peers.getValue(source.peerId)
        val available = addresses.filter { it.isNotBlank() }.distinct()
        // Kept even before discovery reports the network: the platform does not order the
        // service callback against the discovery callback.
        if (available.isEmpty()) peer.endpoints.remove(network) else peer.endpoints[network] = available
        if (port > 0) peer.port = port
        return publish(peer)
    }

    fun lost(peerId: String, network: Network?): Loss? {
        val peer = peers[peerId] ?: return null
        var refresh: Source? = null
        if (network != null) {
            if (!peer.networks.remove(network)) return null
            peer.endpoints.remove(network)
        } else {
            if (peer.anonymousSightings == 0) return null
            peer.anonymousSightings--
            if (peer.anonymousSightings == 0) {
                peer.endpoints.remove(null)
            } else {
                // The loss cannot be attributed to one interface. Keep the last anonymous
                // addresses, reject older updates, and let a fresh query replace them.
                refresh = Source(peerId)
                peer.source = refresh
            }
        }
        if (peer.networks.isEmpty() && peer.anonymousSightings == 0) {
            peers.remove(peerId)
            return Loss(peerLost = true)
        }
        return Loss(peerLost = false, publish = publish(peer), refresh = refresh)
    }

    fun clear() {
        peers.clear()
    }

    private fun publish(peer: Peer): DiscoveryResult? {
        val present = peer.endpoints.filterKeys { key ->
            if (key == null) peer.anonymousSightings > 0 else key in peer.networks
        }
        val addresses = present.values.flatten().distinct()
        val next = when {
            addresses.isNotEmpty() -> DiscoveryResult(addresses.joinToString(","), peer.source.peerId, peer.port)
            // Heart keeps the last list until it gets another one: an empty list withdraws
            // the addresses of a lost network while the peer is still present elsewhere.
            peer.published?.ip?.isNotEmpty() == true -> DiscoveryResult("", peer.source.peerId, peer.port)
            else -> null
        }
        if (next == null || next == peer.published) return null
        peer.published = next
        return next
    }
}
