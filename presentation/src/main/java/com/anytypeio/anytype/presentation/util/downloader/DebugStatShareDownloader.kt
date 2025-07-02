package com.anytypeio.anytype.presentation.util.downloader

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.PathProvider
import java.io.File
import javax.inject.Inject

class DebugStatShareDownloader @Inject constructor(
    private val pathProvider: PathProvider,
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, String>(dispatchers.io) {

    override suspend fun doWork(params: Unit): String {
        // Construct the path using the current time to ensure uniqueness (similar to DebugGoroutines)
        val path = "${pathProvider.cachePath()}/debug/stat/${System.currentTimeMillis()}/"
        val fileName = "debug_stat.json"
        val resultFilePath = "$path$fileName"

        // Create the directories if they do not exist
        File(path).apply { mkdirs() }

        // Get the debug stats JSON from repository
        val jsonString = repo.debugStats()

        // Save the JSON to file
        File(resultFilePath).writeText(jsonString)

        // Return the result file path
        return resultFilePath
    }
}