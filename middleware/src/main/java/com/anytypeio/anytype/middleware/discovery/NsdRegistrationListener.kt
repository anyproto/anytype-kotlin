package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import javax.inject.Inject
import service.DiscoveryObserver
import timber.log.Timber

class NsdRegistrationListener @Inject constructor() : NsdManager.RegistrationListener {

    @Volatile
    private var observer: DiscoveryObserver? = null

    fun registerObserver(observer: DiscoveryObserver) {
        this.observer = observer
    }

    fun unregisterObserver() {
        observer = null
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        Timber.d("Mdns service registered: $serviceInfo")
        observer?.observeError(0, "")
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Timber.e("Mdns service registration failed, info: $serviceInfo errorCode: $errorCode")
        observer?.observeNsdError(
            errorCode,
            "NSD service registration failed: ${serviceInfo.serviceName}"
        )
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
        Timber.d("Mdns service unregistered: $serviceInfo")
    }

    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Timber.e("Mdns service unregistration failed, info: $serviceInfo errorCode: $errorCode")
        observer?.observeNsdError(
            errorCode,
            "NSD service unregistration failed: ${serviceInfo.serviceName}"
        )
    }
}
