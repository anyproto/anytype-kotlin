package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import java.net.InetAddress
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import service.DiscoveryObserver

class DiscoveryObserverCallbacksTest {

    private val observer = mock<DiscoveryObserver>()
    private val service = mock<NsdServiceInfo> {
        on { serviceName }.thenReturn("peer-id")
    }

    private fun discoveryListener(): NsdDiscoveryListener {
        val dispatcher = StandardTestDispatcher()
        return NsdDiscoveryListener(TestScope(dispatcher), dispatcher, mock())
    }

    @Test
    fun `lost service reports the peer id to heart`() {
        val listener = discoveryListener()
        listener.registerObserver(observer)

        listener.onServiceFound(service)
        listener.onServiceLost(service)

        verify(observer).observeLost("peer-id")
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `discovery failures report nonzero codes with the original platform code`() {
        val listener = discoveryListener()
        listener.registerObserver(observer)

        listener.onStartDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onStopDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_OPERATION_NOT_RUNNING)

        inOrder(observer) {
            verify(observer).observeError(
                -1,
                "NSD discovery start failed: _anytype._tcp (NsdManager errorCode=0)"
            )
            verify(observer).observeError(
                NsdManager.FAILURE_OPERATION_NOT_RUNNING.toLong(),
                "NSD discovery stop failed: _anytype._tcp (NsdManager errorCode=5)"
            )
        }
    }

    @Test
    fun `discovery callbacks are ignored after observer removal`() {
        val listener = discoveryListener()
        listener.registerObserver(observer)
        listener.unregisterObserver()

        listener.onServiceLost(service)
        listener.onStartDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onStopDiscoveryFailed("_anytype._tcp", NsdManager.FAILURE_INTERNAL_ERROR)

        verifyNoInteractions(observer)
    }

    @Test
    fun `queued discovery does not resolve after observer removal`() {
        val dispatcher = StandardTestDispatcher()
        val manager = mock<NsdManager>()
        val listener = NsdDiscoveryListener(TestScope(dispatcher), dispatcher, manager)
        listener.registerObserver(observer)
        listener.onServiceFound(service)

        listener.unregisterObserver()
        dispatcher.scheduler.advanceUntilIdle()

        verifyNoInteractions(manager, observer)
    }

    @Test
    fun `successful registration clears a previously reported error`() {
        val listener = NsdRegistrationListener()
        listener.registerObserver(observer)

        listener.onRegistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onServiceRegistered(service)
        listener.onUnregistrationFailed(service, NsdManager.FAILURE_OPERATION_NOT_RUNNING)

        inOrder(observer) {
            verify(observer).observeError(
                -1,
                "NSD service registration failed: peer-id (NsdManager errorCode=0)"
            )
            verify(observer).observeError(0, "")
            verify(observer).observeError(
                NsdManager.FAILURE_OPERATION_NOT_RUNNING.toLong(),
                "NSD service unregistration failed: peer-id (NsdManager errorCode=5)"
            )
        }
    }

    @Test
    fun `registration callbacks are ignored after observer removal`() {
        val listener = NsdRegistrationListener()
        listener.registerObserver(observer)
        listener.unregisterObserver()

        listener.onServiceRegistered(service)
        listener.onRegistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)
        listener.onUnregistrationFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)

        verifyNoInteractions(observer)
    }

    @Test
    fun `resolve failure reports the error and releases the next resolve`() {
        val semaphore = Semaphore(permits = 1, acquiredPermits = 1)
        val listener = ResolveListener(observer, semaphore)

        listener.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)

        verify(observer).observeError(
            -1,
            "NSD service resolve failed: peer-id (NsdManager errorCode=0)"
        )
        assertEquals(1, semaphore.availablePermits)
    }

    @Test
    fun `resolve failure releases the next resolve even if heart throws`() {
        val semaphore = Semaphore(permits = 1, acquiredPermits = 1)
        val listener = ResolveListener(observer, semaphore)
        doThrow(IllegalStateException("Observer stopped"))
            .whenever(observer).observeError(eq(-1L), any())

        listener.onResolveFailed(service, NsdManager.FAILURE_INTERNAL_ERROR)

        assertEquals(1, semaphore.availablePermits)
    }

    @Test
    fun `resolved service releases the next resolve even if heart throws`() {
        val semaphore = Semaphore(permits = 1, acquiredPermits = 1)
        val listener = ResolveListener(observer, semaphore)
        whenever(service.host).thenReturn(InetAddress.getByName("192.168.1.2"))
        doThrow(IllegalStateException("Observer stopped"))
            .whenever(observer).observeChange(any())

        listener.onServiceResolved(service)

        verify(observer).observeChange(any())
        assertEquals(1, semaphore.availablePermits)
    }
}
