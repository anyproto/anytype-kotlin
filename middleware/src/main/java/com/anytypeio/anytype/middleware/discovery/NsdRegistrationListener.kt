package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import javax.inject.Inject
import timber.log.Timber

class NsdRegistrationListener @Inject constructor() : NsdManager.RegistrationListener {
    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        Timber.d("Mdns service registered: $serviceInfo")
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Timber.e("Mdns service registration failed, info: $serviceInfo errorCode: $errorCode")
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
        Timber.d("Mdns service unregistered: $serviceInfo")
    }

    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Timber.e("Mdns service unregistration failed, info: $serviceInfo errorCode: $errorCode")
    }
}