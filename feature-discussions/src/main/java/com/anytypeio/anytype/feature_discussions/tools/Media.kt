package com.anytypeio.anytype.feature_discussions.tools

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
fun launchCamera(
    context: Context,
    launcher: ManagedActivityResultLauncher<Uri, Boolean>,
    onUriReceived: (Uri) -> Unit
) {
    val tempDir = File(context.cacheDir, DISCUSSIONS_TEMP_FOLDER_NAME)
    if (!tempDir.exists()) {
        tempDir.mkdirs()
    }

    val photoFile = File.createTempFile("IMG_", ".jpg", tempDir).apply {
        createNewFile()
        deleteOnExit()
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        photoFile
    )

    onUriReceived(uri)
    launcher.launch(uri)
}

fun launchVideoRecorder(
    context: Context,
    launcher: ManagedActivityResultLauncher<Uri, Boolean>,
    onUriReceived: (Uri) -> Unit
) {
    val tempDir = File(context.cacheDir, DISCUSSIONS_TEMP_FOLDER_NAME)
    if (!tempDir.exists()) {
        tempDir.mkdirs()
    }

    val videoFile = File.createTempFile("VID_", ".mp4", tempDir).apply {
        createNewFile()
        deleteOnExit()
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        videoFile
    )

    onUriReceived(uri)
    launcher.launch(uri)
}

const val DISCUSSIONS_TEMP_FOLDER_NAME = "discussions_temp_folder"
