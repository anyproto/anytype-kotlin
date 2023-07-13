package com.anytypeio.anytype.presentation.util.downloader

import android.net.Uri
import java.io.File

// TODO move to device module
interface UriFileProvider {
    fun getUriForFile(file: File): Uri
}