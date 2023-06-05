package com.anytypeio.anytype.ui_settings.account.repo

import android.net.Uri
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.debugging.DebugSpace
import java.text.SimpleDateFormat
import java.util.*

class DebugSpaceShareDownloader(
    private val debugSpace: DebugSpace,
    private val fileSaver: FileSaver,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<Unit, Uri>(dispatchers.io) {

    private fun getFileName(): String {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val fileName = "DebugSpace$formattedDate.txt"
        return fileName
    }

    override suspend fun doWork(params: Unit): Uri {
        val content = debugSpace.run(Unit)
        return fileSaver.run(FileSaver.Params(content = content, name = getFileName()))
    }
}