package com.anytypeio.anytype.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.networkmode.SetNetworkMode
import com.anytypeio.anytype.presentation.editor.picker.PickerListener
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.OnCopyFileToCacheAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class PreferencesViewModel(
    private val copyFileToCache: CopyFileToCacheDirectory,
    private val getNetworkMode: GetNetworkMode,
    private val setNetworkMode: SetNetworkMode
) : ViewModel(), PickerListener {

    val networkModeState = MutableStateFlow(NetworkModeConfig(NetworkMode.DEFAULT, "", ""))

    fun onStart() {
        Timber.d("onStart")
        viewModelScope.launch {
            getNetworkMode.async(Unit).fold(
                onSuccess = {
                    networkModeState.value = it
                    Timber.d("Successfully get network mode on Start: $it")
                },
                onFailure = {
                    Timber.e(it, "Failed to get network mode")
                }
            )
        }
    }

    fun proceedWithNetworkMode(mode: String?) {
        viewModelScope.launch {
            val config = when (mode) {
                NetworkMode.LOCAL.value -> NetworkModeConfig(NetworkMode.LOCAL)
                NetworkMode.CUSTOM.value -> NetworkModeConfig(NetworkMode.CUSTOM)
                else -> NetworkModeConfig()
            }
            networkModeState.value = config
            setNetworkMode.async(SetNetworkMode.Params(config)).fold(
                onSuccess = { Timber.d("Successfully update network mode with config:$config") },
                onFailure = { Timber.e(it, "Failed to set network mode") }
            )
        }
    }

    fun proceedWithConfigFiles(userFilePath: String?, storedFilePath: String?) {
        viewModelScope.launch {
            val config = NetworkModeConfig(
                networkMode = NetworkMode.CUSTOM,
                userFilePath = userFilePath,
                storedFilePath = storedFilePath
            )
            val params = SetNetworkMode.Params(config)
            setNetworkMode.async(params).fold(
                onSuccess = {
                    networkModeState.value = config
                    Timber.d("Successfully update network mode with config:$config")
                },
                onFailure = { Timber.e(it, "Failed to set network mode") }
            )
        }
    }

    override fun onStartCopyFileToCacheDir(uri: Uri) {
        Timber.d("onStartCopyFileToCacheDir: $uri")
        copyFileToCache.execute(
            uri = uri,
            scope = viewModelScope,
            listener = copyFileListener
        )
    }

    override fun onCancelCopyFileToCacheDir() {
        copyFileToCache.cancel()
    }

    override fun onPickedDocImageFromDevice(ctx: Id, path: String) {}
    override fun onProceedWithFilePath(filePath: String?) {}

    private val copyFileListener = object : OnCopyFileToCacheAction {
        override fun onCopyFileStart() {}
        override fun onCopyFileError(msg: String) {}

        override fun onCopyFileResult(result: String?, fileName: String?) {
            proceedWithConfigFiles(
                userFilePath = fileName,
                storedFilePath = result
            )
        }
    }

    class Factory(
        private val copyFileToCacheDirectory: CopyFileToCacheDirectory,
        private val getNetworkMode: GetNetworkMode,
        private val setNetworkMode: SetNetworkMode
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = PreferencesViewModel(
            copyFileToCache = copyFileToCacheDirectory,
            getNetworkMode = getNetworkMode,
            setNetworkMode = setNetworkMode
        ) as T
    }
}