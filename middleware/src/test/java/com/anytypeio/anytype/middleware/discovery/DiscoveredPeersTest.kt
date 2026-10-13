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
        peers.found("peer", wifi)
        peers.found("peer", ethernet)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)
        peers.updated("peer", ethernet, listOf("10.0.0.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertFalse(loss.peerLost)
        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), loss.publish)
        assertTrue(peers.lost("peer", ethernet)!!.peerLost)
        assertNull(peers.lost("peer", ethernet))
    }

    @Test
    fun `an update replaces the addresses of its network`() {
        peers.found("peer", wifi)
        peers.updated("peer", wifi, listOf("192.168.1.2", "fe80::2%3"), 62654)

        val result = peers.updated("peer", wifi, listOf("192.168.1.3"), 62654)

        assertEquals(DiscoveryResult("192.168.1.3", "peer", 62654), result)
    }

    @Test
    fun `an anonymous update replaces the anonymous addresses`() {
        peers.found("peer", null)
        peers.updated("peer", null, listOf("192.168.1.2", "fe80::1%3"), 62654)

        val result = peers.updated("peer", null, listOf("192.168.1.2", "fe80::2%3"), 62654)

        assertEquals(DiscoveryResult("192.168.1.2,fe80::2%3", "peer", 62654), result)
    }

    @Test
    fun `a known network update leaves with that network while the anonymous route stays`() {
        peers.found("peer", wifi)
        peers.found("peer", null)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)
        peers.updated("peer", null, listOf("10.0.0.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), loss.publish)
    }

    @Test
    fun `an update for a network that is not present yet is published once it is found`() {
        peers.found("peer", null)

        assertNull(peers.updated("peer", ethernet, listOf("10.0.0.2"), 62654))
        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), peers.found("peer", ethernet).publish)
    }

    @Test
    fun `an update for an unknown peer is ignored`() {
        assertNull(peers.updated("peer", wifi, listOf("192.168.1.2"), 62654))
        assertNull(peers.found("peer", wifi).publish)
    }

    @Test
    fun `a lost network found again waits for its next report`() {
        peers.found("peer", wifi)
        peers.found("peer", null)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)
        peers.updated("peer", null, listOf("10.0.0.2"), 62654)

        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), peers.lost("peer", wifi)!!.publish)
        assertNull(peers.found("peer", wifi).publish)
        assertEquals(DiscoveryResult("", "peer", 62654), peers.lost("peer", null)!!.publish)
        assertNull(peers.found("peer", null).publish)
    }

    @Test
    fun `the last anonymous loss drops the anonymous addresses`() {
        peers.found("peer", wifi)
        peers.found("peer", null)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)
        peers.updated("peer", null, listOf("10.0.0.2"), 62654)

        assertEquals(DiscoveryResult("192.168.1.2", "peer", 62654), peers.lost("peer", null)!!.publish)
        assertNull(peers.found("peer", null).publish)
    }

    @Test
    fun `a legacy resolve answers for the sighting it was started for`() {
        val sighting = peers.found("peer", wifi).sighting

        val result = peers.resolved(sighting, ethernet, listOf("192.168.1.2"), 62654)

        assertEquals(DiscoveryResult("192.168.1.2", "peer", 62654), result)
        assertTrue(peers.lost("peer", wifi)!!.peerLost)
    }

    @Test
    fun `an unscoped resolve answered from a known network changes nothing`() {
        val known = peers.found("peer", wifi).sighting
        val anonymous = peers.found("peer", null).sighting
        peers.resolved(known, null, listOf("192.168.1.2"), 62654)
        peers.resolved(anonymous, null, listOf("10.0.0.2"), 62654)

        assertNull(peers.resolved(anonymous, wifi, listOf("192.168.1.9"), 62654))
        assertEquals(DiscoveryResult("192.168.1.2", "peer", 62654), peers.lost("peer", null)!!.publish)
    }

    @Test
    fun `a resolve for a lost network is rejected even after the network returns`() {
        peers.found("peer", ethernet)
        val old = peers.found("peer", wifi).sighting
        peers.lost("peer", wifi)

        assertNull(peers.resolved(old, null, listOf("192.168.1.2"), 62654))
        val current = peers.found("peer", wifi).sighting
        assertNull(peers.resolved(old, null, listOf("192.168.1.2"), 62654))
        assertEquals(
            DiscoveryResult("192.168.1.3", "peer", 62654),
            peers.resolved(current, null, listOf("192.168.1.3"), 62654)
        )
    }

    @Test
    fun `partial anonymous loss refreshes only the anonymous sighting`() {
        val known = peers.found("peer", wifi).sighting
        val anonymous = peers.found("peer", null).sighting
        peers.found("peer", null)
        peers.resolved(anonymous, null, listOf("10.0.0.2"), 62654)

        val loss = peers.lost("peer", null)!!

        assertFalse(loss.peerLost)
        assertNull(loss.publish)
        assertNull(peers.resolved(anonymous, null, listOf("172.16.0.9"), 62654))
        assertEquals(
            DiscoveryResult("10.0.0.2,192.168.1.2", "peer", 62654),
            peers.resolved(known, null, listOf("192.168.1.2"), 62654)
        )
        assertEquals(
            DiscoveryResult("10.0.0.3,192.168.1.2", "peer", 62654),
            peers.resolved(loss.refresh!!, null, listOf("10.0.0.3"), 62654)
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
        peers.found("peer", wifi)
        peers.found("peer", ethernet)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 4006)

        val result = peers.updated("peer", ethernet, listOf("10.0.0.2"), 4007)

        assertEquals(DiscoveryResult("192.168.1.2,10.0.0.2", "peer", 4007), result)
        assertNull(peers.updated("peer", wifi, listOf("192.168.1.2"), 4007))
    }

    @Test
    fun `a report without a port keeps the last known port`() {
        peers.found("peer", wifi)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 4006)

        val result = peers.updated("peer", wifi, listOf("192.168.1.3"), 0)

        assertEquals(DiscoveryResult("192.168.1.3", "peer", 4006), result)
    }

    @Test
    fun `losing the only resolved network retracts its addresses once`() {
        peers.found("peer", wifi)
        peers.found("peer", ethernet)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertFalse(loss.peerLost)
        assertEquals(DiscoveryResult("", "peer", 62654), loss.publish)
        assertNull(peers.updated("peer", ethernet, emptyList(), 62654))
        assertEquals(
            DiscoveryResult("10.0.0.2", "peer", 62654),
            peers.updated("peer", ethernet, listOf("10.0.0.2"), 62654)
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
        peers.found("peer", wifi)
        peers.found("peer", ethernet)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)
        peers.updated("peer", ethernet, listOf("10.0.0.2"), 62654)

        val result = peers.updated("peer", wifi, listOf(""), 62654)

        assertEquals(DiscoveryResult("10.0.0.2", "peer", 62654), result)
    }

    @Test
    fun `an unchanged snapshot is not published again`() {
        peers.found("peer", wifi)
        peers.updated("peer", wifi, listOf("192.168.1.2"), 62654)

        assertNull(peers.updated("peer", wifi, listOf("192.168.1.2"), 62654))
        assertNull(peers.found("peer", wifi).publish)
    }

    @Test
    fun `addresses keep the order in which networks first reported them`() {
        peers.found("peer", wifi)
        peers.found("peer", ethernet)
        peers.updated("peer", ethernet, listOf("10.0.0.2", "192.168.1.2"), 62654)

        val result = peers.updated("peer", wifi, listOf("192.168.1.2", "fe80::2%3", ""), 62654)

        assertEquals(DiscoveryResult("10.0.0.2,192.168.1.2,fe80::2%3", "peer", 62654), result)
    }

    @Test
    fun `nothing revives a peer after its last network is lost`() {
        val sighting = peers.found("peer", wifi).sighting
        peers.lost("peer", wifi)

        assertFalse(peers.isCurrent(sighting))
        assertNull(peers.resolved(sighting, null, listOf("192.168.1.2"), 62654))
        assertNull(peers.updated("peer", wifi, listOf("192.168.1.2"), 62654))
    }

    @Test
    fun `rediscovery does not accept a resolve from the previous presence`() {
        val old = peers.found("peer", null).sighting
        peers.lost("peer", null)
        val current = peers.found("peer", null).sighting

        assertNull(peers.resolved(old, null, listOf("192.168.1.1"), 62654))
        assertEquals(
            DiscoveryResult("192.168.1.2", "peer", 62654),
            peers.resolved(current, null, listOf("192.168.1.2"), 62654)
        )
    }

    @Test
    fun `clearing a session invalidates pending resolves even when the same peer returns`() {
        val old = peers.found("peer", wifi).sighting
        peers.clear()
        val current = peers.found("peer", wifi).sighting

        assertNull(peers.resolved(old, null, listOf("192.168.1.1"), 62654))
        assertFalse(peers.isCurrent(old))
        assertTrue(peers.isCurrent(current))
    }

    @Test
    fun `unknown losses do not remove another network or peer`() {
        val sighting = peers.found("peer", wifi).sighting

        assertNull(peers.lost("peer", ethernet))
        assertNull(peers.lost("peer", null))
        assertNull(peers.lost("another-peer", wifi))
        assertTrue(peers.isCurrent(sighting))
        assertNotNull(peers.lost("peer", wifi))
    }
}
