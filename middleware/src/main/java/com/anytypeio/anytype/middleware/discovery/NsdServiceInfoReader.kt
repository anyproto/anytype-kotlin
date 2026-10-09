package com.anytypeio.anytype.middleware.discovery

import android.annotation.SuppressLint
import android.net.Network
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.ext.SdkExtensions

internal interface NsdServiceInfoReader {
    fun supportsUpdates(): Boolean
    fun network(service: NsdServiceInfo): Network?
    fun addresses(service: NsdServiceInfo): List<String>
    fun unscoped(service: NsdServiceInfo): NsdServiceInfo
}

internal class AndroidNsdServiceInfoReader(
    private val sdkInt: Int = Build.VERSION.SDK_INT,
    private val tExtension: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU)
    } else {
        0
    }
) : NsdServiceInfoReader {
    override fun supportsUpdates(): Boolean = sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        (sdkInt >= Build.VERSION_CODES.TIRAMISU && tExtension >= 7)

    @SuppressLint("NewApi") // sdkInt is the device API level; injectable for capability-boundary tests.
    override fun network(service: NsdServiceInfo): Network? =
        if (sdkInt >= Build.VERSION_CODES.TIRAMISU) service.network else null

    @SuppressLint("NewApi") // supportsUpdates also covers Android 13's T extension 7 backport.
    override fun addresses(service: NsdServiceInfo): List<String> {
        val hosts = if (supportsUpdates()) {
            service.hostAddresses
        } else {
            @Suppress("DEPRECATION")
            listOfNotNull(service.host)
        }
        return hosts.mapNotNull { it.hostAddress }.filter { it.isNotBlank() }.distinct()
    }

    override fun unscoped(service: NsdServiceInfo): NsdServiceInfo = NsdServiceInfo().apply {
        serviceName = service.serviceName
        serviceType = service.serviceType
    }
}
