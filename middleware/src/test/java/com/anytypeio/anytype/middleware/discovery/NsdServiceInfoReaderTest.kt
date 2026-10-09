package com.anytypeio.anytype.middleware.discovery

import android.net.Network
import android.net.nsd.NsdServiceInfo
import java.net.InetAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NsdServiceInfoReaderTest {
    private val service = mock<NsdServiceInfo>()
    private val ipv4 = InetAddress.getByName("192.168.1.2")
    private val ipv6 = InetAddress.getByName("fe80::2")

    @Test
    fun `Android 14 reads all host addresses without using the deprecated single host`() {
        val reader = AndroidNsdServiceInfoReader(sdkInt = 34, tExtension = 0)
        whenever(service.hostAddresses).thenReturn(listOf(ipv4, ipv6, ipv4))

        assertTrue(reader.supportsUpdates())
        assertEquals(listOf(ipv4.hostAddress, ipv6.hostAddress), reader.addresses(service))
        verify(service, never()).host
    }

    @Test
    fun `Android 13 extension 7 also reads all addresses and supports live updates`() {
        val reader = AndroidNsdServiceInfoReader(sdkInt = 33, tExtension = 7)
        whenever(service.hostAddresses).thenReturn(listOf(ipv4, ipv6))

        assertTrue(reader.supportsUpdates())
        assertEquals(listOf(ipv4.hostAddress, ipv6.hostAddress), reader.addresses(service))
        verify(service, never()).host
    }

    @Test
    fun `older Android 13 modules use the available single address API`() {
        val reader = AndroidNsdServiceInfoReader(sdkInt = 33, tExtension = 6)
        whenever(service.host).thenReturn(ipv4)

        assertFalse(reader.supportsUpdates())
        assertEquals(listOf(ipv4.hostAddress), reader.addresses(service))
        verify(service, never()).hostAddresses
    }

    @Test
    fun `legacy platforms never call the network API`() {
        val reader = AndroidNsdServiceInfoReader(sdkInt = 32, tExtension = 0)

        assertFalse(reader.supportsUpdates())
        assertNull(reader.network(service))
        assertEquals(emptyList<String>(), reader.addresses(service))
        verify(service, never()).network
        verify(service, never()).hostAddresses
    }

    @Test
    fun `Android 13 exposes network identity before the address list extension`() {
        val reader = AndroidNsdServiceInfoReader(sdkInt = 33, tExtension = 0)
        val network = mock<Network>()
        whenever(service.network).thenReturn(network)

        assertEquals(network, reader.network(service))
    }
}
