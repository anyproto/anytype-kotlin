package com.anytypeio.anytype.di.main

import android.content.Context
import com.anytypeio.anytype.data.auth.other.DataDownloader
import com.anytypeio.anytype.data.auth.other.Device
import com.anytypeio.anytype.device.base.AndroidDevice
import com.anytypeio.anytype.device.download.AndroidDeviceDownloader
import com.anytypeio.anytype.domain.download.Downloader
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