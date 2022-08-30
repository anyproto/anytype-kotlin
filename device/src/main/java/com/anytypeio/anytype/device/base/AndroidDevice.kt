package com.anytypeio.anytype.device.base

import com.anytypeio.anytype.data.auth.other.Device
import com.anytypeio.anytype.device.download.AndroidDeviceDownloader

class AndroidDevice(
    private val downloader: AndroidDeviceDownloader
) : Device {
    override fun download(url: String, name: String) = downloader.download(url = url, name = name)
}