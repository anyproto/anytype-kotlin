package com.agileburo.anytype.di.main

import android.content.Context
import com.agileburo.anytype.data.auth.other.DataDownloader
import com.agileburo.anytype.data.auth.other.Device
import com.agileburo.anytype.device.base.AndroidDevice
import com.agileburo.anytype.device.download.DeviceDownloader
import com.agileburo.anytype.domain.download.Downloader
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DeviceModule {

    @Provides
    @Singleton
    fun provideDownloader(
        device: Device
    ): Downloader = DataDownloader(device = device)

    @Provides
    @Singleton
    fun provideDevice(
        downloader: DeviceDownloader
    ): Device = AndroidDevice(downloader = downloader)

    @Provides
    @Singleton
    fun provideDeviceDownloader(
        context: Context
    ): DeviceDownloader = DeviceDownloader(context = context)

}