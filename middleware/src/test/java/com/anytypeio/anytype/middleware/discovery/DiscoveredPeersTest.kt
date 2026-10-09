package com.anytypeio.anytype.middleware.discovery

import android.net.Network
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class DiscoveredPeersTest {

    @Test
    fun `peer is lost only when its last network disappears`() {
        val peers = DiscoveredPeers()
        val wifi = mock<Network>()
        val ethernet = mock<Network>()
        peers.found("peer-id", wifi)
        peers.found("peer-id", ethernet)

        assertFalse(peers.lost("peer-id", wifi))
        assertTrue(peers.lost("peer-id", ethernet))
        assertFalse(peers.lost("peer-id", ethernet))
    }

    @Test
    fun `repeated discovery does not prevent loss on older Android versions`() {
        val peers = DiscoveredPeers()
        peers.found("peer-id", null)
        peers.found("peer-id", null)

        assertTrue(peers.lost("peer-id", null))
    }

    @Test
    fun `clearing discovery does not report lost peers`() {
        val peers = DiscoveredPeers()
        peers.found("peer-id", null)

        peers.clear()

        assertFalse(peers.lost("peer-id", null))
    }
}
