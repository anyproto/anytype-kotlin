package com.anytypeio.anytype.middleware.discovery

import android.net.Network
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.util.concurrent.Executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import service.DiscoveryObserver

class DiscoveryObserverCallbacksTest {

    private val observer = mock<DiscoveryObserver>()
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val manager = mock<NsdManager>()
    private val reader = mock<NsdServiceInfoReader>()
    private val pending = mutableListOf<Pair<NsdServiceInfo, NsdManager.ResolveListener>>()
    private data class Subscription(
        val service: NsdServiceInfo,
        val executor: Executor,
        val callback: NsdManager.ServiceInfoCallback
    )
    private val subscriptions = mutableListOf<Subscription>()
    // The background scope reports an exception that escapes the event queue when runTest ends.
    private val listener = NsdDiscoveryListener(scope.backgroundScope, dispatcher, manager, reader)
    private val service = service()

    init {
        doAnswer {
            pending += it.getArgument<NsdServiceInfo>(0) to it.getArgument<NsdManager.ResolveListener>(1)
            null
        }.whenever(manager).resolveService(any(), any())
        doAnswer {
            subscriptions += Subscription(it.getArgument(0), it.getArgument(1), it.getArgument(2))
            null
        }.whenever(manager).registerServiceInfoCallback(any(), any(), any())
        listener.registerObserver(observer)
    }

    private fun test(body: suspend TestScope.() -> Unit) = scope.runTest(testBody = body)

    private fun flush() = dispatcher.scheduler.runCurrent()

    private fun Subscription.updated(update: NsdServiceInfo = this.service) {
        executor.execute { callback.onServiceUpdated(update) }
        flush()
    }

    private fun service(
        network: Network? = null,
        addresses: List<String> = listOf("192.168.1.2"),
        port: Int = 62654,
        name: String = "peer-id"
    ): NsdServiceInfo {
        val info = mock<NsdServiceInfo> {
            on { serviceName }.thenReturn(name)
            on { serviceType }.thenReturn("_anytype._tcp")
            on { this.port }.thenReturn(port)
        }
        whenever(reader.network(info)).thenReturn(network)
        whenever(reader.addresses(info)).thenReturn(addresses)
        whenever(reader.unscoped(info)).thenReturn(info)
        return info
    }

    @Test
    fun `loss before queued resolution skips the request and reports lost`() = test {
        listener.onServiceFound(service)
        listener.onServiceLost(service)
        flush()

        verify(observer).observeLost("peer-id")
        verifyNoMoreInteractions(observer)
        verifyNoInteractions(manager)
    }

    @Test
    fun `resolve completing after loss does not bring the peer back`() = test {
        listener.onServiceFound(service)
        flush()
        listener.onServiceLost(service)
        flush()

        pending.single().second.onServiceResolved(service)
        flush()

        verify(observer).observeLost("peer-id")
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `rediscovery accepts only the new resolve and does not stall behind the old one`() = test {
        listener.onServiceFound(service)
        flush()
        listener.onServiceLost(service)
        val rediscovered = service(addresses = listOf("192.168.1.3"))
        listener.onServiceFound(rediscovered)
        flush()
        assertEquals(1, pending.size)

        pending[0].second.onServiceResolved(service)
        flush()
        assertEquals(2, pending.size)
        pending[1].second.onServiceResolved(rediscovered)
        flush()

        inOrder(observer) {
            verify(observer).observeLost("peer-id")
            verify(observer).observeChange(DiscoveryResult("192.168.1.3", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `partial network loss updates the full address list without reporting the peer lost`() = test {
        val wifi = service(mock(), listOf("192.168.1.2", "fe80::2%wlan0"))
        val ethernet = service(mock(), listOf("10.0.0.2", "192.168.1.2"))
        listener.onServiceFound(wifi)
        listener.onServiceFound(ethernet)
        flush()
        pending[0].second.onServiceResolved(wifi)
        flush()
        pending[1].second.onServiceResolved(ethernet)
        flush()

        listener.onServiceLost(wifi)
        flush()
        listener.onServiceLost(ethernet)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,fe80::2%wlan0", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,fe80::2%wlan0,10.0.0.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2,192.168.1.2", "peer-id", 62654))
            verify(observer).observeLost("peer-id")
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `losing the only resolved network while an unresolved one stays retracts its addresses`() = test {
        val wifi = service(mock(), listOf("192.168.1.2"))
        val ethernet = service(mock(), listOf("10.0.0.2"))
        listener.onServiceFound(wifi)
        listener.onServiceFound(ethernet)
        flush()
        pending[0].second.onServiceResolved(wifi)
        flush()
        pending[1].second.onResolveFailed(ethernet, NsdManager.FAILURE_INTERNAL_ERROR)
        flush()

        listener.onServiceLost(wifi)
        flush()
        listener.onServiceLost(ethernet)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeError(-1, "NSD service resolve failed: peer-id (NsdManager errorCode=0)")
            verify(observer).observeChange(DiscoveryResult("", "peer-id", 62654))
            verify(observer).observeLost("peer-id")
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `the latest port applies to every published address`() = test {
        val wifi = service(mock(), listOf("192.168.1.2"), port = 4006)
        val ethernet = service(mock(), listOf("10.0.0.2"), port = 4007)
        listener.onServiceFound(wifi)
        listener.onServiceFound(ethernet)
        flush()
        pending[0].second.onServiceResolved(wifi)
        flush()
        pending[1].second.onServiceResolved(ethernet)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 4006))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 4007))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `legacy anonymous resolve replaces the previous address instead of accumulating`() = test {
        val firstInterface = service(addresses = listOf("192.168.1.2"))
        val secondInterface = service(addresses = listOf("10.0.0.2"))
        listener.onServiceFound(firstInterface)
        listener.onServiceFound(secondInterface)
        flush()
        pending[0].second.onServiceResolved(firstInterface)
        flush()
        pending[1].second.onServiceResolved(secondInterface)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `a partial anonymous loss keeps the pending resolve of another network`() = test {
        val wifi = service(mock(), listOf("192.168.1.2"))
        val tether = service(null, listOf("10.0.0.2"))
        listener.onServiceFound(wifi)
        listener.onServiceFound(tether)
        listener.onServiceFound(tether)
        flush()

        listener.onServiceLost(tether)
        flush()
        pending[0].second.onServiceResolved(wifi)
        flush()

        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `a resolve that completes after its network is lost is not published when the network returns`() = test {
        val wifiNetwork = mock<Network>()
        val ethernet = service(mock(), listOf("10.0.0.2"))
        val wifi = service(wifiNetwork, listOf("192.168.1.2"))
        listener.onServiceFound(ethernet)
        listener.onServiceFound(wifi)
        flush()
        pending[0].second.onServiceResolved(ethernet)
        flush()

        listener.onServiceLost(wifi)
        flush()
        pending[1].second.onServiceResolved(wifi)
        flush()
        val wifiAgain = service(wifiNetwork, listOf("192.168.1.3"))
        listener.onServiceFound(wifiAgain)
        flush()
        pending[2].second.onServiceResolved(wifiAgain)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2,192.168.1.3", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `an unscoped refresh answered from a known network leaves the anonymous route in place`() = test {
        val wifi = service(mock(), listOf("192.168.1.2"))
        val tether = service(null, listOf("10.0.0.2"))
        listener.onServiceFound(wifi)
        flush()
        pending[0].second.onServiceResolved(wifi)
        flush()
        listener.onServiceFound(tether)
        listener.onServiceFound(tether)
        flush()
        pending[1].second.onServiceResolved(tether)
        flush()
        pending[2].second.onServiceResolved(tether)
        flush()

        val refresh = service()
        whenever(reader.unscoped(tether)).thenReturn(refresh)
        listener.onServiceLost(tether)
        flush()
        assertEquals(refresh, pending[3].first)
        // The platform answers an unscoped query from any interface and names the one it used.
        pending[3].second.onServiceResolved(wifi)
        flush()
        listener.onServiceLost(wifi)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `a scoped resolve answers for the network it was started for`() = test {
        val wifi = service(mock(), listOf("192.168.1.2"))
        val reportedElsewhere = service(mock(), listOf("192.168.1.2"))
        listener.onServiceFound(wifi)
        flush()

        pending[0].second.onServiceResolved(reportedElsewhere)
        flush()

        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
    }

    @Test
    fun `anonymous partial loss invalidates old resolves and re-resolves surviving sightings`() = test {
        val unscoped = service(addresses = listOf("10.0.0.2"))
        whenever(reader.unscoped(service)).thenReturn(unscoped)
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()
        listener.onServiceLost(service)
        flush()

        pending[0].second.onServiceResolved(service)
        flush()
        assertEquals(2, pending.size)
        assertEquals(unscoped, pending[1].first)
        pending[1].second.onServiceResolved(unscoped)
        flush()

        verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
        verifyNoMoreInteractions(observer)
        listener.onServiceLost(service)
        flush()
        verify(observer).observeLost("peer-id")
    }

    @Test
    fun `stopping and restarting with the same observer still invalidates in flight resolves`() = test {
        listener.onServiceFound(service)
        flush()
        listener.unregisterObserver()
        listener.registerObserver(observer)
        listener.onServiceFound(service)
        flush()

        pending[0].second.onServiceResolved(service)
        flush()
        verifyNoInteractions(observer)
        pending[1].second.onServiceResolved(service)
        flush()
        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `stale resolve failures after loss are also ignored`() = test {
        listener.onServiceFound(service)
        flush()
        listener.onServiceLost(service)
        flush()

        pending.single().second.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        flush()

        verify(observer).observeLost("peer-id")
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `discovery failures report nonzero codes with the original platform code`() = test {
        listener.onStartDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onStopDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_OPERATION_NOT_RUNNING)
        flush()

        inOrder(observer) {
            verify(observer).observeError(-1, "NSD discovery start failed: _anytype._tcp (NsdManager errorCode=0)")
            verify(observer).observeError(5, "NSD discovery stop failed: _anytype._tcp (NsdManager errorCode=5)")
        }
    }

    @Test
    fun `discovery callbacks are ignored after observer removal`() = test {
        listener.unregisterObserver()
        listener.onServiceLost(service)
        listener.onStartDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onStopDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        flush()

        verifyNoInteractions(observer)
    }

    @Test
    fun `queued discovery does not resolve after observer removal`() = test {
        listener.onServiceFound(service)
        listener.unregisterObserver()
        flush()

        verifyNoInteractions(manager, observer)
    }

    @Test
    fun `successful registration clears a previously reported error`() = test {
        val registration = NsdRegistrationListener()
        registration.registerObserver(observer)
        registration.onRegistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        registration.onServiceRegistered(service)
        registration.onUnregistrationFailed(service, NsdManager.FAILURE_OPERATION_NOT_RUNNING)

        inOrder(observer) {
            verify(observer).observeError(-1, "NSD service registration failed: peer-id (NsdManager errorCode=0)")
            verify(observer).observeError(0, "")
            verify(observer).observeError(5, "NSD service unregistration failed: peer-id (NsdManager errorCode=5)")
        }
    }

    @Test
    fun `registration callbacks are ignored after observer removal`() = test {
        val registration = NsdRegistrationListener()
        registration.registerObserver(observer)
        registration.unregisterObserver()
        registration.onServiceRegistered(service)
        registration.onRegistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        registration.onUnregistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)

        verifyNoInteractions(observer)
    }

    @Test
    fun `resolve failure reports the error and releases the next resolve`() = test {
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()
        pending[0].second.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        flush()

        verify(observer).observeError(-1, "NSD service resolve failed: peer-id (NsdManager errorCode=0)")
        assertEquals(2, pending.size)
    }

    @Test
    fun `synchronous resolve failure does not block later discovery`() = test {
        doThrow(IllegalArgumentException("NSD rejected request"))
            .doAnswer {
                pending += it.getArgument<NsdServiceInfo>(0) to it.getArgument<NsdManager.ResolveListener>(1)
                null
            }.whenever(manager).resolveService(any(), any())
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()

        verify(observer).observeError(-1, "NSD service resolve failed: peer-id (NsdManager errorCode=0)")
        assertEquals(1, pending.size)
    }

    @Test
    fun `resolve callback exceptions still release the next resolve`() = test {
        val semaphore = Semaphore(permits = 1, acquiredPermits = 1)
        val callback = ResolveListener(semaphore, { error("Callback stopped") }, { _, _ -> error("Callback stopped") })

        callback.onServiceResolved(service)
        assertEquals(1, semaphore.availablePermits)
        semaphore.tryAcquire()
        callback.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        assertEquals(1, semaphore.availablePermits)
    }

    @Test
    fun `continuous updates include IPv6 addresses arriving after the first response`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val discovered = service(mock(), listOf("192.168.1.2"))
        listener.onServiceFound(discovered)
        flush()
        subscriptions.single().updated(discovered)

        whenever(reader.addresses(discovered)).thenReturn(listOf("192.168.1.2", "fe80::2%wlan0"))
        subscriptions.single().updated(discovered)

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,fe80::2%wlan0", "peer-id", 62654))
        }
        verify(manager, never()).resolveService(any(), any())
    }

    @Test
    fun `an unchanged update is not published again`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        listener.onServiceFound(service)
        flush()

        subscriptions.single().updated()
        subscriptions.single().updated()

        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `one unscoped subscription serves every network of a peer`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val wifi = mock<Network>()
        val discoveredOnWifi = service(wifi, listOf("192.168.1.2"))
        val discoveredOnTether = service(null, listOf("10.0.0.2"))
        val unscoped = service()
        whenever(reader.unscoped(discoveredOnWifi)).thenReturn(unscoped)
        listener.onServiceFound(discoveredOnWifi)
        listener.onServiceFound(discoveredOnTether)
        flush()

        val subscription = subscriptions.single()
        assertEquals(unscoped, subscription.service)
        subscription.updated(discoveredOnWifi)
        subscription.updated(discoveredOnTether)
        listener.onServiceLost(discoveredOnWifi)
        flush()
        verify(manager, never()).unregisterServiceInfoCallback(any())
        listener.onServiceLost(discoveredOnTether)
        flush()

        verify(manager).unregisterServiceInfoCallback(subscription.callback)
        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 62654))
            // The Wi-Fi address reached the shared subscription, but it leaves with Wi-Fi.
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
            verify(observer).observeLost("peer-id")
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `an anonymous update replaces the previous anonymous addresses`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        listener.onServiceFound(service)
        flush()

        subscriptions.single().updated(service(addresses = listOf("192.168.1.2", "fe80::1%3")))
        subscriptions.single().updated(service(addresses = listOf("192.168.1.2", "fe80::2%3")))

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,fe80::1%3", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,fe80::2%3", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `an update that arrives before discovery reports its network is published when it does`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val ethernet = mock<Network>()
        val discoveredOnEthernet = service(ethernet, listOf("10.0.0.2"))
        listener.onServiceFound(service)
        flush()

        subscriptions.single().updated(discoveredOnEthernet)
        verifyNoInteractions(observer)
        listener.onServiceFound(discoveredOnEthernet)
        flush()

        verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
        verifyNoMoreInteractions(observer)
        assertEquals(1, subscriptions.size)
    }

    @Test
    fun `anonymous partial loss replaces the subscription and ignores the old one`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val unscoped = service()
        whenever(reader.unscoped(service)).thenReturn(unscoped)
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()
        val old = subscriptions.single()
        old.updated(service(addresses = listOf("192.168.1.2")))

        listener.onServiceLost(service)
        flush()
        old.updated(service(addresses = listOf("172.16.0.9")))
        subscriptions[1].updated(service(addresses = listOf("10.0.0.2")))

        verify(manager).unregisterServiceInfoCallback(old.callback)
        assertEquals(unscoped, subscriptions[1].service)
        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `unscoped service callback loss cannot claim the whole peer disappeared`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()

        val subscription = subscriptions.single()
        subscription.executor.execute { subscription.callback.onServiceLost() }
        flush()

        verifyNoInteractions(observer)
        subscription.updated()
        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
    }

    @Test
    fun `observer removal closes subscriptions and same observer restart rejects old updates`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        listener.onServiceFound(service)
        flush()
        val old = subscriptions.single()
        listener.unregisterObserver()
        listener.registerObserver(observer)
        listener.onServiceFound(service)
        flush()

        old.updated()
        verifyNoInteractions(observer)
        verify(manager).unregisterServiceInfoCallback(old.callback)
        subscriptions[1].updated()
        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
    }

    @Test
    fun `subscription registration failure reports a nonzero error and allows rediscovery`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        listener.onServiceFound(service)
        flush()
        val failed = subscriptions.single()
        failed.executor.execute {
            failed.callback.onServiceInfoCallbackRegistrationFailed(NsdManager.FAILURE_INTERNAL_ERROR)
        }
        flush()

        listener.onServiceFound(service)
        flush()

        verify(observer).observeError(-1, "NSD service resolve failed: peer-id (NsdManager errorCode=0)")
        assertEquals(2, subscriptions.size)
        verify(manager, never()).unregisterServiceInfoCallback(failed.callback)
    }

    @Test
    fun `a network found again after its loss waits for a fresh update`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val wifi = service(mock(), listOf("192.168.1.2"))
        val ethernet = service(mock(), listOf("10.0.0.2"))
        listener.onServiceFound(wifi)
        listener.onServiceFound(ethernet)
        flush()
        subscriptions.single().updated(wifi)
        subscriptions.single().updated(ethernet)

        listener.onServiceLost(wifi)
        flush()
        listener.onServiceFound(wifi)
        flush()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `a registration failure removes only the subscription of its own peer`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val first = service(name = "first-peer")
        val second = service(name = "second-peer")
        listener.onServiceFound(first)
        listener.onServiceFound(second)
        flush()
        val failed = subscriptions[1]
        failed.executor.execute {
            failed.callback.onServiceInfoCallbackRegistrationFailed(NsdManager.FAILURE_INTERNAL_ERROR)
        }
        flush()

        listener.onServiceFound(second)
        listener.onServiceFound(first)
        flush()
        subscriptions[0].updated(first)

        // Only the failed peer subscribes again; the other peer keeps its subscription.
        assertEquals(listOf(first, second, second), subscriptions.map { it.service })
        verify(observer).observeError(-1, "NSD service resolve failed: second-peer (NsdManager errorCode=0)")
        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "first-peer", 62654))
        verify(manager, never()).unregisterServiceInfoCallback(any())
    }

    @Test
    fun `cancelling the discovery scope closes live subscriptions`() = test {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val discoveryScope = CoroutineScope(dispatcher + Job())
        val cancellable = NsdDiscoveryListener(discoveryScope, dispatcher, manager, reader)
        cancellable.registerObserver(observer)
        cancellable.onServiceFound(service)
        flush()

        discoveryScope.cancel()
        flush()

        verify(manager).unregisterServiceInfoCallback(subscriptions.single().callback)
    }
}
