package com.anytypeio.anytype.middleware.discovery

import android.net.Network

internal class DiscoveredPeers {
    // Accessed only from NsdDiscoveryListener's event queue.
    private val peers = mutableMapOf<String, Peer>()

    // Identity is the generation of one network of a peer: a legacy resolve started for an
    // older sighting is rejected.
    class Sighting(val peerId: String, val network: Network?)

    data class Found(val sighting: Sighting, val publish: DiscoveryResult?)

    data class Loss(
        val peerLost: Boolean,
        val publish: DiscoveryResult? = null,
        val refresh: Sighting? = null
    )

    private class Peer(val id: String) {
        val networks = mutableMapOf<Network, Sighting>()
        // Android does not identify a tethering interface, so its sightings are counted.
        var anonymousSightings = 0
        var anonymous: Sighting? = null
        // Each report is the complete address set of one network. Insertion order is the
        // order in which the networks first reported an address.
        val endpoints = linkedMapOf<Network?, List<String>>()
        var port = 0
        var published: DiscoveryResult? = null
    }

    fun found(peerId: String, network: Network?): Found {
        val peer = peers.getOrPut(peerId) { Peer(peerId) }
        val sighting = if (network == null) {
            peer.anonymousSightings++
            peer.anonymous ?: Sighting(peerId, null).also { peer.anonymous = it }
        } else {
            peer.networks.getOrPut(network) { Sighting(peerId, network) }
        }
        return Found(sighting, publish(peer))
    }

    fun isCurrent(sighting: Sighting): Boolean {
        val peer = peers[sighting.peerId] ?: return false
        val current = if (sighting.network == null) peer.anonymous else peer.networks[sighting.network]
        return current === sighting
    }

    // A subscription update names its network. It can arrive before discovery reports that
    // network: the platform does not order the two callbacks.
    fun updated(peerId: String, network: Network?, addresses: List<String>, port: Int): DiscoveryResult? {
        val peer = peers[peerId] ?: return null
        return store(peer, network, addresses, port)
    }

    // A legacy resolve answers for the network of its sighting. An unscoped refresh can be
    // answered on any interface. An answer from a known network says nothing about the
    // anonymous route, and that network's own resolve owns its addresses.
    fun resolved(sighting: Sighting, network: Network?, addresses: List<String>, port: Int): DiscoveryResult? {
        if (!isCurrent(sighting)) return null
        if (sighting.network == null && network != null) return null
        return store(peers.getValue(sighting.peerId), sighting.network, addresses, port)
    }

    fun lost(peerId: String, network: Network?): Loss? {
        val peer = peers[peerId] ?: return null
        var refresh: Sighting? = null
        if (network != null) {
            peer.networks.remove(network) ?: return null
            peer.endpoints.remove(network)
        } else {
            if (peer.anonymousSightings == 0) return null
            peer.anonymousSightings--
            if (peer.anonymousSightings == 0) {
                peer.anonymous = null
                peer.endpoints.remove(null)
            } else {
                // The loss cannot be attributed to one interface. Keep the last anonymous
                // addresses, reject older anonymous resolves, and let a fresh query replace them.
                refresh = Sighting(peerId, null)
                peer.anonymous = refresh
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

    private fun store(peer: Peer, network: Network?, addresses: List<String>, port: Int): DiscoveryResult? {
        val available = addresses.filter { it.isNotBlank() }.distinct()
        if (available.isEmpty()) peer.endpoints.remove(network) else peer.endpoints[network] = available
        if (port > 0) peer.port = port
        return publish(peer)
    }

    private fun publish(peer: Peer): DiscoveryResult? {
        val present = peer.endpoints.filterKeys { key ->
            if (key == null) peer.anonymousSightings > 0 else key in peer.networks
        }
        val addresses = present.values.flatten().distinct()
        val next = when {
            addresses.isNotEmpty() -> DiscoveryResult(addresses.joinToString(","), peer.id, peer.port)
            // Heart keeps the last list until it gets another one: an empty list withdraws
            // the addresses of a lost network while the peer is still present elsewhere.
            peer.published?.ip?.isNotEmpty() == true -> DiscoveryResult("", peer.id, peer.port)
            else -> null
        }
        if (next == null || next == peer.published) return null
        peer.published = next
        return next
    }
}
