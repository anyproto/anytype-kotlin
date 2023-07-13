package com.anytypeio.anytype.device.share.debug

import android.content.Context
import com.anytypeio.anytype.data.auth.other.DebugSpaceDeviceContentSaver
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class DebugSpaceDeviceFileContentSaver @Inject constructor(
    private val context: Context
) : DebugSpaceDeviceContentSaver {

    @Throws(IOException::class)
    override fun save(content: String): File {
        val cacheDir = context.cacheDir

        require(cacheDir != null) { "Impossible to cache files!" }

        val filename = getFileName()

        // Creating folder

        val downloadFolder = File("${cacheDir.path}/$FOLDER_NAME/").apply {
            mkdirs()
        }

        val resultFilePath = "${cacheDir.path}/$FOLDER_NAME/${filename}"
        val resultFile = File(resultFilePath)

        // Writing content

        val tempFileFolderPath = "${downloadFolder.absolutePath}/$TEMP_FOLDER_NAME"
        val tempDir = File(tempFileFolderPath)
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        val tempResult = File(tempFileFolderPath, filename)

        FileOutputStream(tempResult).use { stream ->
            stream.write(content.toByteArray())
        }

        tempResult.renameTo(resultFile)

        // Clearing

        tempDir.deleteRecursively()

        // Sending file

        return resultFile
    }

    private fun getFileName(): String {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        return "DebugSpace$formattedDate.txt"
    }

    companion object {
        const val FOLDER_NAME = "debug_space"
        const val TEMP_FOLDER_NAME = "tmp"
        const val DATE_FORMAT = "dd-MM-yyyy-HH:mm:ss"
    }
}