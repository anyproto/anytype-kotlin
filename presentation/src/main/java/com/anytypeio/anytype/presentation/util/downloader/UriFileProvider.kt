package com.anytypeio.anytype.presentation.util.downloader

import android.net.Uri
import java.io.File

interface UriFileProvider {

    fun getUriForFile(file: File): Uri

}