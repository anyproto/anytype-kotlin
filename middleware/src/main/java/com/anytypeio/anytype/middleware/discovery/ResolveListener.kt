package com.anytypeio.anytype.middleware.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.sync.Semaphore
import timber.log.Timber

internal class ResolveListener(
    private val semaphore: Semaphore,
    private val onResolved: (NsdServiceInfo) -> Unit,
    private val onFailed: (NsdServiceInfo, Int) -> Unit
) : NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        try {
            onFailed(serviceInfo, errorCode)
        } catch (e: Exception) {
            Timber.e(e, "Error while reporting NSD resolve failure")
        } finally {
            releaseSemaphore()
        }
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Timber.d("Mdns discovery resolve succeed: $serviceInfo")
        try {
            onResolved(serviceInfo)
        } catch (e: Exception) {
            Timber.e(e, "Error after onServiceResolved")
        } finally {
            releaseSemaphore()
        }
    }

    private fun releaseSemaphore() {
        try {
            semaphore.release()
        } catch (e: Exception) {
            Timber.e(e, "Error while releasing semaphore")
        }
    }
}
