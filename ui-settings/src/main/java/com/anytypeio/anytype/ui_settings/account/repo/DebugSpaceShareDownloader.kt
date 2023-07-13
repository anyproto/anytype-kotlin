package com.anytypeio.anytype.ui_settings.account.repo

import android.net.Uri
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.debugging.DebugSpace
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DebugSpaceShareDownloader(
    private val debugSpace: DebugSpace,
    private val fileSaver: DebugSpaceFileContentSaver,
    private val uriFileProvider: UriFileProvider,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<Unit, Uri>(dispatchers.io) {

    private fun getFileName(): String {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        return "DebugSpace$formattedDate.txt"
    }

    override suspend fun doWork(params: Unit): Uri {
        val content = debugSpace.run(Unit)
        val file = fileSaver.run(
            DebugSpaceFileContentSaver.Params(
                content = content,
                filename = getFileName()
            )
        )
        return uriFileProvider.getUriForFile(file)
    }

    companion object {
        const val DATE_FORMAT = "dd-MM-yyyy-HH:mm:ss"
    }
}