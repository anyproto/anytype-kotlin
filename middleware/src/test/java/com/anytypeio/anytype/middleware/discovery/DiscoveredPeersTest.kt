package com.anytypeio.anytype.middleware.discovery

import android.net.Network
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class DiscoveredPeersTest {

    private val peers = DiscoveredPeers()
    private val wifi = mock<Network>()
    private val ethernet = mock<Network>()

    @Test
    fun `peer is lost only when its last network disappears`() {
        val source = peers.found("peer", wifi).source
        peers.found("peer", ethernet)
        peers.updated(source, wifi, listOf("192.168.1.2"), 62654)
        peers.updated(source, ethernet, listOf("10.0.0.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertFalse(loss.peerLost)
        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), loss.publish)
        assertTrue(peers.lost("peer", ethernet)!!.peerLost)
        assertNull(peers.lost("peer", ethernet))
    }

    @Test
    fun `an update replaces the addresses of its network`() {
        val source = peers.found("peer", wifi).source
        peers.updated(source, wifi, listOf("192.168.1.2", "fe80::2%3"), 62654)

        val result = peers.updated(source, wifi, listOf("192.168.1.3"), 62654)

        assertEquals(DiscoveryResult("192.168.1.3", "peer", 62654), result)
    }

    @Test
    fun `an anonymous update replaces the anonymous addresses`() {
        val source = peers.found("peer", null).source
        peers.updated(source, null, listOf("192.168.1.2", "fe80::1%3"), 62654)

        val result = peers.updated(source, null, listOf("192.168.1.2", "fe80::2%3"), 62654)

        assertEquals(DiscoveryResult("192.168.1.2,fe80::2%3", "peer", 62654), result)
    }

    @Test
    fun `a known network update leaves with that network while the anonymous route stays`() {
        val source = peers.found("peer", wifi).source
        peers.found("peer", null)
        peers.updated(source, wifi, listOf("192.168.1.2"), 62654)
        peers.updated(source, null, listOf("10.0.0.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), loss.publish)
    }

    @Test
    fun `an update for a network that is not present yet is published once it is found`() {
        val source = peers.found("peer", null).source

        assertNull(peers.updated(source, ethernet, listOf("10.0.0.2"), 62654))
        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), peers.found("peer", ethernet).publish)
    }

    @Test
    fun `partial anonymous loss keeps the anonymous addresses until the refresh replaces them`() {
        val source = peers.found("peer", null).source
        peers.found("peer", null)
        peers.updated(source, null, listOf("192.168.1.2"), 62654)

        val loss = peers.lost("peer", null)!!

        assertFalse(loss.peerLost)
        assertNull(loss.publish)
        assertNull(peers.updated(source, null, listOf("172.16.0.9"), 62654))
        assertEquals(
            DiscoveryResult("10.0.0.2", "peer", 62654),
            peers.updated(loss.refresh!!, null, listOf("10.0.0.2"), 62654)
        )
    }

    @Test
    fun `anonymous sightings require matching losses`() {
        peers.found("peer", null)
        peers.found("peer", null)

        assertFalse(peers.lost("peer", null)!!.peerLost)
        assertTrue(peers.lost("peer", null)!!.peerLost)
        assertNull(peers.lost("peer", null))
    }

    @Test
    fun `a duplicate discovery on a known network needs a single loss`() {
        peers.found("peer", wifi)
        peers.found("peer", wifi)

        assertTrue(peers.lost("peer", wifi)!!.peerLost)
    }

    @Test
    fun `the latest port applies to every address`() {
        val source = peers.found("peer", wifi).source
        peers.found("peer", ethernet)
        peers.updated(source, wifi, listOf("192.168.1.2"), 4006)

        val result = peers.updated(source, ethernet, listOf("10.0.0.2"), 4007)

        assertEquals(DiscoveryResult("192.168.1.2,10.0.0.2", "peer", 4007), result)
        assertNull(peers.updated(source, wifi, listOf("192.168.1.2"), 4007))
    }

    @Test
    fun `losing the only resolved network retracts its addresses once`() {
        val source = peers.found("peer", wifi).source
        peers.found("peer", ethernet)
        peers.updated(source, wifi, listOf("192.168.1.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertFalse(loss.peerLost)
        assertEquals(DiscoveryResult("", "peer", 62654), loss.publish)
        assertNull(peers.updated(source, ethernet, emptyList(), 62654))
        assertEquals(
            DiscoveryResult("10.0.0.2", "peer", 62654),
            peers.updated(source, ethernet, listOf("10.0.0.2"), 62654)
        )
    }

    @Test
    fun `nothing is retracted before any address was published`() {
        peers.found("peer", wifi)
        peers.found("peer", ethernet)

        val loss = peers.lost("peer", wifi)!!

        assertFalse(loss.peerLost)
        assertNull(loss.publish)
    }

    @Test
    fun `an empty update removes the addresses of its network`() {
        val source = peers.found("peer", wifi).source
        peers.found("peer", ethernet)
        peers.updated(source, wifi, listOf("192.168.1.2"), 62654)
        peers.updated(source, ethernet, listOf("10.0.0.2"), 62654)

        val result = peers.updated(source, wifi, listOf(""), 62654)

        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), result)
    }

    @Test
    fun `an unchanged snapshot is not published again`() {
        val source = peers.found("peer", wifi).source
        peers.updated(source, wifi, listOf("192.168.1.2"), 62654)

        assertNull(peers.updated(source, wifi, listOf("192.168.1.2"), 62654))
        assertNull(peers.found("peer", wifi).publish)
    }

    @Test
    fun `addresses keep the order in which networks first reported them`() {
        val source = peers.found("peer", wifi).source
        peers.found("peer", ethernet)
        peers.updated(source, ethernet, listOf("10.0.0.2", "192.168.1.2"), 62654)

        val result = peers.updated(source, wifi, listOf("192.168.1.2", "fe80::2%3", ""), 62654)

        assertEquals(DiscoveryResult("10.0.0.2,192.168.1.2,fe80::2%3", "peer", 62654), result)
    }

    @Test
    fun `an update cannot revive a peer after its last network is lost`() {
        val source = peers.found("peer", wifi).source
        peers.lost("peer", wifi)

        assertFalse(peers.isCurrent(source))
        assertNull(peers.updated(source, wifi, listOf("192.168.1.2"), 62654))
    }

    @Test
    fun `rediscovery does not accept an update from the previous presence`() {
        val old = peers.found("peer", null).source
        peers.lost("peer", null)
        val current = peers.found("peer", null).source

        assertNull(peers.updated(old, null, listOf("192.168.1.1"), 62654))
        assertEquals(
            DiscoveryResult("192.168.1.2", "peer", 62654),
            peers.updated(current, null, listOf("192.168.1.2"), 62654)
        )
    }

    @Test
    fun `clearing a session invalidates pending updates even when the same peer returns`() {
        val old = peers.found("peer", wifi).source
        peers.clear()
        val current = peers.found("peer", wifi).source

        assertNull(peers.updated(old, wifi, listOf("192.168.1.1"), 62654))
        assertFalse(peers.isCurrent(old))
        assertTrue(peers.isCurrent(current))
    }

    @Test
    fun `unknown losses do not remove another network or peer`() {
        val source = peers.found("peer", wifi).source

        assertNull(peers.lost("peer", ethernet))
        assertNull(peers.lost("peer", null))
        assertNull(peers.lost("another-peer", wifi))
        assertTrue(peers.isCurrent(source))
        assertNotNull(peers.lost("peer", wifi))
    }
}
