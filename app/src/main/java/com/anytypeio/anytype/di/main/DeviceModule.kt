package com.anytypeio.anytype.di.main

import android.content.Context
import com.anytypeio.anytype.data.auth.other.DataDownloader
import com.anytypeio.anytype.data.auth.other.Device
import com.anytypeio.anytype.device.DefaultLocalProvider
import com.anytypeio.anytype.device.SharedFileUploader
import com.anytypeio.anytype.device.base.AndroidDevice
import com.anytypeio.anytype.device.download.AndroidDeviceDownloader
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.download.Downloader
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
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

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocaleProvider(context: Context): LocaleProvider = DefaultLocalProvider(context)

    @JvmStatic
    @Provides
    @Singleton
    fun provideFileSharer(context: Context): FileSharer = SharedFileUploader(context)
}