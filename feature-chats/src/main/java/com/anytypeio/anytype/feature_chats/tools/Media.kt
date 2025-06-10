package com.anytypeio.anytype.feature_chats.tools

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import javax.inject.Inject
import timber.log.Timber


fun launchCamera(
    context: Context,
    launcher: ManagedActivityResultLauncher<Uri, Boolean>,
    onUriReceived: (Uri) -> Unit
) {
    val tempDir = File(context.cacheDir, CHATS_TEMP_FOLDER_NAME)
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

const val CHATS_TEMP_FOLDER_NAME = "chats_temp_folder"

class ClearChatsTempFolder @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val CHATS_TEMP_FOLDER_NAME = "chats_temp_folder"
    }

    operator fun invoke(): Boolean {
        val folder = File(context.cacheDir, CHATS_TEMP_FOLDER_NAME)
        return if (folder.exists()) {
            val deleted = folder.deleteRecursively()
            // Optional: log if needed
            deleted
        } else {
            false
        }
    }
}