package com.anytypeio.anytype.feature_discussions.tools

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import timber.log.Timber

fun launchCamera(
    context: Context,
    launcher: ManagedActivityResultLauncher<Uri, Boolean>,
    onUriReceived: (Uri) -> Unit
) {
    val tempDir = File(context.cacheDir, DISCUSSIONS_TEMP_FOLDER_NAME)
    if (!tempDir.exists()) {
        val created = tempDir.mkdirs()
        Timber.d("Created camera temp dir: $created at ${tempDir.absolutePath}")
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

    Timber.d("Launching camera with URI: $uri (path: ${photoFile.absolutePath})")

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
        val created = tempDir.mkdirs()
        Timber.d("Created video temp dir: $created at ${tempDir.absolutePath}")
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

    Timber.d("Launching video recorder with URI: $uri (path: ${videoFile.absolutePath})")

    onUriReceived(uri)
    launcher.launch(uri)
}

const val DISCUSSIONS_TEMP_FOLDER_NAME = "discussions_temp_folder"
