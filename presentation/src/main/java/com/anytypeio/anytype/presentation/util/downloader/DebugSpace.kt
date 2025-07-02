package com.anytypeio.anytype.presentation.util.downloader

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.util.downloader.DebugSpace.Params
import java.io.File
import javax.inject.Inject

class DebugSpace @Inject constructor(
    private val repo: BlockRepository,
    private val pathProvider: PathProvider,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, String>(dispatchers.io) {

    override suspend fun doWork(params: Params): String {
        // Get the space summary data from repository
        val spaceSummaryData = repo.debugSpace(space = params.spaceId)

        // Create the file path with timestamp for uniqueness
        val timestamp = System.currentTimeMillis()
        val path = "${pathProvider.cachePath()}/debug/space/${params.spaceId.id.take(16)}/"
        val fileName = "space_summary_$timestamp.json"
        val filePath = "$path$fileName"

        // Save the space summary data to file
        val file = File(filePath)
        file.writeText(spaceSummaryData)

        return filePath
    }

    data class Params(
        val spaceId: SpaceId
    )
}