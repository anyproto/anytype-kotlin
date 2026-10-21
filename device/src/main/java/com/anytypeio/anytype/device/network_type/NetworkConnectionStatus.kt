package com.anytypeio.anytype.device.network_type

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.anytypeio.anytype.core_models.DeviceNetworkType
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.NetworkConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber

class NetworkConnectionStatusImpl(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val blockRepository: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : NetworkConnectionStatus {

    private data class Report(val type: DeviceNetworkType, val networkId: String)

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // Reports must reach the middleware in the order the OS produced them: heart
    // coalesces bursts into one trailing run where the last value wins, so a stale
    // report overtaking a newer one would wedge its view of connectivity. The
    // channel lives for the object's lifetime and is never closed, so start/stop
    // can cycle across login/logout.
    private val reports = Channel<Report>(Channel.UNLIMITED)
    private var consumer: Job? = null

    @Volatile
    private var isMonitoring = false

    @Volatile
    private var currentNetworkType: DeviceNetworkType = DeviceNetworkType.NOT_CONNECTED

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            // networkHandle is stable per network instance and changes on every
            // reconnect -- exactly the identity semantics networkId wants.
            report(mapNetworkType(networkCapabilities), network.networkHandle.toString())
        }

        // On a default-network callback onLost means "lost default status", not
        // "disconnected": it fires on every Wi-Fi<->cellular handoff. Report the
        // network that is default now; only a genuinely absent default yields
        // NOT_CONNECTED, which heart treats as fully offline and throttles.
        override fun onLost(network: Network) {
            reportCurrent()
        }

        // Never delivered for registerDefaultNetworkCallback (the platform only
        // calls it for requestNetwork with a timeout). Kept so a change of
        // registration style cannot silently lose offline reports.
        override fun onUnavailable() {
            report(DeviceNetworkType.NOT_CONNECTED, "")
        }
    }

    override fun start() {
        if (isMonitoring) return
        // Discard anything a late callback pushed after the previous session's
        // stop() -- a stale value must not become heart's baseline for this one.
        while (reports.tryReceive().isSuccess) {
            // drop
        }
        try {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
            isMonitoring = true
        } catch (e: RuntimeException) {
            Timber.w(e, "Failed to register network callback")
            return
        }
        val previous = consumer
        consumer = coroutineScope.launch(dispatchers.io) {
            // Cancellation is cooperative and the RPC below is a blocking JNI call
            // with no suspension points: a consumer stopped mid-call keeps running
            // until the call returns. Wait it out so its report cannot complete
            // after (and thereby override) this session's.
            previous?.join()
            for (report in reports) {
                try {
                    blockRepository.setDeviceNetworkState(report.type, report.networkId)
                } catch (e: Throwable) {
                    // Per-report: a throw escaping the loop would silently disable
                    // network reporting for the rest of the session.
                    Timber.w(e, "Failed to update network state")
                }
            }
        }
        // No callback fires when there is no default network at launch, so prime
        // heart's baseline explicitly; with a default up this is a cheap duplicate
        // of the onCapabilitiesChanged that follows registration.
        reportCurrent()
    }

    override fun stop() {
        if (!isMonitoring) return
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Throwable) {
            Timber.w(e, "Failed to unregister network callback")
        } finally {
            isMonitoring = false
            // Cancel but keep the reference: the next start()'s consumer joins it,
            // so an RPC in flight right now cannot complete after -- and thereby
            // override -- the next session's reports.
            consumer?.cancel()
        }
    }

    override fun getCurrentNetworkType(): DeviceNetworkType =
        if (isMonitoring) currentNetworkType
        else mapNetworkType(activeNetworkAndCaps()?.second)

    private fun report(type: DeviceNetworkType, networkId: String) {
        currentNetworkType = type
        // trySend on an UNLIMITED channel never suspends nor fails-on-full, so
        // ConnectivityManager's callback thread is never blocked.
        reports.trySend(Report(type, networkId))
    }

    private fun reportCurrent() {
        val (network, caps) = activeNetworkAndCaps()
            ?: return report(DeviceNetworkType.NOT_CONNECTED, "")
        report(mapNetworkType(caps), network.networkHandle.toString())
    }

    /** The default network and its capabilities, or null if there is no usable default. */
    private fun activeNetworkAndCaps(): Pair<Network, NetworkCapabilities>? {
        val manager = connectivityManager ?: return null
        val network = manager.activeNetwork ?: return null
        val caps = try {
            manager.getNetworkCapabilities(network)
        } catch (e: Throwable) {
            Timber.w(e, "Failed to get network capabilities")
            null
        } ?: return null
        return network to caps
    }

    // Transport only. Deliberately NOT gated on NET_CAPABILITY_VALIDATED:
    // validation gates internet, but a LAN-only network (or a captive portal)
    // still syncs over local P2P, and heart throttles NOT_CONNECTED devices.
    // See anytype-heart 8a2fa9d91 / docs/mobile-network-integration.md.
    private fun mapNetworkType(networkCapabilities: NetworkCapabilities?): DeviceNetworkType =
        when {
            networkCapabilities == null -> DeviceNetworkType.NOT_CONNECTED
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                DeviceNetworkType.CELLULAR
            // wifi / ethernet / vpn / tethering, incl. LAN-only without internet
            else -> DeviceNetworkType.WIFI
        }
}
