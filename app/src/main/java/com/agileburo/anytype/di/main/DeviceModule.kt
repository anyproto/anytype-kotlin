package com.agileburo.anytype.di.main

import android.content.Context
import com.agileburo.anytype.data.auth.other.DataDownloader
import com.agileburo.anytype.data.auth.other.Device
import com.agileburo.anytype.device.base.AndroidDevice
import com.agileburo.anytype.device.download.AndroidDeviceDownloader
import com.agileburo.anytype.domain.download.Downloader
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DeviceModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideDownloader(
        device: Device
    ): Downloader = DataDownloader(device = device)

    @JvmStatic
    @Provides
    @Singleton
    fun provideDevice(
        downloader: AndroidDeviceDownloader
    ): Device = AndroidDevice(downloader = downloader)

    @JvmStatic
    @Provides
    @Singleton
    fun provideDeviceDownloader(
        context: Context
    ): AndroidDeviceDownloader = AndroidDeviceDownloader(context = context)

}