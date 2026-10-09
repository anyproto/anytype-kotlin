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
        val first = peers.found("peer", wifi)
        val second = peers.found("peer", ethernet)
        peers.resolved(first, listOf("192.168.1.2"), 62654)
        peers.resolved(second, listOf("10.0.0.2"), 62654)

        val loss = peers.lost("peer", wifi)!!

        assertFalse(loss.peerLost)
        assertEquals("10.0.0.2", loss.remaining?.ip)
        assertTrue(peers.lost("peer", ethernet)!!.peerLost)
        assertNull(peers.lost("peer", ethernet))
    }

    @Test
    fun `duplicate discovery on a known network replaces the resolve generation`() {
        val old = peers.found("peer", wifi)
        val current = peers.found("peer", wifi)

        assertNull(peers.resolved(old, listOf("192.168.1.1"), 62654))
        assertNotNull(peers.resolved(current, listOf("192.168.1.2"), 62654))
        assertTrue(peers.lost("peer", wifi)!!.peerLost)
    }

    @Test
    fun `anonymous interfaces require matching losses instead of collapsing into one network`() {
        peers.found("peer", null)
        peers.found("peer", null)

        val firstLoss = peers.lost("peer", null)!!

        assertFalse(firstLoss.peerLost)
        assertNotNull(firstLoss.refresh)
        assertTrue(peers.lost("peer", null)!!.peerLost)
    }

    @Test
    fun `resolve cannot revive a peer after its last network is lost`() {
        val resolution = peers.found("peer", wifi)
        peers.lost("peer", wifi)

        assertFalse(peers.isCurrent(resolution))
        assertNull(peers.resolved(resolution, listOf("192.168.1.2"), 62654))
    }

    @Test
    fun `rediscovery does not accept a resolve from the previous presence`() {
        val old = peers.found("peer", null)
        peers.lost("peer", null)
        val current = peers.found("peer", null)

        assertNull(peers.resolved(old, listOf("192.168.1.1"), 62654))
        assertEquals("192.168.1.2", peers.resolved(current, listOf("192.168.1.2"), 62654)?.ip)
    }

    @Test
    fun `clearing a session invalidates pending resolves even when the same peer returns`() {
        val old = peers.found("peer", wifi)
        peers.clear()
        val current = peers.found("peer", wifi)

        assertNull(peers.resolved(old, listOf("192.168.1.1"), 62654))
        assertTrue(peers.isCurrent(current))
    }

    @Test
    fun `every update contains the union of addresses across networks`() {
        val first = peers.found("peer", wifi)
        val second = peers.found("peer", ethernet)
        peers.resolved(first, listOf("192.168.1.2", "fe80::2%wlan0", ""), 62654)

        val result = peers.resolved(second, listOf("10.0.0.2", "192.168.1.2"), 62654)

        assertEquals(DiscoveryResult("192.168.1.2,fe80::2%wlan0,10.0.0.2", "peer", 62654), result)
    }

    @Test
    fun `refresh replaces only that networks addresses`() {
        val first = peers.found("peer", wifi)
        val second = peers.found("peer", ethernet)
        peers.resolved(first, listOf("192.168.1.2"), 62654)
        peers.resolved(second, listOf("10.0.0.2"), 62654)
        val refresh = peers.found("peer", wifi)

        val result = peers.resolved(refresh, listOf("192.168.1.3", "fe80::3%wlan0"), 62654)

        assertEquals("192.168.1.3,fe80::3%wlan0,10.0.0.2", result?.ip)
    }

    @Test
    fun `anonymous resolutions accumulate addresses until a loss requires a fresh query`() {
        val first = peers.found("peer", null)
        val second = peers.found("peer", null)
        peers.resolved(first, listOf("192.168.1.2"), 62654)
        assertEquals("192.168.1.2,10.0.0.2", peers.resolved(second, listOf("10.0.0.2"), 62654)?.ip)

        val loss = peers.lost("peer", null)!!

        assertNull(peers.resolved(first, listOf("192.168.1.2"), 62654))
        assertNull(peers.resolved(second, listOf("10.0.0.2"), 62654))
        assertEquals("192.168.1.2,10.0.0.2", peers.resolved(loss.refresh!!, listOf("10.0.0.2"), 62654)?.ip)
    }

    @Test
    fun `one anonymous loss and single address refresh preserve other surviving routes`() {
        val resolution = peers.found("peer", null)
        peers.found("peer", null)
        peers.found("peer", null)
        peers.resolved(resolution, listOf("192.168.1.2"), 62654)
        peers.resolved(resolution, listOf("10.0.0.2"), 62654)
        peers.resolved(resolution, listOf("172.16.0.2"), 62654)

        val loss = peers.lost("peer", null)!!
        val result = peers.resolved(loss.refresh!!, listOf("10.0.0.2"), 62654)

        assertFalse(loss.peerLost)
        assertEquals("192.168.1.2,10.0.0.2,172.16.0.2", result?.ip)
        assertFalse(peers.lost("peer", null)!!.peerLost)
        assertTrue(peers.lost("peer", null)!!.peerLost)
    }

    @Test
    fun `unknown losses do not remove another network or peer`() {
        val resolution = peers.found("peer", wifi)

        assertNull(peers.lost("peer", ethernet))
        assertNull(peers.lost("another-peer", wifi))
        assertTrue(peers.isCurrent(resolution))
    }
}
