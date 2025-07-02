package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.PathProvider
import java.io.File
import javax.inject.Inject

class DebugGoroutines @Inject constructor(
    private val pathProvider: PathProvider,
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DebugGoroutines.Params, String>(dispatchers.io) {

    override suspend fun doWork(params: Params): String {
        val path: String
        val resultFilePath: String

        if (params.path == null) {
            // Construct the path using the current time to ensure uniqueness
            path = "${pathProvider.cachePath()}/debug/goroutines/${System.currentTimeMillis()}/"
            // Middleware put the log in a subdirectory called logs
            resultFilePath = "$path/logs/"

            // Create the directories if they do not exist
            File(path).apply { mkdirs() }
        } else {
            path = params.path
            resultFilePath = path
        }

        // Perform the debug operation
        repo.debugStackGoroutines(path)

        // Return the result file path
        return resultFilePath
    }

    // Data class for the parameters, with a default value of null for the path
    data class Params(val path: String? = null)
}