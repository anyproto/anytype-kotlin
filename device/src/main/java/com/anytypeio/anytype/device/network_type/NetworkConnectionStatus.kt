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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class NetworkConnectionStatusImpl(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val blockRepository: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : NetworkConnectionStatus {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private var isMonitoring = false

    private var currentNetworkType: DeviceNetworkType = DeviceNetworkType.NOT_CONNECTED

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Called when the network is available
            updateNetworkState(networkCapabilities = getNetworkCapabilities())
        }

        override fun onLost(network: Network) {
            // Called when the network is disconnected
            updateNetworkState(networkCapabilities = getNetworkCapabilities())
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            // Called when the network capabilities (like Wi-Fi vs Cellular) change
            updateNetworkState(networkCapabilities)
        }
    }

    override fun start() {
        if (!isMonitoring) {
            try {
                connectivityManager?.registerDefaultNetworkCallback(networkCallback)
                isMonitoring = true
                updateNetworkState(networkCapabilities = getNetworkCapabilities())
            } catch (
                e: RuntimeException
            ) {
                Timber.w(e, "Failed to register network callback")
                isMonitoring = false
            }
        }
    }

    override fun stop() {
        if (isMonitoring) {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
                isMonitoring = false
            } catch (e: Throwable) {
                Timber.w(e, "Failed to unregister network callback")
                isMonitoring = false
            }
        }
    }

    private fun updateNetworkState(networkCapabilities: NetworkCapabilities?) {
        coroutineScope.launch {
            val networkType = mapNetworkType(networkCapabilities)
            currentNetworkType = networkType
            withContext(dispatchers.io) {
                try {
                    blockRepository.setDeviceNetworkState(networkType)
                } catch (
                    e: Throwable
                ) {
                    Timber.w(e, "Failed to update network state")
                }
            }
        }
    }

    private fun getNetworkCapabilities(): NetworkCapabilities? {
        try {
            return connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        } catch (e: Throwable) {
            Timber.w(e, "Failed to get network capabilities")
            return null
        }
    }

    private fun mapNetworkType(networkCapabilities: NetworkCapabilities?): DeviceNetworkType =
        when {
            networkCapabilities == null -> DeviceNetworkType.NOT_CONNECTED
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> DeviceNetworkType.WIFI
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> DeviceNetworkType.CELLULAR
            else -> DeviceNetworkType.NOT_CONNECTED
        }

    override fun getCurrentNetworkType(): DeviceNetworkType {
        if (!isMonitoring) {
            return mapNetworkType(getNetworkCapabilities())
        }
        return currentNetworkType
    }
}