package com.anytypeio.anytype.device.network_type

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.DeviceNetworkType
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NetworkConnectionStatusImplTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var scope: CoroutineScope
    private lateinit var repo: BlockRepository
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var subject: NetworkConnectionStatusImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        repo = mock()
        scope = CoroutineScope(SupervisorJob() + dispatcher)
        subject = NetworkConnectionStatusImpl(
            context = context,
            coroutineScope = scope,
            blockRepository = repo,
            dispatchers = AppCoroutineDispatchers(
                io = dispatcher,
                computation = dispatcher,
                main = dispatcher
            )
        )
    }

    @After
    fun tearDown() {
        subject.stop()
        scope.cancel()
    }

    // region helpers

    private fun caps(vararg transports: Int): NetworkCapabilities {
        val caps = ShadowNetworkCapabilities.newInstance()
        transports.forEach { shadowOf(caps).addTransportType(it) }
        return caps
    }

    private fun network(netId: Int): Network = ShadowNetwork.newInstance(netId)

    private fun disconnectDefaultNetwork() {
        shadowOf(connectivityManager).setDefaultNetworkActive(false)
    }

    /** The single callback registered by the subject. */
    private fun registeredCallback(): ConnectivityManager.NetworkCallback =
        shadowOf(connectivityManager).networkCallbacks.single()

    private fun startDisconnected(): ConnectivityManager.NetworkCallback {
        disconnectDefaultNetwork()
        subject.start()
        return registeredCallback()
    }

    // endregion

    @Test
    fun `wifi capabilities report WIFI with the network handle as id`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()
            val net = network(101)

            callback.onCapabilitiesChanged(net, caps(NetworkCapabilities.TRANSPORT_WIFI))
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(
                DeviceNetworkType.WIFI,
                net.networkHandle.toString()
            )
        }

    @Test
    fun `cellular capabilities report CELLULAR with the network handle as id`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()
            val net = network(102)

            callback.onCapabilitiesChanged(net, caps(NetworkCapabilities.TRANSPORT_CELLULAR))
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(
                DeviceNetworkType.CELLULAR,
                net.networkHandle.toString()
            )
        }

    @Test
    fun `ethernet reports WIFI not NOT_CONNECTED`() = runTest(dispatcher.scheduler) {
        val callback = startDisconnected()
        val net = network(103)

        callback.onCapabilitiesChanged(net, caps(NetworkCapabilities.TRANSPORT_ETHERNET))
        advanceUntilIdle()

        verify(repo).setDeviceNetworkState(
            DeviceNetworkType.WIFI,
            net.networkHandle.toString()
        )
    }

    @Test
    fun `vpn over cellular reports CELLULAR`() = runTest(dispatcher.scheduler) {
        val callback = startDisconnected()
        val net = network(104)

        callback.onCapabilitiesChanged(
            net,
            caps(NetworkCapabilities.TRANSPORT_VPN, NetworkCapabilities.TRANSPORT_CELLULAR)
        )
        advanceUntilIdle()

        verify(repo).setDeviceNetworkState(
            DeviceNetworkType.CELLULAR,
            net.networkHandle.toString()
        )
    }

    @Test
    fun `wifi without VALIDATED capability still reports WIFI`() =
        runTest(dispatcher.scheduler) {
            // Regression guard for the DROID-4548 contract reversal: a LAN-only or
            // captive-portal Wi-Fi (no NET_CAPABILITY_VALIDATED) must NOT report
            // NOT_CONNECTED -- heart throttles offline devices, and local P2P sync
            // still works on such networks. See anytype-heart 8a2fa9d91.
            val callback = startDisconnected()
            val net = network(105)
            val lanOnlyWifi = caps(NetworkCapabilities.TRANSPORT_WIFI)
            assertFalse(
                lanOnlyWifi.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            )

            callback.onCapabilitiesChanged(net, lanOnlyWifi)
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(
                DeviceNetworkType.WIFI,
                net.networkHandle.toString()
            )
        }

    @Test
    fun `onLost with a replacement default up reports its type instead of NOT_CONNECTED`() =
        runTest(dispatcher.scheduler) {
            // A default-network callback delivers onLost on every handoff (the old
            // default lost *default status*), not only on true disconnection. When a
            // replacement is already up, reporting NOT_CONNECTED would flip heart's
            // undebounced IsOffline and churn mDNS for nothing.
            val callback = startDisconnected()
            advanceUntilIdle()
            clearInvocations(repo)

            val replacement = shadowOf(connectivityManager).run {
                setDefaultNetworkActive(true)
                connectivityManager.activeNetwork!!.also {
                    setNetworkCapabilities(it, caps(NetworkCapabilities.TRANSPORT_CELLULAR))
                }
            }

            callback.onLost(network(106))
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(
                DeviceNetworkType.CELLULAR,
                replacement.networkHandle.toString()
            )
            verify(repo, never()).setDeviceNetworkState(DeviceNetworkType.NOT_CONNECTED, "")
        }

    @Test
    fun `onLost with no default network reports NOT_CONNECTED with empty id`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()
            advanceUntilIdle()
            clearInvocations(repo)

            callback.onLost(network(107))
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(DeviceNetworkType.NOT_CONNECTED, "")
        }

    @Test
    fun `onUnavailable reports NOT_CONNECTED with empty id`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()
            advanceUntilIdle()
            clearInvocations(repo)

            callback.onUnavailable()
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(DeviceNetworkType.NOT_CONNECTED, "")
        }

    @Test
    fun `identical consecutive reports are both sent -- no client-side debounce`() =
        runTest(dispatcher.scheduler) {
            // Heart dedupes and coalesces server-side; the contract explicitly
            // forbids client-side debounce.
            val callback = startDisconnected()
            val net = network(108)
            val wifi = caps(NetworkCapabilities.TRANSPORT_WIFI)

            callback.onCapabilitiesChanged(net, wifi)
            callback.onCapabilitiesChanged(net, wifi)
            advanceUntilIdle()

            verify(repo, times(2)).setDeviceNetworkState(
                DeviceNetworkType.WIFI,
                net.networkHandle.toString()
            )
        }

    @Test
    fun `a burst of reports is delivered in callback order`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()
            advanceUntilIdle()
            val wifiNet = network(109)
            val cellNet = network(110)

            callback.onCapabilitiesChanged(wifiNet, caps(NetworkCapabilities.TRANSPORT_WIFI))
            callback.onCapabilitiesChanged(cellNet, caps(NetworkCapabilities.TRANSPORT_CELLULAR))
            callback.onLost(cellNet)
            advanceUntilIdle()

            inOrder(repo) {
                verify(repo).setDeviceNetworkState(
                    DeviceNetworkType.WIFI, wifiNet.networkHandle.toString()
                )
                verify(repo).setDeviceNetworkState(
                    DeviceNetworkType.CELLULAR, cellNet.networkHandle.toString()
                )
                verify(repo).setDeviceNetworkState(DeviceNetworkType.NOT_CONNECTED, "")
            }
        }

    @Test
    fun `a failing RPC does not kill the consumer -- the next report is still delivered`() =
        runTest(dispatcher.scheduler) {
            repo.stub {
                onBlocking { setDeviceNetworkState(any(), any()) }
                    .doThrow(IllegalStateException("middleware not ready"))
                    .doReturn(Unit)
            }
            val callback = startDisconnected() // baseline NOT_CONNECTED -> throws
            advanceUntilIdle()

            val net = network(111)
            callback.onCapabilitiesChanged(net, caps(NetworkCapabilities.TRANSPORT_WIFI))
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(
                DeviceNetworkType.WIFI,
                net.networkHandle.toString()
            )
        }

    @Test
    fun `start with no default network reports a NOT_CONNECTED baseline`() =
        runTest(dispatcher.scheduler) {
            // No callback fires when there is no default network at launch, so
            // start() must prime heart's baseline explicitly.
            disconnectDefaultNetwork()
            subject.start()
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(DeviceNetworkType.NOT_CONNECTED, "")
        }

    @Test
    fun `stop unregisters and a following start registers and delivers again`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()
            advanceUntilIdle()
            subject.stop()
            assertFalse(shadowOf(connectivityManager).networkCallbacks.contains(callback))
            clearInvocations(repo)

            subject.start()
            val second = registeredCallback()
            val net = network(112)
            second.onCapabilitiesChanged(net, caps(NetworkCapabilities.TRANSPORT_CELLULAR))
            advanceUntilIdle()

            verify(repo).setDeviceNetworkState(
                DeviceNetworkType.CELLULAR,
                net.networkHandle.toString()
            )
        }

    @Test
    fun `a report landing after stop is dropped -- not replayed into the next session`() =
        runTest(dispatcher.scheduler) {
            // ConnectivityManager delivers callbacks asynchronously: one can land
            // after stop(). It must not become heart's baseline for the next login.
            val callback = startDisconnected()
            advanceUntilIdle()
            subject.stop()
            clearInvocations(repo)

            val stale = network(113)
            callback.onCapabilitiesChanged(stale, caps(NetworkCapabilities.TRANSPORT_WIFI))

            disconnectDefaultNetwork()
            subject.start()
            advanceUntilIdle()

            verify(repo, never()).setDeviceNetworkState(
                DeviceNetworkType.WIFI,
                stale.networkHandle.toString()
            )
            verify(repo).setDeviceNetworkState(DeviceNetworkType.NOT_CONNECTED, "")
        }

    @Test
    fun `an in-flight RPC from a stopped session completes before the next session's reports`() =
        runTest(dispatcher.scheduler) {
            // Cancellation is cooperative and the real RPC is a blocking JNI call
            // with no suspension points: a consumer stopped mid-call keeps running
            // until the call returns. Its stale report must not complete after the
            // next session's baseline -- heart trusts the last value it hears.
            val gate = CompletableDeferred<Unit>()
            val completions = mutableListOf<DeviceNetworkType>()
            repo.stub {
                onBlocking { setDeviceNetworkState(any(), any()) } doSuspendableAnswer { invocation ->
                    val type = invocation.getArgument<DeviceNetworkType>(0)
                    if (type == DeviceNetworkType.WIFI) {
                        // Simulates the wedged JNI call: survives cancellation.
                        withContext(NonCancellable) { gate.await() }
                    }
                    completions += type
                    Unit
                }
            }

            val callback = startDisconnected()
            advanceUntilIdle() // baseline NOT_CONNECTED delivered
            callback.onCapabilitiesChanged(network(120), caps(NetworkCapabilities.TRANSPORT_WIFI))
            advanceUntilIdle() // consumer now wedged inside the WIFI RPC

            subject.stop()
            disconnectDefaultNetwork()
            subject.start()
            advanceUntilIdle() // next session's baseline queued (or wrongly delivered)

            gate.complete(Unit) // the wedged call finally returns
            advanceUntilIdle()

            // The stale WIFI report must not be heart's final word for the new session.
            assertEquals(DeviceNetworkType.NOT_CONNECTED, completions.last())
        }

    @Test
    fun `getCurrentNetworkType reflects the last callback synchronously`() =
        runTest(dispatcher.scheduler) {
            val callback = startDisconnected()

            callback.onCapabilitiesChanged(
                network(114),
                caps(NetworkCapabilities.TRANSPORT_CELLULAR)
            )
            // Deliberately no advanceUntilIdle: the type must be visible before the
            // RPC coroutine runs.
            assertEquals(DeviceNetworkType.CELLULAR, subject.getCurrentNetworkType())
        }

    @Test
    fun `getCurrentNetworkType queries on demand when not monitoring`() {
        // WIFI differs from the field's initial NOT_CONNECTED, so this fails if
        // the not-monitoring branch ever returns the stale cached value.
        shadowOf(connectivityManager).setNetworkCapabilities(
            connectivityManager.activeNetwork,
            caps(NetworkCapabilities.TRANSPORT_WIFI)
        )
        assertEquals(DeviceNetworkType.WIFI, subject.getCurrentNetworkType())
    }

    @Test
    fun `start registers exactly one default network callback`() {
        disconnectDefaultNetwork()
        subject.start()
        subject.start() // idempotent
        assertTrue(shadowOf(connectivityManager).networkCallbacks.size == 1)
    }
}
