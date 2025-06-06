package com.anytypeio.anytype.feature_chats.tools

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
    val photoFile = File.createTempFile("IMG_", ".jpg", context.cacheDir).apply {
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