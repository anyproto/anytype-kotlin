package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdServiceInfo
import android.os.Build
import java.net.InetAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork

// NsdServiceInfo is a framework class: only Robolectric runs its real setters and getters.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class NsdServiceInfoUnscopedTest {

    private val reader = AndroidNsdServiceInfoReader(sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)

    @Test
    fun `unscoped service keeps the identity and drops the network binding`() {
        val discovered = NsdServiceInfo().apply {
            serviceName = "peer-id"
            serviceType = "_anytype._tcp"
            port = 62654
            network = ShadowNetwork.newInstance(100)
            hostAddresses = listOf(InetAddress.getByName("192.168.1.2"))
        }

        val unscoped = reader.unscoped(discovered)

        assertNotSame(discovered, unscoped)
        assertEquals("peer-id", unscoped.serviceName)
        assertEquals("_anytype._tcp", unscoped.serviceType)
        assertNull(unscoped.network)
        assertEquals(0, unscoped.port)
        assertTrue(unscoped.hostAddresses.isEmpty())
    }

    @Test
    fun `an update reports the network it describes`() {
        val wifi = ShadowNetwork.newInstance(100)
        val update = NsdServiceInfo().apply { network = wifi }

        assertEquals(wifi, reader.network(update))
        assertNull(reader.network(NsdServiceInfo()))
    }
}
