package com.anytypeio.anytype.providers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import java.io.File
import javax.inject.Inject

class DefaultUriFileProvider @Inject constructor(
    private val context: Context
) : UriFileProvider {

    override fun getUriForFile(file: File): Uri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + PROVIDER,
        file
    )
}

private const val PROVIDER = ".provider"