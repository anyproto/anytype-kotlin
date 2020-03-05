package com.agileburo.anytype.data.auth.other

import com.agileburo.anytype.domain.common.Url
import com.agileburo.anytype.domain.download.Downloader

class DataDownloader(private val device: Device) : Downloader {

    override fun download(url: Url, name: String) {
        device.download(url, name)
    }
}