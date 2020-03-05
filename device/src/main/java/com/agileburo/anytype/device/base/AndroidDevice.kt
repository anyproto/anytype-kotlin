package com.agileburo.anytype.device.base

import com.agileburo.anytype.data.auth.other.Device
import com.agileburo.anytype.device.download.DeviceDownloader

class AndroidDevice(private val downloader: DeviceDownloader) : Device {

    override fun download(url: String, name: String) {
        downloader.download(url = url, name = name)
    }
}