package com.anytypeio.anytype.device

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.anytypeio.anytype.core_models.DeviceState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.SetDeviceState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class AppStateService @Inject constructor(
    private val setDeviceState: SetDeviceState,
    private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        handleStateTransition(DeviceState.FOREGROUND)
    }

    override fun onStop(owner: LifecycleOwner) {
        handleStateTransition(DeviceState.BACKGROUND)
    }

    private fun handleStateTransition(deviceState: DeviceState) {
        coroutineScope.launch {
            val params = SetDeviceState.Params(deviceState)
            setDeviceState.async(params).fold(
                onSuccess = {
                    Timber.d("Device state set successfully: $deviceState")
                },
                onFailure = { error ->
                    Timber.d("Failed to set device state: $error")
                }
            )
        }
    }
} 