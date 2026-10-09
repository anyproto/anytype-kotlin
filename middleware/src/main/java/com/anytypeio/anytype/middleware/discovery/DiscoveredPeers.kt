package com.anytypeio.anytype.middleware.discovery

import android.net.Network

internal class DiscoveredPeers {
    // Accessed only from NsdDiscoveryListener's event queue.
    private val networksByPeer = mutableMapOf<String, MutableMap<Network?, Presence>>()

    // Identity is the generation: rediscovery must never accept an older resolve.
    class Resolution(val peerId: String, val network: Network?)

    data class Loss(
        val peerLost: Boolean,
        val remaining: DiscoveryResult? = null,
        val refresh: Resolution? = null
    )

    private class Presence(
        var resolution: Resolution,
        var sightings: Int = 1,
        var addresses: Set<String> = emptySet(),
        var port: Int = 0
    )

    fun found(peerId: String, network: Network?): Resolution {
        val networks = networksByPeer.getOrPut(peerId) { mutableMapOf() }
        val previous = networks[network]
        if (network == null && previous != null) {
            // Legacy NSD loses the interface identity, but still sends one add/remove per sighting.
            previous.sightings++
            return previous.resolution
        }
        val resolution = Resolution(peerId, network)
        networks[network] = Presence(
            resolution = resolution,
            addresses = previous?.addresses.orEmpty(),
            port = previous?.port ?: 0
        )
        return resolution
    }

    fun isCurrent(resolution: Resolution): Boolean =
        networksByPeer[resolution.peerId]?.get(resolution.network)?.resolution === resolution

    fun resolved(resolution: Resolution, addresses: List<String>, port: Int): DiscoveryResult? {
        if (!isCurrent(resolution)) return null
        val presence = networksByPeer.getValue(resolution.peerId).getValue(resolution.network)
        val available = addresses.filter { it.isNotBlank() }.toSet()
        presence.addresses = if (resolution.network == null) {
            presence.addresses + available
        } else {
            available
        }
        presence.port = port
        return snapshot(resolution.peerId)
    }

    fun lost(peerId: String, network: Network?): Loss? {
        val networks = networksByPeer[peerId] ?: return null
        val presence = networks[network] ?: return null
        var refresh: Resolution? = null
        if (network == null && presence.sightings > 1) {
            presence.sightings--
            // We cannot attribute an anonymous loss to one address set. Keep the conservative
            // union until the last sighting is gone: clearing it can discard a surviving route.
            // Invalidate pending resolves and query again to learn any new reachable addresses.
            refresh = Resolution(peerId, null)
            presence.resolution = refresh
        } else {
            networks.remove(network)
        }
        if (networks.isEmpty()) {
            networksByPeer.remove(peerId)
            return Loss(peerLost = true)
        }
        return Loss(peerLost = false, remaining = snapshot(peerId), refresh = refresh)
    }

    fun clear() {
        networksByPeer.clear()
    }

    private fun snapshot(peerId: String): DiscoveryResult? {
        val resolved = networksByPeer[peerId]?.values?.filter { it.addresses.isNotEmpty() }.orEmpty()
        if (resolved.isEmpty()) return null
        return DiscoveryResult(
            ip = resolved.flatMap { it.addresses }.distinct().joinToString(","),
            peerId = peerId,
            port = resolved.first().port
        )
    }
}
