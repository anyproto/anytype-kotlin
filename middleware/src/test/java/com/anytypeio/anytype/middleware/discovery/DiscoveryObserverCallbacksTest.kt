package com.anytypeio.anytype.middleware.discovery

import android.net.Network
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.util.concurrent.Executor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.After
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
    private val listener = NsdDiscoveryListener(scope, dispatcher, manager, reader)
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

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun flush() = dispatcher.scheduler.runCurrent()

    private fun Subscription.updated(service: NsdServiceInfo = this.service) {
        executor.execute { callback.onServiceUpdated(service) }
        flush()
    }

    private fun service(network: Network? = null, addresses: List<String> = listOf("192.168.1.2")): NsdServiceInfo {
        val info = mock<NsdServiceInfo> {
            on { serviceName }.thenReturn("peer-id")
            on { serviceType }.thenReturn("_anytype._tcp")
            on { port }.thenReturn(62654)
        }
        whenever(reader.network(info)).thenReturn(network)
        whenever(reader.addresses(info)).thenReturn(addresses)
        whenever(reader.unscoped(info)).thenReturn(info)
        return info
    }

    @Test
    fun `loss before queued resolution skips the request and reports lost`() {
        listener.onServiceFound(service)
        listener.onServiceLost(service)
        flush()

        verify(observer).observeLost("peer-id")
        verifyNoMoreInteractions(observer)
        verifyNoInteractions(manager)
    }

    @Test
    fun `resolve completing after loss does not bring the peer back`() {
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
    fun `rediscovery accepts only the new resolve and does not stall behind the old one`() {
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
    fun `partial network loss updates the full address list without reporting the peer lost`() {
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
    fun `anonymous partial loss invalidates old resolves and re-resolves surviving sightings`() {
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
    fun `stopping and restarting with the same observer still invalidates in flight resolves`() {
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
    fun `stale resolve failures after loss are also ignored`() {
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
    fun `discovery failures report nonzero codes with the original platform code`() {
        listener.onStartDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onStopDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_OPERATION_NOT_RUNNING)
        flush()

        inOrder(observer) {
            verify(observer).observeError(-1, "NSD discovery start failed: _anytype._tcp (NsdManager errorCode=0)")
            verify(observer).observeError(5, "NSD discovery stop failed: _anytype._tcp (NsdManager errorCode=5)")
        }
    }

    @Test
    fun `discovery callbacks are ignored after observer removal`() {
        listener.unregisterObserver()
        listener.onServiceLost(service)
        listener.onStartDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onStopDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        flush()

        verifyNoInteractions(observer)
    }

    @Test
    fun `queued discovery does not resolve after observer removal`() {
        listener.onServiceFound(service)
        listener.unregisterObserver()
        flush()

        verifyNoInteractions(manager, observer)
    }

    @Test
    fun `successful registration clears a previously reported error`() {
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
    fun `registration callbacks are ignored after observer removal`() {
        val registration = NsdRegistrationListener()
        registration.registerObserver(observer)
        registration.unregisterObserver()
        registration.onServiceRegistered(service)
        registration.onRegistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        registration.onUnregistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)

        verifyNoInteractions(observer)
    }

    @Test
    fun `resolve failure reports the error and releases the next resolve`() {
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()
        pending[0].second.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        flush()

        verify(observer).observeError(-1, "NSD service resolve failed: peer-id (NsdManager errorCode=0)")
        assertEquals(2, pending.size)
    }

    @Test
    fun `synchronous resolve failure does not block later discovery`() {
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
    fun `resolve callback exceptions still release the next resolve`() {
        val semaphore = Semaphore(permits = 1, acquiredPermits = 1)
        val callback = ResolveListener(semaphore, { error("Callback stopped") }, { _, _ -> error("Callback stopped") })

        callback.onServiceResolved(service)
        assertEquals(1, semaphore.availablePermits)
        semaphore.tryAcquire()
        callback.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        assertEquals(1, semaphore.availablePermits)
    }

    @Test
    fun `continuous updates include IPv6 addresses arriving after the first response`() {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val discovered = service(mock(), listOf("192.168.1.2"))
        listener.onServiceFound(discovered)
        flush()
        subscriptions.single().updated()

        whenever(reader.addresses(discovered)).thenReturn(listOf("192.168.1.2", "fe80::2%wlan0"))
        subscriptions.single().updated()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,fe80::2%wlan0", "peer-id", 62654))
        }
        verify(manager, never()).resolveService(any(), any())
    }

    @Test
    fun `network loss closes only its subscription and rejects late updates`() {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val wifi = service(mock(), listOf("192.168.1.2"))
        val ethernet = service(mock(), listOf("10.0.0.2"))
        listener.onServiceFound(wifi)
        listener.onServiceFound(ethernet)
        flush()
        subscriptions[0].updated()
        subscriptions[1].updated()

        listener.onServiceLost(wifi)
        flush()
        subscriptions[0].updated()
        verify(manager).unregisterServiceInfoCallback(subscriptions[0].callback)
        verify(manager, never()).unregisterServiceInfoCallback(subscriptions[1].callback)
        listener.onServiceLost(ethernet)
        flush()
        subscriptions[1].updated()

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("10.0.0.2", "peer-id", 62654))
            verify(observer).observeLost("peer-id")
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `unscoped service callback loss cannot claim the whole peer disappeared`() {
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
    fun `observer removal closes subscriptions and same observer restart rejects old updates`() {
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
    fun `subscription registration failure reports a nonzero error and allows rediscovery`() {
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
    fun `anonymous partial loss replaces the subscription without losing known routes`() {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val unscoped = service(addresses = listOf("10.0.0.2"))
        whenever(reader.unscoped(service)).thenReturn(unscoped)
        listener.onServiceFound(service)
        listener.onServiceFound(service)
        flush()
        val old = subscriptions.single()
        old.updated(service)
        whenever(reader.addresses(service)).thenReturn(listOf("10.0.0.2"))
        old.updated(service)

        listener.onServiceLost(service)
        flush()
        old.updated()
        subscriptions[1].updated()

        verify(manager).unregisterServiceInfoCallback(old.callback)
        assertEquals(unscoped, subscriptions[1].service)
        verify(observer, never()).observeLost(any())
        verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
        // Before and after the refresh, both previously known paths remain available.
        verify(observer, org.mockito.kotlin.times(3))
            .observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 62654))
    }

    @Test
    fun `anonymous interfaces share an unscoped subscription from the first sighting`() {
        whenever(reader.supportsUpdates()).thenReturn(true)
        val firstInterface = service(addresses = listOf("192.168.1.2"))
        val secondInterface = service(addresses = listOf("10.0.0.2"))
        val unscoped = service()
        whenever(reader.unscoped(firstInterface)).thenReturn(unscoped)
        listener.onServiceFound(firstInterface)
        listener.onServiceFound(secondInterface)
        flush()

        val subscription = subscriptions.single()
        assertEquals(unscoped, subscription.service)
        subscription.updated(firstInterface)
        subscription.updated(secondInterface)

        inOrder(observer) {
            verify(observer).observeChange(DiscoveryResult("192.168.1.2", "peer-id", 62654))
            verify(observer).observeChange(DiscoveryResult("192.168.1.2,10.0.0.2", "peer-id", 62654))
        }
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `cancelling the discovery scope closes live subscriptions`() {
        whenever(reader.supportsUpdates()).thenReturn(true)
        listener.onServiceFound(service)
        flush()

        scope.cancel()
        flush()

        verify(manager).unregisterServiceInfoCallback(subscriptions.single().callback)
    }
}
