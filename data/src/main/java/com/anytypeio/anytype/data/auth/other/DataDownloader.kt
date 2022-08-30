package com.anytypeio.anytype.data.auth.other

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.download.Downloader

class DataDownloader(private val device: Device) : Downloader {

    override fun download(url: Url, name: String) = device.download(url, name)
}