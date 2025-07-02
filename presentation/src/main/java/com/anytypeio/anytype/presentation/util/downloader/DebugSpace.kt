package com.anytypeio.anytype.presentation.util.downloader

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.PathProvider
import java.io.File
import javax.inject.Inject

class DebugSpace @Inject constructor(
    private val repo: BlockRepository,
    private val pathProvider: PathProvider,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<String, String>(dispatchers.io) {

    override suspend fun doWork(params: String): String {
        // Get the space summary data from repository
        val spaceSummaryData = repo.debugSpace(space = SpaceId(params))

        // Create the file path with timestamp for uniqueness
        val timestamp = System.currentTimeMillis()
        val fileName = "space_summary_$timestamp.json"
        val filePath = "${pathProvider.cachePath()}/$fileName"

        // Save the space summary data to file
        val file = File(filePath)
        file.writeText(spaceSummaryData)

        return filePath
    }
}