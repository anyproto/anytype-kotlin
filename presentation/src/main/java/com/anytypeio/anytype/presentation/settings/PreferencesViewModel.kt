package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.networkmode.SetNetworkMode
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory

class PreferencesViewModel(
    private val copyFileToCache: CopyFileToCacheDirectory,
    private val getNetworkMode: GetNetworkMode,
    private val setNetworkMode: SetNetworkMode
) : ViewModel() {

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