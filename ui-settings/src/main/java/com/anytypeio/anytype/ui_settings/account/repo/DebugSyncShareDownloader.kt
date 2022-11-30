package com.anytypeio.anytype.ui_settings.account.repo

import android.net.Uri
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.debugging.DebugSync
import java.text.SimpleDateFormat
import java.util.*

class DebugSyncShareDownloader(
    private val debugSync: DebugSync,
    private val fileSaver: FileSaver,
) : ResultInteractor<Unit, Uri>() {

    private fun getFileName(): String {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val fileName = "DebugSync$formattedDate.txt"
        return fileName
    }

    override suspend fun doWork(params: Unit): Uri {
        val content = debugSync.run(Unit)
        return fileSaver.run(FileSaver.Params(content = content, name = getFileName()))
    }
}