package com.agileburo.anytype.device.download

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import timber.log.Timber

class DeviceDownloader(private val context: Context) {

    private val manager by lazy {
        context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    fun download(url: String, name: String) {

        Timber.d("Downloading file: $name from url: $url")

        val uri = Uri.parse(url)

        context.getExternalFilesDir(DIRECTORY_DOWNLOADS)?.mkdirs()

        val request = DownloadManager.Request(uri)
            .setTitle(name)
            .setDescription(DESCRIPTION_TEXT)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, name)

        manager.enqueue(request)
    }

    companion object {
        const val DESCRIPTION_TEXT = "Downloading..."
    }
}